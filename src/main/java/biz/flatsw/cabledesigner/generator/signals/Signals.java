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

import biz.flatsw.cabledesigner.Services;
import biz.flatsw.cabledesigner.generator.FormatterPlugin;
import biz.flatsw.cabledesigner.generator.GeneratorBase;
import biz.flatsw.cabledesigner.model.PathNode;
import biz.flatsw.cabledesigner.model.Signal;
import biz.flatsw.cabledesigner.model.SignalPath;

import java.util.stream.Collectors;

public class Signals extends GeneratorBase<Signals.Formatter> {


    @Override
    protected void generateContent() {
        Services.getSignalManager()
                .listSignals()
                .forEach(this::generateSignal);
    }

    @Override
    protected Class<Formatter> getFormatterClass() {
        return Formatter.class;
    }

    @Override
    public int getPriority() {
        return 1000;
    }

    private void generateSignal(Signal signal) {
        signal.getSignalPaths().forEach(this::generateSignal);
    }

    private void generateSignal(SignalPath signalPath) {
        formatter.formatSignal(
                signalPath.getSignal().getName(),
                signalPath.getSignal().getDescription(),
                signalPath.getWireType().getWireSection(),
                signalPath.getWireType().getColor().getNames(),
                Services.getConnectorManager()
                        .listConnectors()
                        .stream()
                        .filter(c -> !c.findPinsBySignalPath(signalPath).isEmpty())
                        .map(PathNode::getName)
                        .collect(Collectors.joining(", ")));
    }

    public interface Formatter extends FormatterPlugin {

        // signals page
        void formatSignal(String signalName, String signalDescription, float crossSection, String colorName,
                          String connectors);

    }
}
