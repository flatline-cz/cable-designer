/*******************************************************************************
 * Copyright (c) 2022. originally created by flatline.cz
 * This file is a part of CABLE-DESIGNER project.
 *
 *  This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.
 *
 *
 ******************************************************************************/

package biz.flatsw.cabledesigner.model;


import biz.flatsw.cabledesigner.model.defs.ConnectorPinComponent;

public class PinImpl implements Pin {
    private final Connector connector;
    private final PinName name;
    private final String signalName;
    private Signal signal;
    private ConnectorPinComponent pinType;
    private ConnectorPinComponent sealType;

    public PinImpl(Connector connector, PinName name, String signalName) {
        this.connector = connector;
        this.name = name;
        this.signalName = signalName;
    }

    @Override
    public Connector getConnector() {
        return connector;
    }

    @Override
    public int getPosition() {
        return name.getPosition();
    }

    @Override
    public String getName() {
        return name.getName();
    }

    @Override
    public String getSignalName() {
        return signal != null ? signal.getName() : signalName;
    }

    @Override
    public Signal getSignal() {
        return signal;
    }

    public void setSignal(Signal signal) {
        this.signal = signal;
    }

    @Override
    public ConnectorPinComponent getPinType() {
        return pinType;
    }

    @Override
    public void setPinType(ConnectorPinComponent pinType) {
        this.pinType = pinType;
    }

    @Override
    public ConnectorPinComponent getSealType() {
        return sealType;
    }

    @Override
    public void setSealType(ConnectorPinComponent sealType) {
        this.sealType = sealType;
    }
}
