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

package biz.flatsw.cabledesigner.generator.overview;

import biz.flatsw.cabledesigner.Services;
import biz.flatsw.cabledesigner.generator.FormatterPlugin;
import biz.flatsw.cabledesigner.generator.GeneratorBase;
import biz.flatsw.cabledesigner.model.*;
import biz.flatsw.cabledesigner.model.defs.*;

import java.util.List;
import java.util.stream.Collectors;

public class Overview extends GeneratorBase<Overview.Formatter> {

    @Override
    protected Class<Formatter> getFormatterClass() {
        return Formatter.class;
    }

    @Override
    public int getPriority() {
        return 100;
    }



    private void generateConnector(Connector connector) {
        formatter.formatConnector(
                connector.getName(),
                connector.getDescription(),
                connector.getModel().getGender(),
                connector.getModel().getPinCount(),
                connector.getModel()
                        .listComponents()
                        .stream()
                        .filter(this::isConnectorComponent)
                        .map(Component::getPartNumber)
                        .map(PartNumber::getPartNumber)
                        .collect(Collectors.joining(", ")));
    }

    private void generateConnectorSignals(Connector connector) {
        List<Pin> pins = connector.listPins();
        for (int i = 0; i < pins.size(); i++) {
            Pin pin = pins.get(i);
            String pinName = connector.getModel().getPinName(i);
            if (pin == null)
                formatter.formatConnectorSignal(connector.getName(), pinName);
            else
                formatter.formatConnectorSignal(connector.getName(), pinName,
                        pin.getSignalName(), pin.getSignal().getDescription(),
                        pin.getSignal().getWireType().getWireSection(),
                        pin.getSignal().getWireType().getColor().getNames());
        }
    }

    private boolean isConnectorComponent(Component component) {
        return component.getType() == Component.Type.HOUSING || component.getType() == Component.Type.ACCESSORY;
    }




    @Override
    protected void generateContent() {

        Services.getConnectorManager()
                .listConnectors()
                .forEach(this::generateConnector);

        Services.getConnectorManager()
                .listConnectors()
                .forEach(this::generateConnectorSignals);
    }



    public interface Formatter extends FormatterPlugin {

        // connector page
        void formatConnector(String connectorName, String connectorDescription,
                             ConnectorGender gender, int pinCount,
                             String partNumbers);

        // connector signals page
        void formatConnectorSignal(String connectorName, String pinName,
                                   String signalName, String signalDescription,
                                   float crossSection, String colorName);

        void formatConnectorSignal(String connectorName, String pinName);

    }
}
