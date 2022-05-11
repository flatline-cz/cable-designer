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

package biz.flatsw.cabledesigner.model;

import biz.flatsw.cabledesigner.Services;
import biz.flatsw.cabledesigner.model.defs.ConnectorModel;
import biz.flatsw.cabledesigner.model.defs.ConnectorModelIdentification;
import biz.flatsw.cabledesigner.parser.Location;
import biz.flatsw.cabledesigner.parser.SourceFileErrorReporter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class ConnectorManagerImpl implements ConnectorManager {
    private final Map<String, ConnectorImpl> connectorMap = new TreeMap<>();

    @Override
    public Connector referenceConnector(Location referenceLocation, String name) {
        ConnectorImpl connector = connectorMap.get(name);
        if (connector == null) {
            SourceFileErrorReporter.showError(
                    referenceLocation, String.format("Connector %s is not defined", name));
            return null;
        }
        return connector;
    }

    @Override
    public Connector getConnector(String name) {
        return connectorMap.get(name);
    }

    @Override
    public Connector createConnector(Location definitionLocation,
                                     String name, String description,
                                     ConnectorModelIdentification model) {
        // check redefinition
        ConnectorImpl connector = connectorMap.get(name);
        if (connector != null) {
            if (connector.getDescription() != null) {
                if (connector.getDescription().equals(description))
                    return connector;
                SourceFileErrorReporter.showSymbolRedefinitionError(
                        definitionLocation, connectorMap.get(name), "connector");
            }
            connector.setDescription(definitionLocation, description);
            return connector;
        }

        // get connector model
        ConnectorModel connectorModel=Services
                .getDefinitionManager()
                .getConnectorModel(definitionLocation, model);

        connector = new ConnectorImpl(definitionLocation,
                name, description,
                connectorModel, model.getVariant());
        connectorMap.put(name, connector);
        return connector;
    }

    @Override
    public Collection<Connector> listConnectors() {
        return new ArrayList<>(connectorMap.values());
    }
}
