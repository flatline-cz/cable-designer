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

public class ConnectorPinImpl implements ConnectorPinComponent {
    private final PartNumber partNumber;
    private final float crossSectionMin;
    private final float crossSectionMax;
    private final float insulationMin;
    private final float insulationMax;

    public ConnectorPinImpl(PartNumber partNumber,
                            float crossSectionMin, float crossSectionMax,
                            float insulationMin, float insulationMax) {
        this.partNumber = partNumber;
        this.crossSectionMin = crossSectionMin;
        this.crossSectionMax = crossSectionMax;
        this.insulationMin = insulationMin;
        this.insulationMax = insulationMax;
    }

    @Override
    public Type getType() {
        return Type.PIN;
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
    public boolean suits(float wireSection, float insulationDiameter) {
        if (!fitsMin(wireSection, crossSectionMin))
            return false;
        if (!fitsMax(wireSection, crossSectionMax))
            return false;
        if (!fitsMin(insulationDiameter, insulationMin))
            return false;
        if (!fitsMax(insulationDiameter, insulationMax))
            return false;
        return true;
    }

    @Override
    public boolean underMinimum(float crossSection, float insulationDiameter) {
        return !fitsMin(crossSection, crossSectionMin) || !fitsMin(insulationDiameter, insulationMin);
    }

    @Override
    public boolean overMaximum(float crossSection, float insulationDiameter) {
        return !fitsMax(crossSection, crossSectionMax) || !fitsMax(insulationDiameter, insulationMax);
    }

    private boolean fitsMin(float wire, float component) {
        return component == 0 || wire - component > -0.001f;
    }

    private boolean fitsMax(float wire, float component) {
        return component == 0 || wire - component < 0.001f;
    }


}
