package biz.flatsw.cabledesigner.generator.cables;

import biz.flatsw.cabledesigner.Services;
import biz.flatsw.cabledesigner.generator.FormatterPlugin;
import biz.flatsw.cabledesigner.generator.GeneratorBase;
import biz.flatsw.cabledesigner.model.*;
import biz.flatsw.cabledesigner.model.defs.Color;

public class Cables extends GeneratorBase<Cables.Formatter> {

    @Override
    protected void generateContent() {
        Services.getSignalManager().listSignalWirings().forEach(this::generateSignalWiring);
    }

    private void generateSignalWiring(SignalWiring signalWiring) {
        Services.getCableManager().listCables().forEach(cable -> {
            for (WireChain wireChain : cable.getWireChains())
                formatter.addCable(cable.getName(), wireChain.getSignalName(),
                        wireChain.getSignalPath().getWireType().getWireSection(),
                        cable.getLength(),
                        wireChain.getSignalPath().getSpecification().getColor());
        });
    }


    @Override
    protected Class<Formatter> getFormatterClass() {
        return Formatter.class;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    public interface Formatter extends FormatterPlugin {
        void addCable(String cableName, String signalName, float wireSection, int length, Color color);

    }
}
