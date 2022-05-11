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

package biz.flatsw.cabledesigner.parser;

import java.util.ArrayList;
import java.util.List;

public abstract class SymbolBase implements Symbol {
    private final String name;
    private Location definitionLocation;
    private final List<Location> referenceLocations=new ArrayList<>();

    public SymbolBase(Location definitionLocation, String name) {
        this.definitionLocation = definitionLocation;
        this.name=name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setDefinitionLocation(Location definitionLocation) {
        this.definitionLocation = definitionLocation;
    }

    public Location getDefinitionLocation() {
        return definitionLocation;
    }

    public void addReference(Location location) {
        referenceLocations.add(location);
    }

    public List<Location> getReferenceLocations() {
        return referenceLocations;
    }
}
