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

import biz.flatsw.cabledesigner.generator.TextFormatterBase;
import biz.flatsw.cabledesigner.model.defs.ConnectorGender;

public class TextFormatter extends TextFormatterBase implements Overview.Formatter {

    private final static String PAGE_CONNECTORS = "========= CONNECTORS ============";
    private final static String PAGE_CONNECTOR_SIGNALS = "========= CONNECTOR SIGNALS ============";

    private String currentConnector;



    @Override
    public void formatConnector(String connectorName, String connectorDescription, ConnectorGender gender,
                                int pinCount, String partNumbers) {
        setPage(PAGE_CONNECTORS);
        output.append(fill(connectorName, 10));
        output.append(fill(connectorDescription, 30));
        output.append(fill(gender.name(), 10));
        output.append(fill(String.valueOf(pinCount), 5));
        output.append(partNumbers);
        output.append('\n');
    }

    @Override
    public void formatConnectorSignal(String connectorName, String pinName,
                                      String signalName, String signalDescription,
                                      float crossSection, String colorName) {
        setPage(PAGE_CONNECTOR_SIGNALS);
        if (!connectorName.equals(currentConnector)) {
            if (currentConnector != null)
                output.append('\n');
            currentConnector = connectorName;
        }
        output.append(fill(connectorName, 10));
        output.append(fill(pinName, 10));
        output.append(fill(signalName, 10));
        output.append(fill(signalDescription, 30));
        output.append(fill(String.format("%.2fmm²", crossSection), 10));
        output.append(colorName);
        output.append('\n');

    }

    @Override
    public void formatConnectorSignal(String connectorName, String pinName) {
        setPage(PAGE_CONNECTOR_SIGNALS);
        if (!connectorName.equals(currentConnector)) {
            if (currentConnector != null)
                output.append('\n');
            currentConnector = connectorName;
        }
        output.append(fill(connectorName, 10));
        output.append(fill(pinName, 10));
        output.append(fill("n.c.", 10));
        output.append("--- Not connected ---");
        output.append('\n');
    }

}
