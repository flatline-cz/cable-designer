package biz.flatsw.cabledesigner;

import biz.flatsw.cabledesigner.model.*;
import biz.flatsw.cabledesigner.model.defs.ConnectorPinComponent;
import biz.flatsw.cabledesigner.model.defs.WireType;

import java.util.*;

public class ComponentAssigner {
    private final List<WireClass> wireClasses = new ArrayList<>();
    private final Map<SignalPath, Integer> signalPathWireIndexes = new HashMap<>();


    private void phaseOneAssignWires() {

        Services.getSignalManager().listSignals().forEach(
                signal -> signal.getSignalPaths().forEach(signalPath -> {
                    int i, c = wireClasses.size();
                    for (i = 0; i < c; i++) {
                        if (!wireClasses.get(i).match(signalPath.getSpecification()))
                            continue;
                        signalPathWireIndexes.put(signalPath, i);
                        return;
                    }
                    throw new RuntimeException("Unable to find wire type for signal '" + signal.getName() + "'");
                }));

    }

    private int findBiggerWire(int currentIndex, SignalPath signalPath) {
        for (++currentIndex; currentIndex < wireClasses.size(); currentIndex++) {
            if (wireClasses.get(currentIndex).match(signalPath.getSpecification()))
                break;
        }
        return (currentIndex < wireClasses.size()) ? currentIndex : -1;
    }

    private float computePinCrossSection(Pin pin) {
        float crossSection = 0;
        int count = 0;
        for (SignalPath signalPath : pin.getSignalPaths()) {
            count++;
            Integer wcIndex = signalPathWireIndexes.get(signalPath);
            if (wcIndex == null)
                throw new RuntimeException("Wire class not assigned to signal '" + signalPath.getSignal().getName() + "'");
            crossSection += wireClasses.get(wcIndex).wireSection;
        }
        return (count == 1) ? crossSection : (crossSection * 1.f);
    }

    private float computePinInsulationDiameter(Pin pin) {
        float insulationDiameter = 0;
        int count = 0;
        for (SignalPath signalPath : pin.getSignalPaths()) {
            count++;
            Integer wcIndex = signalPathWireIndexes.get(signalPath);
            if (wcIndex == null)
                throw new RuntimeException("Wire class not assigned to signal '" + signalPath.getSignal().getName() + "'");
            float d = wireClasses.get(wcIndex).insulationDiameter;
            insulationDiameter += d * d;
        }
        return (float) ((count == 1) ? Math.sqrt(insulationDiameter) : (Math.sqrt(insulationDiameter) * 1.f));
    }

    private void checkPinWiring() {
        Services.getSignalManager()
                .listSignals()
                .stream()
                .flatMap(signal -> signal.getSignalPaths().stream())
                .forEach(this::checkPinWiring);
    }

    private void checkPinWiring(SignalPath signalPath) {
        for (Pin pin : signalPath.listTerminals()) {
            System.out.println("Signal '" + signalPath.getSignal().getName() + "'. Checking terminal: " + pin);
            // compute cross-section
            float crossSection = computePinCrossSection(pin);
            // compute insulation
            float insulationDiameter = computePinInsulationDiameter(pin);
            pin.getConnector().getModel().checkWiring(pin, crossSection, insulationDiameter);
        }
    }

