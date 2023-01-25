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

package biz.flatsw.cabledesigner.generator.wiring;

import biz.flatsw.cabledesigner.Services;
import biz.flatsw.cabledesigner.generator.FormatterPlugin;
import biz.flatsw.cabledesigner.generator.GeneratorBase;
import biz.flatsw.cabledesigner.model.*;
import biz.flatsw.cabledesigner.model.defs.Component;
import biz.flatsw.cabledesigner.model.defs.ConnectorModel;
import biz.flatsw.cabledesigner.model.defs.PartNumber;
import biz.flatsw.cabledesigner.model.defs.WireType;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class Wiring extends GeneratorBase<Wiring.Formatter> {

    @Override
    protected Class<Formatter> getFormatterClass() {
        return Formatter.class;
    }

    @Override
    public int getPriority() {
        return 200;
    }

    private void generatePin(WireChainPin wireChainPin) {
        Pin pin = wireChainPin.getPin();
        String pinPartNumber = pin.getPinType() != null
                ? pin.getPinType().getPartNumber().getPartNumber()
                : "???";
        String sealPartNumber = pin.getSealType() != null
                ? pin.getSealType().getPartNumber().getPartNumber()
                : null;
        formatter.formatPin(
                pin.getConnector().getName(),
                pin.getName(),
                pinPartNumber,
                sealPartNumber,
                null);  // TODO: rework
    }

    private void generateWireSegment(WireChainSegment wireChainSegment) {
        Wire wire = wireChainSegment.getWire();
        WireType wireType=wireChainSegment.getWireChain().getSignalPath().getWireType();

        formatter.formatWire(
                wire.getLength(),
                wireType.getColor().getCodes(),
                wireType.getPartNumber().getPartNumber(),
                wireType.getWireSection(),
                wire.getPath()
                        .stream()
                        .map(PathNode::getName)
                        .collect(Collectors.joining(" ")));
    }

    private void generateWiring(SignalWiring signalWiring) {
        formatter.formatSignal(signalWiring.getSignalName());

        for(WireChain wireChain : signalWiring.listChains()) {
            for (WireChainPart chainPart : wireChain.listParts()) {
                if (chainPart instanceof WireChainPin) {
                    generatePin((WireChainPin) chainPart);
                    continue;
                }
                if (chainPart instanceof WireChainSegment) {
                    generateWireSegment((WireChainSegment) chainPart);
                    continue;
                }
            }
        }
    }


    private void generateConnectors(Connector connector) {
        formatter.formatConnector(connector.getName());
        ConnectorModel model = connector.getModel();
        model.listComponents()
                .stream().filter(component -> !Component.Type.CAVITY_PLUG.equals(component.getType()))
                .forEach(component -> formatter.formatConnectorComponent(
                        component.getType().name().toLowerCase(),
                        component.getPartNumber().getPartNumber(),
                        component.getCount()));
        List<Pin> pins = connector.listPins();
        for (int i = 0; i < pins.size(); i++) {
            Pin pin = pins.get(i);
            if (pin != null)
                continue;
            String pinName = model.getPinName(i);
            PartNumber partNumber = model.getPinCavityPlug(i);
            if (partNumber != null)
                formatter.formatConnectorCavity(
                        partNumber.getPartNumber(),
                        pinName);
        }

    }


    @Override
    protected void generateContent() {
        Services.getSignalManager()
                .listSignalWirings()
                .forEach(this::generateWiring);

        Services.getConnectorManager()
                .listConnectors()
                .forEach(this::generateConnectors);
    }

    public interface Formatter extends FormatterPlugin {

        // wires
        void formatSignal(String signalName);

        void formatPin(String connectorName, String pinName, String pinPartNumber,
                       String sealPartNumber, String wireJoint);

        void formatWire(int length, String colorCode, String partNumber, float crossSection, String pathNodes);

        // connectors
        void formatConnector(String name);

        void formatConnectorComponent(String type, String partNumber, int qty);

        void formatConnectorCavity(String partNumber, String pinName);

        InputStream getOutput();
    }
}
