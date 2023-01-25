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

package biz.flatsw.cabledesigner.model;

import biz.flatsw.cabledesigner.model.defs.WireType;
import biz.flatsw.cabledesigner.model.defs.WireTypeImpl;
import biz.flatsw.cabledesigner.parser.Location;
import biz.flatsw.cabledesigner.parser.SourceFileErrorReporter;

import java.util.*;

public class SignalManagerImpl implements SignalManager {
    private final Map<String, SignalImpl> signalMap = new TreeMap<>();
    private final Map<String, SignalWiring> signalWirings = new TreeMap<>();

    @Override
    public SignalWiring createSignalWiring(Signal signal) {
        SignalWiring wiring=signalWirings.get(signal.getName());
        if(wiring==null) {
            wiring=new SignalWiringImpl(signal);
            signalWirings.put(signal.getName(), wiring);
        }
        return wiring;
    }

    @Override
    public Collection<SignalWiring> listSignalWirings() {
        return Collections.unmodifiableCollection(signalWirings.values());
    }

    @Override
    public Signal createSignal(Location location,
                               String name, String description) {
        // check redefinition
        SignalImpl signal = signalMap.get(name);
        if (signal != null) {
            SourceFileErrorReporter.showSymbolRedefinitionError(
                    location, signal, "signal");
            return null;
        }

        signal = new SignalImpl(location, name, description);
        signalMap.put(name, signal);
        return signal;
    }

    @Override
    public SignalPath createSignalPath(Location location,
                                       Signal signal,
                                       SignalSpecification signalSpecification,
                                       boolean ordered) {
        SignalPath signalPath=new SignalPathImpl(
                ordered, signalSpecification, signal);
        ((SignalImpl)signal).addSignalPath(signalPath);
        return signalPath;
    }

    @Override
    public Collection<Signal> listSignals() {
        return Collections.unmodifiableCollection(signalMap.values());
    }
}
