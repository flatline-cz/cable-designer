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

import biz.flatsw.cabledesigner.generator.TextFormatterBase;
import biz.flatsw.cabledesigner.generator.overview.Overview;
import biz.flatsw.cabledesigner.model.defs.ConnectorGender;

public class TextFormatter extends TextFormatterBase implements BOM.Formatter {
    private final static String PAGE_BOM = "========= BOM ============";
    private String currentPage;
    private String currentConnector;

    @Override
    public void formatMaterialLine(String type, String partNumber, String units, int qty) {
        setPage(PAGE_BOM);
        output.append(fill(type, 15));
        output.append(fill(partNumber, 25));
        output.append(fill(String.valueOf(qty), 10, true));
        output.append(units);
        output.append('\n');
    }

}
