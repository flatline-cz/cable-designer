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

import biz.flatsw.cabledesigner.generator.PDFTableFormatter;

public class PDFFormatter extends PDFTableFormatter implements Wiring.Formatter {
    private String currentTitle;

    @Override
    protected String getName() {
        return "Production";
    }

    @Override
    protected LayoutStyle getLayoutStyle() {
        return LayoutStyle.LANDSCAPE;
    }

    @Override
    public void formatSignal(String signalName) {
        boolean first = currentTitle == null;
        currentTitle = String.format("'%s' wiring SOP", signalName);
        addTable(0.08f, 0.18f, 0.18f, 0.08f, 0.08f, 0.1f, 0.3f);
        setTitle(currentTitle, first);
        addHeaderRow();
        addCell("Type");
        addCell("Pin/Wire PN");
        addCell("Seal PN");
        addCell("Length");
        addCell("Wire");
        addCell("Color");
        addCell("Notes");
    }

    @Override
    public void formatPin(String connectorName, String pinName,
                          String pinPartNumber, String sealPartNumber,
                          String wireJoint) {
        addRow();
        addCell("pin");
        addCell(pinPartNumber, LEFT);
        addCell((sealPartNumber != null) ? ("seal:" + sealPartNumber) : "", LEFT);
        addCell("");
        addCell("");
        addCell("");
        String note = String.format("Inserted into %s/%s", connectorName, pinName);
        if (wireJoint != null)
            note += ". " + wireJoint;
        addCell(note, LEFT);
    }

    @Override
    public void formatWire(int length, String colorCode, String partNumber, float crossSection, String pathNodes) {
        addRow();
        addCell("wire");
        addCell(partNumber, LEFT);
        addCell("");
        addCell(String.format("%dmm", length), RIGHT);
        addCell(String.format("%.2fmm²", crossSection), RIGHT, false);
        addCell(colorCode);
        addCell((pathNodes != null && !pathNodes.isEmpty())
                ? String.format(String.format("Through %s", pathNodes))
                : "", LEFT);
    }

    @Override
    public void formatConnector(String name) {
        currentTitle = String.format("'%s' SOP", name);
        addTable(0.25f, 0.25f, 0.25f, 0.25f);
        setTitle(currentTitle, false);
        addHeaderRow();
        addCell("Component");
        addCell("Part number");
        addCell("Quantity");
        addCell("Note");
    }

    @Override
    public void formatConnectorComponent(String type, String partNumber, int qty) {
        addRow();
        addCell(type);
        addCell(partNumber, LEFT);
        addCell(String.valueOf(qty), RIGHT);
        addCell("");
    }

    @Override
    public void formatConnectorCavity(String partNumber, String pinName) {
        addRow();
        addCell("cavity plug");
        addCell(partNumber, LEFT);
        addCell("1", RIGHT);
        addCell(String.format("at position %s", pinName));
    }

}
