package biz.flatsw.cabledesigner.model;

import java.util.List;

public interface SignalWiring {

    Signal getSignal();

    String getSignalName();

    WireChain createWireChain(SignalPath signalPath, Pin firstPin);

    List<WireChain> listChains();


}
