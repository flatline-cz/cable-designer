package biz.flatsw.cabledesigner.model;

import biz.flatsw.cabledesigner.model.defs.WireType;

import java.util.Collection;

public interface SignalPath {

    boolean isOrdered();

    void addTerminal(Pin terminal);

    Collection<Pin> listTerminals();

    void setWireType(WireType type);
    WireType getWireType();

    SignalSpecification getSpecification();

    Signal getSignal();
}
