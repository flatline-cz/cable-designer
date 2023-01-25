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

import biz.flatsw.cabledesigner.model.Pin;
import biz.flatsw.cabledesigner.parser.Location;

import java.util.List;

public interface ConnectorModel {

    // model information
    ConnectorFamily getFamily();

    ConnectorGender getGender();

    // components
    void addComponent(Component.Type type, PartNumber partNumber, int quantity);

    List<Component> listComponents();

    // pin definitions
    void setDefaultPinNames();
    void setPinNames(String sectionName, List<String> pinNames);

    void addPinType(Location location,
                    String sectionName,
                    PartNumber pinPartNumber,
                    float crossSectionMin, float crossSectionMax,
                    float insulationMin, float insulationMax);

    void addPinSeal(Location location,
                    String sectionName,
                    PartNumber sealPartNumber,
                    float insulationMin, float insulationMax);

    void addCavityPlug(Location location,
                       String sectionName,
                       PartNumber cavityPlugPartNumber);


    // pins query
    String getPinName(int position);

    PartNumber getPinCavityPlug(int position);

    ConnectorPinComponent findSuitablePinType(int position, float wireSection, float insulationDiameter);

    ConnectorPinComponent findSuitablePinSeal(int position, float wireSection, float insulationDiameter);


    void checkWiring(Pin pin, float crossSection, float insulationDiameter);



    boolean needPinSeal(int position, WireType wireType);

    int getPinCount();

    List<String> getPinNames();

}
