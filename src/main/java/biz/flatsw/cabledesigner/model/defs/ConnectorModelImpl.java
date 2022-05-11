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

import biz.flatsw.cabledesigner.parser.Location;
import biz.flatsw.cabledesigner.parser.SymbolBase;

import java.util.*;
import java.util.stream.Collectors;

public class ConnectorModelImpl extends SymbolBase implements ConnectorModel {
    private final ConnectorFamily family;
    private final ConnectorGender gender;
    private final int pinCount;
    // pin numbering
    private final List<String> pinNames;
    // components
    private final List<Component> components = new ArrayList<>();
    private Component cavityPlug;
    // pin sections
    private final Map<String, ConnectorPinSection> pinSectionMap=new HashMap<>();
    private final ConnectorPinSection defaultSection;

    public ConnectorModelImpl(Location location, ConnectorFamily family,
                              ConnectorGender gender, int pinCount) {
        super(location, family.getName()); // FIXME: build real name
        this.family = family;
        this.gender = gender;
        this.pinCount = pinCount;
        this.defaultSection=new ConnectorPinSectionImpl();
        this.pinNames = new ArrayList<>(pinCount);
        for (int i = 0; i < pinCount; i++)
            this.pinNames.add(String.valueOf(i + 1));
    }

    @Override
    public List<Component> listComponents() {
        return Collections.unmodifiableList(components);
    }

    @Override
    public void setPinNames(List<String> pinNames) {
        if(pinNames.size()!=pinCount)
            return;
        this.pinNames.clear();
        this.pinNames.addAll(pinNames);
    }

    @Override
    public void addComponent(Component.Type type, PartNumber partNumber, int quantity) {
        Component component = new ComponentImpl(partNumber, type, quantity);
        components.add(component);
        if (Component.Type.CAVITY_PLUG.equals(type))
            cavityPlug = component;
    }

    @Override
    public ConnectorFamily getFamily() {
        return family;
    }

    @Override
    public ConnectorGender getGender() {
        return gender;
    }

    @Override
    public int getPinCount() {
        return pinCount;
    }

    @Override
    public String getPinName(int position) {
        return (position < pinNames.size()) ? pinNames.get(position) : null;
    }

    @Override
    public PartNumber getPinCavityPlug(int position) {
        return cavityPlug != null ? cavityPlug.getPartNumber() : null;
    }

    @Override
    public List<String> getPinNames() {
        return Collections.unmodifiableList(pinNames);
    }

    @Override
    public void addPinType(PartNumber pinPartNumber,
                           float crossSectionMin, float crossSectionMax,
                           float insulationMin, float insulationMax) {
        defaultSection.addPinType(
                pinPartNumber,
                crossSectionMin, crossSectionMax,
                insulationMin, insulationMax);
    }

    @Override
    public ConnectorPinComponent findSuitablePinType(int position, WireType wireType) {
        return defaultSection.findMatchingPin(wireType);
    }

    @Override
    public void addPinSeal(PartNumber sealPartNumber, float insulationMin, float insulationMax) {
        defaultSection.addPinSeal(
                sealPartNumber,
                insulationMin, insulationMax);
    }

    @Override
    public ConnectorPinComponent findSuitablePinSeal(int position, WireType wireType) {
        return defaultSection.findMatchingSeal(wireType);
    }

    @Override
    public boolean needPinSeal(int position, WireType wireType) {
        return defaultSection.needPinSeal(wireType);
    }
}
