package biz.flatsw.cabledesigner.model;

import biz.flatsw.cabledesigner.model.defs.WireType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SignalPathImpl implements SignalPath {
    private final boolean ordered;
    private final SignalSpecification signalSpecification;
    private final Signal signal;
    private WireChain wireChain;
    private WireType wireType;
    private final List<Pin> terminals=new ArrayList<>();

    public SignalPathImpl(boolean ordered,
                          SignalSpecification signalSpecification,
                          Signal signal) {
        this.ordered = ordered;
        this.signalSpecification = signalSpecification;
        this.signal=signal;
    }

    @Override
    public boolean isOrdered() {
        return ordered;
    }

    @Override
    public void addTerminal(Pin terminal) {
        terminals.add(terminal);
    }

    @Override
    public Collection<Pin> listTerminals() {
        return Collections.unmodifiableList(terminals);
    }

    @Override
    public void setWireType(WireType type) {
        wireType=type;
    }

    @Override
    public WireType getWireType() {
        return wireType;
    }

    @Override
    public SignalSpecification getSpecification() {
        return signalSpecification;
    }

    @Override
    public Signal getSignal() {
        return signal;
    }

    @Override
    public WireChain getWireChain() {
        return wireChain;
    }

    public void setWireChain(WireChain wireChain) {
        this.wireChain = wireChain;
    }
}