    private boolean updateSignalPath(SignalPath signalPath) {
        System.out.println("Updating signal path '" + signalPath.getSignal().getName() + "', pins = " + signalPath.listTerminals());
        int currentIndex = signalPathWireIndexes.get(signalPath);
        while (true) {
            currentIndex = findBiggerWire(currentIndex, signalPath);
            if (currentIndex == -1)
                break;
            signalPathWireIndexes.put(signalPath, currentIndex);
            System.out.println("Trying wire class: " + wireClasses.get(currentIndex));
            try {
                checkPinWiring(signalPath);
            } catch (UnderMinimumException exMin) {
                System.out.println(" - not enough: " + exMin);
                continue;
            } catch (OverMaximumException exMax) {
                System.out.println(" - failed: " + exMax);
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean fitPinWiring(Pin pin) {
        // list of signal paths
        Collection<SignalPath> signalPaths = pin.getSignalPaths();
        Map<SignalPath, Integer> backup = new HashMap<>(signalPathWireIndexes);

        for (SignalPath signalPath : signalPaths) {
            signalPathWireIndexes.clear();
            signalPathWireIndexes.putAll(backup);
            if (updateSignalPath(signalPath))
                return true;
        }
        return false;
    }

    private void assignWireColors() {
        Services.getSignalManager().listSignals().forEach(
                signal -> signal.getSignalPaths().forEach(signalPath -> {
                    Integer wcIndex = signalPathWireIndexes.get(signalPath);
                    if (wcIndex == null)
                        throw new RuntimeException("Wire class not assigned to signal '" + signal.getName() + "'");

                    Optional<WireType> wireType = wireClasses.get(wcIndex).wireTypes
                            .stream()
                            .filter(wt -> wt.matches(signalPath.getSpecification()))
                            .findFirst();
                    if (!wireType.isPresent())
                        throw new RuntimeException("Unable to find wire type for signal '" + signal.getName() + "'");
                    signalPath.setWireType(wireType.get());
                }));
    }

    private void assignPinsAndSeals() {
        Set<Pin> assigned = new HashSet<>();

        for (Signal signal : Services.getSignalManager().listSignals()) {
            for (SignalPath signalPath : signal.getSignalPaths()) {
                for (Pin pin : signalPath.listTerminals()) {
                    if (assigned.contains(pin))
                        continue;
                    assigned.add(pin);

                    // compute cross-section
                    float crossSection = computePinCrossSection(pin);
                    // compute insulation
                    float insulationDiameter = computePinInsulationDiameter(pin);

                    ConnectorPinComponent pinType = pin
                            .getConnector()
                            .getModel()
                            .findSuitablePinType(
                                    pin.getPosition(),
                                    crossSection,
                                    insulationDiameter);
                    pin.setPinType(pinType);
                    // seal type
                    ConnectorPinComponent sealType = pin
                            .getConnector()
                            .getModel()
                            .findSuitablePinSeal(
                                    pin.getPosition(),
                                    crossSection,
                                    insulationDiameter);
                    pin.setSealType(sealType);

                }
            }
        }
    }

    public void assignComponents() {

        // make a list of suitable wires
        Services.getDefinitionManager().listWireTypes().forEach(wireType -> {
            Optional<WireClass> wireClass = wireClasses.stream().filter(wc -> wc.match(wireType)).findFirst();
            if (wireClass.isPresent())
                wireClass.get().addWireType(wireType);
            else
                wireClasses.add(new WireClass(wireType));
        });
        wireClasses.sort(WireClass::compare);
//        wireClasses.forEach(System.out::println);

        // assign wires
        phaseOneAssignWires();

        // validate models
        int count = 0;
        do {
            try {
                checkPinWiring();
                break;
            } catch (UnderMinimumException exMin) {
                // ignore
                if (!fitPinWiring(exMin.getPin()))
                    throw new RuntimeException("Unable to fit wires to pin: " + exMin.getPin() + ", cross-section=" + exMin.getCrossSection() + ", insulation=" + exMin.getInsulationDiameter());
            }
        } while (++count < 10);

        // assign actual wire types
        assignWireColors();

        // assign pin & seals
        assignPinsAndSeals();
    }

    static class WireClass {
        private final float wireSection;
        private final float insulationDiameter;
        private final float currentRating;
        private final List<WireType> wireTypes;

        WireClass(WireType wireType) {
            wireTypes = new ArrayList<>();
            wireTypes.add(wireType);
            wireSection = wireType.getWireSection();
            insulationDiameter = wireType.getInsulationDiameter();
            currentRating = wireType.getCurrentRating();
        }

        public boolean match(WireType wireType) {
            if (wireType.getWireSection() != wireSection)
                return false;
            if (wireType.getCurrentRating() != currentRating)
                return false;
            return (wireType.getInsulationDiameter() == insulationDiameter);
        }

        public void addWireType(WireType wireType) {
            wireTypes.add(wireType);
        }

        public float getWireSection() {
            return wireSection;
        }

        public float getInsulationDiameter() {
            return insulationDiameter;
        }

        public float getCurrentRating() {
            return currentRating;
        }

        public boolean match(SignalSpecification signalSpecification) {
            return wireTypes
                    .stream()
                    .anyMatch(wireType -> wireType.matches(signalSpecification));
        }

        public static int compare(WireClass o1, WireClass o2) {
            return Comparator
                    .comparing(WireClass::getCurrentRating)
                    .thenComparing(WireClass::getWireSection)
                    .thenComparing(WireClass::getInsulationDiameter)
                    .compare(o1, o2);
        }

        @Override
        public String toString() {
            return "current: " + currentRating + ", cross: " + wireSection + ", insulation: " + insulationDiameter + ", count: " + wireTypes.size();
        }
    }


}
