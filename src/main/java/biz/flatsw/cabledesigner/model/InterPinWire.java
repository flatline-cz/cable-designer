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

import java.util.Collections;
import java.util.List;

public class InterPinWire implements Wire {
    private final int length;
    private final Signal signal;

    public InterPinWire(int length, Signal signal) {
        this.length = length;
        this.signal = signal;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public Signal getSignal() {
        return signal;
    }

    @Override
    public List<PathNode> getPath() {
        return Collections.emptyList();
    }
}
