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

package biz.flatsw.cabledesigner.generator.bom;

import biz.flatsw.cabledesigner.generator.PDFTableFormatter;

public class PDFFormatter extends PDFTableFormatter implements BOM.Formatter {
    private boolean first=true;

    @Override
    protected PDFTableFormatter.LayoutStyle getLayoutStyle() {
        return LayoutStyle.PORTRAIT;
    }

    @Override
    protected String getName() {
        return "BOM";
    }

    @Override
    public void formatMaterialLine(String type, String partNumber,
                                   String units, int qty) {
        if(first) {
            addTable(0.2f, 0.5f, 0.15f, 0.15f);
            setTitle("BOM");
            addHeaderRow();
            addCell("Type");
            addCell("Part number");
            addCell("Quantity");
            addCell("Units");
            first=false;
        }
        addRow();
        addCell(type);
        addCell(partNumber, LEFT);
        addCell(String.valueOf(qty), RIGHT);
        addCell(units);
    }



}
