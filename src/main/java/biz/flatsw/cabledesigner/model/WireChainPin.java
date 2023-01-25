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

import java.util.Comparator;
import java.util.List;

public class WireChainPin implements WireChainPart {
    private final WireChain chain;
    private final Pin pin;

    public WireChainPin(WireChain chain, Pin pin) {
        this.chain = chain;
        this.pin = pin;
    }

    @Override
    public WireChain getWireChain() {
        return chain;
    }

    public Pin getPin() {
        return pin;
    }

    public WireJointInfo getWireJointInfo() {
        List<WireChain> chains=chain.getSignalWiring().listChainsByPin(pin);
        if(chains.size()<2)
            return null;
        chains.sort(Comparator.comparing(WireChain::getSequence));
        int idx=chains.indexOf(chain)+1;
        return new WireJointInfo(idx, chains.size());
    }


    public static class WireJointInfo {
        private final int sequence;
        private final int count;

        public WireJointInfo(int sequence, int count) {
            this.sequence = sequence;
            this.count = count;
        }

        public int getSequence() {
            return sequence;
        }

        public int getCount() {
            return count;
        }
    }

}
