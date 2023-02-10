package biz.flatsw.cabledesigner;

import biz.flatsw.cabledesigner.model.defs.ConnectorPinComponent;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorAssigner {
    private final static Pattern harnessPattern = Pattern.compile("=+ Cables \\(([^)]+)\\) =+");
    private final Map<CableKey, Cable> cables = new HashMap<>();
    private final Map<String, Signal> signals = new HashMap<>();

    private void loadFile(String filename) throws IOException {
        FileInputStream fstream = new FileInputStream(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

        String harness = null;
        String strLine;
        // Read File Line By Line
        while ((strLine = br.readLine()) != null) {
            strLine = strLine.trim();
            if (harness == null) {
                Matcher matcher = harnessPattern.matcher(strLine);
                if (!matcher.matches())
                    continue;
                harness = matcher.group(1);
                continue;
            }

            String[] parts = strLine.split("\t");
            if (parts.length != 4 && parts.length != 5)
                continue;
            String cableName = parts[0];
            String signalName = parts[1];
            float wireSection = Float.parseFloat(parts[2]);
            float cableLength = Float.parseFloat(parts[3]);
            String colors = (parts.length == 5) ? parts[4] : null;


            CableKey key = new CableKey(harness, cableName);
            Cable cable = cables.get(key);
            if (cable == null) {
                cable = new Cable(key);
                cables.put(key, cable);
            }

            Signal signal = signals.get(signalName);
            if (signal == null) {
                signal = new Signal(signalName);
                signals.put(signalName, signal);
            }

            cable.addSignal(signal, cableLength);

            Wire wire = new Wire(signal, cable, wireSection, colors);
            cable.addWire(wire);
            signal.addWire(wire);


            if (colors != null)
                signal.addColor(colors);
        }

        // Close the input stream
        fstream.close();


    }

    private void process() {
        dumpCables();
        dumpSignalColors();
    }


    private void dumpCables() {
        cables.forEach((name, cable) -> {
            System.out.println("Cable '" + name + "':");
            for (Wire wire : cable.wires) {
                System.out.println("  Signal " + wire.signal.getName() + ": " + String.format("%.2fmm²", wire.getWireSection())+" "+wire.signal.colors);
            }
        });
    }


    private Signal findSignalToRoute() {
        return signals.values().stream()
                .filter(s -> s.getColors().size() == 0)
                .sorted(Comparator.comparingDouble(Signal::getMaxWireSection).reversed().thenComparing(Signal::getLength).reversed())
                .findFirst()
                .orElse(null);
    }

    private void dumpSignalColors() {
        signals.values().stream()
                .filter(s -> s.getColors().size() > 1)
                .forEach(s -> System.out.println("Multi-color signal: " + s.getName()));
    }

    public static void main(String[] args) throws IOException {
        ColorAssigner app = new ColorAssigner();
        for (String arg : args) {
            app.loadFile(arg);
        }
        app.process();
    }

    private static class CableKey {
        private final String harness;
        private final String name;

        public CableKey(String harness, String name) {
            this.harness = harness;
            this.name = name;
        }

        @Override
        public int hashCode() {
            return (harness + "##||##" + name).hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof CableKey) {
                String s1 = ((CableKey) obj).getHarness() + "##||##" + ((CableKey) obj).getName();
                String s2 = harness + "##||##" + name;
                return s1.equals(s2);
            }
            return false;
        }

        public String getHarness() {
            return harness;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return harness + "/" + name;
        }
    }

    private static class Cable {
        private final CableKey key;
        private int length;
        private final List<Signal> signals = new ArrayList<>();
        private final List<Wire> wires = new ArrayList<>();

        public Cable(CableKey key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return "Length: " + length;
        }

        public int getLength() {
            return length;
        }

        public CableKey getKey() {
            return key;
        }

        public void addWire(Wire wire) {
            wires.add(wire);
        }

        public List<Wire> getWires() {
            return Collections.unmodifiableList(wires);
        }

        public void addSignal(Signal signal, float length) {
            signals.add(signal);
            signal.addCable(this);
            this.length += length;
        }
    }

    private static class Wire {
        private final Signal signal;
        private final Cable cable;
        private final float wireSection;
        private String color;

        public Wire(Signal signal, Cable cable, float wireSection, String color) {
            this.signal = signal;
            this.cable = cable;
            this.wireSection = wireSection;
            this.color = color;
        }

        public float getWireSection() {
            return wireSection;
        }
    }

    private static class Signal {
        private final String name;
        private final Set<String> colors = new HashSet<>();
        private final Map<CableKey, Cable> cables = new HashMap<>();
        private final List<Wire> wires = new ArrayList<>();

        public Signal(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void addCable(Cable cable) {
            cables.put(cable.getKey(), cable);
        }

        public void addColor(String colorKey) {
            colors.add(colorKey);
        }

        public Set<String> getColors() {
            return Collections.unmodifiableSet(colors);
        }

        public int getLength() {
            return cables.values().stream().mapToInt(Cable::getLength).sum();
        }

        public void addWire(Wire wire) {
            wires.add(wire);
        }

        public float getMaxWireSection() {
            return wires.stream()
                    .max(Comparator.comparingDouble(Wire::getWireSection))
                    .map(Wire::getWireSection)
                    .orElse(0.f);
        }

        @Override
        public String toString() {
            return "Signal{" +
                    "name='" + name + '\'' +
                    ", colors=" + colors +
                    ", cables=" + cables +
                    '}';
        }
    }

}
