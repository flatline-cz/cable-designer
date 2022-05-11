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

package biz.flatsw.cabledesigner.model.defs;

public class ComponentImpl implements Component {
    private final PartNumber partNumber;
    private final Type type;
    private int count;
    private final String key;

    public ComponentImpl(PartNumber partNumber, Type type, int count) {
        this.partNumber = partNumber;
        this.type = type;
        this.count = count;
        key=partNumber+"#||#"+type.name();
    }

    @Override
    public PartNumber getPartNumber() {
        return partNumber;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public int getCount() {
        return count;
    }

    public String getKey() {
        return key;
    }
}
