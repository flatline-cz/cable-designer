/*******************************************************************************
 * Copyright (c) 2022. originally created by flatline.cz
 * This file is a part of CABLE-DESIGNER project.
 * <p>
 *  This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.
 * <p>
 *
 ******************************************************************************/

package biz.flatsw.cabledesigner.model.defs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConnectorPinSectionImpl implements ConnectorPinSection {
    private final List<ConnectorPinComponent> pinTypes=new ArrayList<>();
    private final List<ConnectorPinComponent> pinSeals=new ArrayList<>();
    private PartNumber cavityPlug;


    @Override
    public PartNumber getCavityPlug() {
        return cavityPlug;
    }

    @Override
    public void setCavityPlug(PartNumber cavityPlug) {
        this.cavityPlug = cavityPlug;
    }

    @Override
    public void addPinType(PartNumber pinPartNumber,
                           float crossSectionMin, float crossSectionMax,
                           float insulationMin, float insulationMax) {
        pinTypes.add(new ConnectorPinImpl(
                pinPartNumber,
                crossSectionMin, crossSectionMax,
                insulationMin, insulationMax));
    }

    @Override
    public ConnectorPinComponent findMatchingPin(float wireSection, float insulationDiameter) {
        return pinTypes
                .stream()
                .filter(type -> type.suits(wireSection, insulationDiameter))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void addPinSeal(PartNumber sealPartNumber, float insulationMin, float insulationMax) {
        pinSeals.add(new ConnectorPinSealImpl(
                sealPartNumber,
                insulationMin, insulationMax));
    }

    @Override
    public ConnectorPinComponent findMatchingSeal(float wireSection, float insulationDiameter) {
        return pinSeals
                .stream()
                .filter(type -> type.suits(wireSection, insulationDiameter))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean needPinSeal(WireType wireType) {
        return !pinSeals.isEmpty();
    }

    @Override
    public List<ConnectorPinComponent> listPins() {
        return Collections.unmodifiableList(pinTypes);
    }

    @Override
    public List<ConnectorPinComponent> listPinSeals() {
        return Collections.unmodifiableList(pinSeals);
    }
}
