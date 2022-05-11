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

import biz.flatsw.cabledesigner.model.defs.ConnectorModel;
import biz.flatsw.cabledesigner.parser.Location;
import biz.flatsw.cabledesigner.parser.SourceFileErrorReporter;
import biz.flatsw.cabledesigner.parser.SymbolBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ConnectorImpl extends SymbolBase implements Connector, PathNode {
    // physical layout related
    private Cable cable;
    // connector related
    private Device device;
    private String description;
    private ConnectorModel connectorModel;
    private String connectorVariant;
    // pin related
    private final List<PinName> pinNames = new ArrayList<>();
    private final List<PinImpl> pins = new ArrayList<>();


    public ConnectorImpl(Location definitionLocation,
                         String name, String description,
                         ConnectorModel connectorModel, String variant) {
        super(definitionLocation, name);
        this.description = description;
        this.connectorModel = connectorModel;
        this.connectorVariant = variant;
        List<String> pinNames = connectorModel.getPinNames();
        for (int i = 0; i < pinNames.size(); i++) {
            PinName pinName = new PinName(i, pinNames.get(i));
            this.pinNames.add(pinName);
            this.pins.add(null);
        }
    }

    @Override
    public void attachDevice(Device device, int position) {
        this.device = device;
        this.description = device.getName(position);
    }

    public void setDescription(Location definitionLocation, String description) {
        setDefinitionLocation(definitionLocation);
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public ConnectorModel getModel() {
        return connectorModel;
    }

    @Override
    public Cable getCable() {
        return cable;
    }

    @Override
    public void addCableConnection(Cable cable) {
        this.cable = cable;
    }

    private int findPinByName(Location location, String name) {
        for (int i = 0; i < pinNames.size(); i++)
            if (pinNames.get(i).getName().equals(name))
                return i;
        // FIXME: more reasonable message
        SourceFileErrorReporter.showSymbolReferenceError(location, "pin");
        return 0;
    }

    @Override
    public void attachSignal(Location location, String pinName, Signal signal) {
        // find pin by name
        int position = findPinByName(location, pinName);
        PinImpl pin = pins.get(position);

        // another signal already assigned?
        if (pin!=null && pin.getSignal() != null) {
            if (pin.getSignal().getName().equals(signal.getName()))
                return;
            // FIXME: more reasonable message
            SourceFileErrorReporter.showError(location, "Pin has already assigned signal");
        } else {
            // assign signal
            pin=new PinImpl(this, pinNames.get(position), signal.getName());
            pin.setSignal(signal);
            pins.set(position, pin);
            signal.addTerminal(pin);
        }
    }

    @Override
    public List<Pin> findPinsBySignal(Signal signal) {
        List<Pin> list = new ArrayList<>();
        for (Pin pin : pins) {
            if (pin!=null && pin.getSignal() != null && signal.getName().equals(pin.getSignalName()))
                list.add(pin);
        }
        return list;
    }

    @Override
    public List<Pin> listPins() {
        return Collections.unmodifiableList(pins);
    }

    @Override
    public String toString() {
        return "Connector " + getName();
    }
}
