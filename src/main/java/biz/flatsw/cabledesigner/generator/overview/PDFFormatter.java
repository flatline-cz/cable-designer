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

import biz.flatsw.cabledesigner.generator.PDFTableFormatter;
import biz.flatsw.cabledesigner.model.defs.ConnectorGender;

public class PDFFormatter extends PDFTableFormatter implements Overview.Formatter {
    private Section section;
    private String currentConnector;

    @Override
    protected LayoutStyle getLayoutStyle() {
        return LayoutStyle.LANDSCAPE;
    }

    private void setSection(Section section) {
        if (section == this.section)
            return;
        this.section = section;
        if (section == Section.CONNECTORS) {
            addTable(0.1f, 0.4f, 0.1f, 0.1f, 0.3f);
            setTitle("Connectors");
            addHeaderRow();
            addCell("Code");
            addCell("Description");
            addCell("Gender");
            addCell("Pin count");
            addCell("Part numbers");
        }
    }

    @Override
    protected String getName() {
        return "Harness overview";
    }

    @Override
    public void formatConnector(
            String connectorName, String connectorDescription,
            ConnectorGender gender, int pinCount, String partNumbers) {
        setSection(Section.CONNECTORS);
        addRow();
        addCell(connectorName);
        addCell(connectorDescription);
        addCell(gender.name().toLowerCase());
        addCell(String.valueOf(pinCount), RIGHT);
        addCell(partNumbers);

    }

    @Override
    public void formatConnectorSignal(String connectorName, String pinName, String signalName, String signalDescription,
                                      String wireDescription) {
        setSection(Section.CONNECTOR_SIGNALS);
        if (!connectorName.equals(currentConnector)) {
            addTable(0.1f, 0.1f, 0.1f, 0.4f, 0.3f);
            setTitle("Signals on "+connectorName, currentConnector == null);
            addHeaderRow();
            addCell("Connector");
            addCell("Pin");
            addCell("Signal");
            addCell("Description");
            addCell("Wire");
            currentConnector = connectorName;
        }
        addRow();
        addCell(connectorName);
        addCell(pinName);
        addCell(signalName);
        addCell(signalDescription, LEFT);
//        addCell(String.format("%.2fmm²", crossSection));
        addCell(wireDescription);
    }

    @Override
    public void formatConnectorSignal(String connectorName, String pinName) {
        setSection(Section.CONNECTOR_SIGNALS);
    }

    enum Section {
        CONNECTORS, CONNECTOR_SIGNALS
    }
}
