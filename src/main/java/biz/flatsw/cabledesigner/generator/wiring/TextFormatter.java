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

import biz.flatsw.cabledesigner.generator.TextFormatterBase;

public class TextFormatter extends TextFormatterBase implements Wiring.Formatter {

    @Override
    public void formatSignal(String signalName) {
        output.append("\n=============\n\n");
        output.append(String.format("Wiring for signal '%s'\n", signalName));
    }

    @Override
    public void formatPin(String connectorName, String pinName, String pinPartNumber, String sealPartNumber) {
        output.append(fill("  PIN", 8));
        output.append(fill(pinPartNumber, 20));
        output.append(fill((sealPartNumber!=null)?("seal:"+sealPartNumber):"", 25));
        output.append(fill("", 30));
        output.append(connectorName);
        output.append("/");
        output.append(pinName);
        output.append("\n");
    }

    @Override
    public void formatWire(int length, String colorCode, String partNumber, float crossSection, String pathNodes) {
        output.append(fill("  WIRE", 8));
        output.append(fill(partNumber, 20));
        output.append(fill("", 25));
        output.append(fill(colorCode, 10));
        output.append(fill(""+length+"mm", 10));
        if(pathNodes==null || pathNodes.isEmpty()) {
            output.append(String.format("%.2fmm²", crossSection));
        } else {
            output.append(fill(String.format("%.2fmm²", crossSection), 10));
            output.append(pathNodes);
        }
        output.append("\n");
    }

    @Override
    public void formatConnector(String name) {

        output.append("\n=============\n\n");
        output.append(String.format("Connector '%s'\n", name));
    }

    @Override
    public void formatConnectorComponent(String type, String partNumber, int qty) {
        output.append(fill("  "+type, 15));
        output.append(fill(partNumber, 25));
        output.append(qty);
        output.append("pcs\n");
    }

    @Override
    public void formatConnectorCavity(String partNumber, String pinName) {
        output.append(fill("  cav.plug", 15));
        output.append(fill(partNumber, 25));
        output.append(fill("1pcs", 10));
        output.append("on pin ");
        output.append(pinName);
        output.append('\n');
    }

}
