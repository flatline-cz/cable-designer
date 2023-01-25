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

import biz.flatsw.cabledesigner.model.OverMaximumException;
import biz.flatsw.cabledesigner.model.Pin;
import biz.flatsw.cabledesigner.model.UnderMinimumException;
import biz.flatsw.cabledesigner.parser.Location;
import biz.flatsw.cabledesigner.parser.SourceFileErrorReporter;
import biz.flatsw.cabledesigner.parser.SymbolBase;

import java.util.*;
import java.util.stream.Collectors;

public class ConnectorModelImpl extends SymbolBase implements ConnectorModel {
    private final ConnectorFamily family;
    private final ConnectorGender gender;
    private final int pinCount;
    // pin numbering
    private final List<String> pinNames;
    private final Map<String, String> pinSections = new HashMap<>();
    // components
    private final List<Component> components = new ArrayList<>();
    // pin sections
    private final Map<String, ConnectorPinSection> pinSectionMap = new HashMap<>();
    private final ConnectorPinSection defaultSection;

    public ConnectorModelImpl(Location location, ConnectorFamily family,
                              ConnectorGender gender, int pinCount) {
        super(location, family.getName()); // FIXME: build real name
        this.family = family;
        this.gender = gender;
        this.pinCount = pinCount;
        this.defaultSection = new ConnectorPinSectionImpl();
        this.pinNames = new ArrayList<>(pinCount);
    }

    @Override
    public List<Component> listComponents() {
        return Collections.unmodifiableList(components);
    }

    private ConnectorPinSection getSection(int position) {
        String pinName = pinNames.get(position);
        String sectionName = pinSections.get(pinName);
        return sectionName != null ? pinSectionMap.get(sectionName) : defaultSection;
    }

    @Override
    public void setDefaultPinNames() {
        for (int i = 0; i < pinCount; i++)
            this.pinNames.add(String.valueOf(i + 1));
    }

    @Override
    public void setPinNames(String sectionName, List<String> pinNames) {
        if (sectionName != null)
            pinSectionMap.computeIfAbsent(sectionName, n -> new ConnectorPinSectionImpl());
        this.pinNames.addAll(pinNames);
        pinNames.forEach(name -> pinSections.put(name, sectionName));
    }

    @Override
    public void addComponent(Component.Type type, PartNumber partNumber, int quantity) {
        Component component = new ComponentImpl(partNumber, type, quantity);
        components.add(component);
        if (Component.Type.CAVITY_PLUG.equals(type))
            defaultSection.setCavityPlug(partNumber);
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
        return getSection(position).getCavityPlug();
    }

    @Override
    public List<String> getPinNames() {
        return Collections.unmodifiableList(pinNames);
    }

    public void addCavityPlug(Location location,
                              String sectionName,
                              PartNumber cavityPlugPartNumber) {
        ConnectorPinSection section;
        if (sectionName == null) {
            section = defaultSection;
        } else {
            section = pinSectionMap.get(sectionName);
            if (section == null) {
                SourceFileErrorReporter.showError(location, "Section not defined");
                return;
            }
        }
        section.setCavityPlug(cavityPlugPartNumber);
    }

    @Override
    public void addPinType(Location location,
                           String sectionName,
                           PartNumber pinPartNumber,
                           float crossSectionMin, float crossSectionMax,
                           float insulationMin, float insulationMax) {
        ConnectorPinSection section;
        if (sectionName == null) {
            section = defaultSection;
        } else {
            section = pinSectionMap.get(sectionName);
            if (section == null) {
                SourceFileErrorReporter.showError(location, "Section not defined");
                return;
            }
        }
        section.addPinType(
                pinPartNumber,
                crossSectionMin, crossSectionMax,
                insulationMin, insulationMax);
    }

    @Override
    public ConnectorPinComponent findSuitablePinType(int position, float wireSection, float insulationDiameter) {
        return getSection(position).findMatchingPin(wireSection, insulationDiameter);
    }

    @Override
    public void addPinSeal(Location location,
                           String sectionName,
                           PartNumber sealPartNumber,
                           float insulationMin, float insulationMax) {
        ConnectorPinSection section;
        if (sectionName == null) {
            section = defaultSection;
        } else {
            section = pinSectionMap.get(sectionName);
            if (section == null) {
                SourceFileErrorReporter.showError(location, "Section not defined");
                return;
            }
        }
        section.addPinSeal(
                sealPartNumber,
                insulationMin, insulationMax);
    }

    @Override
    public ConnectorPinComponent findSuitablePinSeal(int position, float wireSection, float insulationDiameter) {
        return getSection(position).findMatchingSeal(wireSection, insulationDiameter);
    }

    @Override
    public void checkWiring(Pin pin, float crossSection, float insulationDiameter) {
        ConnectorPinSection section=getSection(pin.getPosition());

        // wire is over maximum of all pins or seals?
        boolean allOver=true;
        for(ConnectorPinComponent pinComponent : section.listPins()) {
            if(!pinComponent.overMaximum(crossSection, insulationDiameter)) {
                allOver=false;
                break;
            }
        }
        for(ConnectorPinComponent pinComponent : section.listPinSeals()) {
            if(!pinComponent.overMaximum(crossSection, insulationDiameter)) {
                allOver=false;
                break;
            }
        }
        if(allOver)
            throw new OverMaximumException(pin, crossSection, insulationDiameter);

        // wire is under minimum of all pins and seals?
        boolean allUnder=true;
        for(ConnectorPinComponent pinComponent : section.listPins()) {
            if(!pinComponent.underMinimum(crossSection, insulationDiameter)) {
                allUnder=false;
                break;
            }
        }
        for(ConnectorPinComponent pinComponent : section.listPinSeals()) {
            if(!pinComponent.underMinimum(crossSection, insulationDiameter)) {
                allUnder=false;
                break;
            }
        }
        if(allUnder)
            throw new UnderMinimumException(pin, crossSection, insulationDiameter);
    }

    @Override
    public boolean needPinSeal(int position, WireType wireType) {
        return getSection(position).needPinSeal(wireType);
    }
}
