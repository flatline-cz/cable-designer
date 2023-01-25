package biz.flatsw.cabledesigner.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SignalWiringImpl implements SignalWiring {
    private final Signal signal;
    private final List<WireChain> wireChains=new ArrayList<>();

    public SignalWiringImpl(Signal signal) {
        this.signal = signal;
    }

    @Override
    public Signal getSignal() {
        return signal;
    }

    @Override
    public String getSignalName() {
        return signal.getName();
    }

    @Override
    public WireChain createWireChain(SignalPath signalPath, Pin firstPin) {
        WireChain chain=new WireChainImpl(this, signalPath, firstPin);
        wireChains.add(chain);
        return chain;
    }

    @Override
    public List<WireChain> listChains() {
        // TODO: make dependency tree
        return Collections.unmodifiableList(wireChains);
    }
}
