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
import biz.flatsw.cabledesigner.parser.SourceFileErrorReporter;

import java.util.*;

public class ConnectorFamilyImpl implements ConnectorFamily {
    private final String name;
    private final Map<String, ConnectorModelImpl> connectorModelMap = new TreeMap<>();

    public ConnectorFamilyImpl(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ConnectorModel createModel(Location location, int pins, ConnectorGender gender) {
        String key = buildKey(pins, gender);
        if (connectorModelMap.containsKey(key)) {
            SourceFileErrorReporter.showSymbolRedefinitionError(
                    location, connectorModelMap.get(key),
                    "connector type");
            return null;
        }
        ConnectorModelImpl model = new ConnectorModelImpl(location, this, gender, pins);
        connectorModelMap.put(key, model);
        return model;
    }

    @Override
    public ConnectorModel getModel(Location location, int pins, ConnectorGender gender, String variant) {
        String key = buildKey(pins, gender);
        ConnectorModel model = connectorModelMap.get(key);
        if (model == null) {
            // FIXME: more reasonable message
            SourceFileErrorReporter.showError(location, "Connector model not found");
            return null;
        }
        return model;
    }

    @Override
    public Collection<ConnectorModel> listModels() {
        return new ArrayList<>(connectorModelMap.values());
    }

    private String buildKey(int pins, ConnectorGender gender) {
        return "" + pins + "#" + gender.name();
    }


}
