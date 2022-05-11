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

package biz.flatsw.cabledesigner.generator.signals;

import biz.flatsw.cabledesigner.generator.PDFTableFormatter;


public class PDFFormatter extends PDFTableFormatter implements Signals.Formatter {
    private boolean first=true;

    @Override
    protected LayoutStyle getLayoutStyle() {
        return LayoutStyle.LANDSCAPE;
    }

    @Override
    protected String getName() {
        return "List of signals";
    }

    @Override
    public void formatSignal(String signalName, String signalDescription,
                             float crossSection, String colorName, String connectors) {
        if(first) {
            addTable(0.1f, 0.4f, 0.2f, 0.3f);
//            setTitle("Signals");
            addHeaderRow();
            addCell("Signal");
            addCell("Description");
//            addCell("Cross-section");
            addCell("Color");
            addCell("Connectors");
            first=false;
        }
        addRow();
        addCell(signalName);
        addCell(signalDescription, LEFT);
//        addCell(String.format("%.2fmm²", crossSection));
        addCell(colorName);
        addCell(connectors.replace(",", ""), LEFT);
    }

}
