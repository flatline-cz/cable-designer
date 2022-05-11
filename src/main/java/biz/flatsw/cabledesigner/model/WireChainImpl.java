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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WireChainImpl implements WireChain {
    private final Signal signal;
    private final List<WireChainPart> parts=new ArrayList<>();

    public WireChainImpl(Signal signal, Pin firstPin) {
        this.signal = signal;
        parts.add(new WireChainPin(this, firstPin));
    }

    @Override
    public String getSignalName() {
        return signal.getName();
    }

    @Override
    public void addPin(Pin pin) {
        parts.add(new WireChainPin(this, pin));

    }

    @Override
    public void addInterConnectorWire(Connector from, List<Cable> cables) {
        parts.add(new WireChainSegment(this, new InterConnectorWire(from, cables, signal)));

    }

    @Override
    public void addInterPinConnection(Pin pin1, Pin pin2) {
        // TODO: get length
        parts.add(new WireChainSegment(this, new InterPinWire(100, signal)));
    }

    @Override
    public List<WireChainPart> listParts() {
        return Collections.unmodifiableList(parts);
    }
}
