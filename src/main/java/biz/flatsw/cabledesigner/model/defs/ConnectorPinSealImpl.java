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

public class ConnectorPinSealImpl implements ConnectorPinComponent{
    private final PartNumber partNumber;
    private final float insulationMin;
    private final float insulationMax;

    public ConnectorPinSealImpl(PartNumber partNumber,
                            float insulationMin, float insulationMax) {
        this.partNumber = partNumber;
        this.insulationMin = insulationMin;
        this.insulationMax = insulationMax;
    }

    @Override
    public Type getType() {
        return Type.PIN_SEAL;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public PartNumber getPartNumber() {
        return partNumber;
    }

    @Override
    public boolean suits(WireType wireType) {
        return !(wireType.getInsulationDiameter() < insulationMin) && !(wireType.getInsulationDiameter() > insulationMax);
    }
}
