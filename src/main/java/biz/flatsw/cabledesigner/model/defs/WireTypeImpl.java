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

import biz.flatsw.cabledesigner.model.SignalSpecification;

public class WireTypeImpl implements WireType {
    private final float wireSection;
    private final float insulationDiameter;

    private final float currentRating;
    private final Color color;
    private final PartNumber partNumber;

    public WireTypeImpl(float wireSection, float insulationDiameter,
                        float currentRating,
                        Color color, PartNumber partNumber) {
        this.wireSection = wireSection;
        this.insulationDiameter = insulationDiameter;
        this.color = color;
        this.partNumber=partNumber;
        this.currentRating=currentRating;
    }

    @Override
    public float getWireSection() {
        return wireSection;
    }

    @Override
    public float getInsulationDiameter() {
        return insulationDiameter;
    }


    @Override
    public boolean hasColor() {
        return color!=null;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public String getColorCodes() {
        return color!=null?color.getCodes():"";
    }

    @Override
    public PartNumber getPartNumber() {
        return partNumber;
    }

    private String getKey() {
        return ""+wireSection+"-"+insulationDiameter+"-"+color.getCodes();
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }

    @Override
    public boolean matches(SignalSpecification signalSpecification) {
        if(currentRating<signalSpecification.getCurrentRating())
            return false;
        return signalSpecification.getColor() == null || signalSpecification.getColor().equals(color);
    }
}
