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
    private final SignalWiring signalWiring;
    private final SignalPath signalPath;
    private final List<WireChainPart> parts=new ArrayList<>();
    private final int sequence;

    public WireChainImpl(SignalWiring signalWiring,
                         SignalPath signalPath,
                         Pin firstPin,
                         int sequence) {
        this.signalWiring = signalWiring;
        this.signalPath=signalPath;
        this.sequence=sequence;
        parts.add(new WireChainPin(this, firstPin));
    }

    @Override
    public String getSignalName() {
        return signalWiring.getSignal().getName();
    }

    @Override
    public SignalPath getSignalPath() {
        return signalPath;
    }

    @Override
    public void addPin(Pin pin) {
        parts.add(new WireChainPin(this, pin));

    }

    @Override
    public void addInterConnectorWire(Connector from, List<Cable> cables) {
        parts.add(new WireChainSegment(this, new InterConnectorWire(from, cables)));

    }

    @Override
    public void addInterPinConnection(Pin pin1, Pin pin2) {
        // TODO: get length
        parts.add(new WireChainSegment(this, new InterPinWire(100)));
    }

    @Override
    public List<WireChainPart> listParts() {
        return Collections.unmodifiableList(parts);
    }

    @Override
    public SignalWiring getSignalWiring() {
        return signalWiring;
    }

    @Override
    public int getSequence() {
        return sequence;
    }

    @Override
    public boolean hasPin(Pin pin) {
        return parts.stream().anyMatch(p -> (p instanceof WireChainPin) && ((WireChainPin) p).getPin()==pin);
    }

    @Override
    public boolean isPinDoubleWired(Pin pin) {
        int i;
        for(i=0;i<parts.size();i++) {
            WireChainPart part=parts.get(i);
            if(part instanceof WireChainPin) {
                if(((WireChainPin) part).getPin()==pin)
                    break;
            }
        }
        if(i==0 || i+1==parts.size() || parts.size()<3)
            return false;
        return true;
    }
}
