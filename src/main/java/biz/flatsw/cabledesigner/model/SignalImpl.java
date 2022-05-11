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

import biz.flatsw.cabledesigner.Services;
import biz.flatsw.cabledesigner.model.defs.WireType;
import biz.flatsw.cabledesigner.parser.CompilerFailure;
import biz.flatsw.cabledesigner.parser.Location;
import biz.flatsw.cabledesigner.parser.SourceFileErrorReporter;
import biz.flatsw.cabledesigner.parser.SymbolBase;

import java.util.*;

public class SignalImpl extends SymbolBase implements Signal {
    private final String description;
    private final boolean net;

    private final SignalSpecification specification;
    private WireType wireType;
    private final Set<Pin> terminals = new HashSet<>();


    public SignalImpl(Location definitionLocation,
                      String name,
                      String description,
                      boolean net,
                      SignalSpecification specification) {
        super(definitionLocation, name);
        this.description=description;
        this.net = net;
        this.specification = specification;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public WireType getWireType() {
        return wireType;
    }

    @Override
    public void setWireType(WireType wireType) {
        this.wireType = wireType;
    }

    @Override
    public SignalSpecification getSpecification() {
        return specification;
    }

    @Override
    public boolean isNet() {
        return net;
    }

    @Override
    public void addTerminal(Pin terminal) {
        terminals.add(terminal);
    }

    @Override
    public Collection<Pin> listTerminals() {
        return Collections.unmodifiableCollection(terminals);
    }
}
