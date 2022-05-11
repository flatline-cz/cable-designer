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

import biz.flatsw.cabledesigner.generator.TextFormatterBase;

public class TextFormatter extends TextFormatterBase implements Signals.Formatter {
    private final static String PAGE_SIGNALS = "========= SIGNALS ============";

    @Override
    public void formatSignal(String signalName, String signalDescription, float crossSection, String colorName, String connectors) {
        setPage(PAGE_SIGNALS);
        output.append(fill(signalName, 15));
        output.append(fill(signalDescription, 30));
        output.append(fill(String.format("%.2fmm²", crossSection), 10));
        output.append(fill(colorName, 20));
        output.append(connectors);
        output.append('\n');
    }
}
