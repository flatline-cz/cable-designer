package biz.flatsw.cabledesigner.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SignalWiringImpl implements SignalWiring {
    private final Signal signal;
    private final List<WireChain> wireChains=new ArrayList<>();
    private int sequence=1;

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
        WireChain chain=new WireChainImpl(this, signalPath, firstPin, sequence++);
        ((SignalPathImpl)signalPath).setWireChain(chain);
        wireChains.add(chain);
        return chain;
    }

    @Override
    public List<WireChain> listChains() {
        // TODO: make dependency tree
        return Collections.unmodifiableList(wireChains);
    }

    @Override
    public List<WireChain> listChainsByPin(Pin pin) {
        return wireChains.stream()
                .filter(wireChain -> wireChain.hasPin(pin))
                .collect(Collectors.toList());
    }
}
