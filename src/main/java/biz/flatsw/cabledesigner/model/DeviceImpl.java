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

import biz.flatsw.cabledesigner.model.defs.ConnectorModel;
import biz.flatsw.cabledesigner.parser.Location;
import biz.flatsw.cabledesigner.parser.SourceFileErrorReporter;
import biz.flatsw.cabledesigner.parser.SymbolBase;

import java.util.ArrayList;
import java.util.List;

public class DeviceImpl extends SymbolBase implements Device {
    private final List<ConnAssignment> connectorModels = new ArrayList<>();


    public DeviceImpl(Location definitionLocation, String name) {
        super(definitionLocation, name);
    }

    @Override
    public String getName(int position) {
        return connectorModels.size() == 1
                ? getName()
                : (getName() + "/" + position);
    }

    public void addConnectorModel(ConnectorModel connectorModel) {
        connectorModels.add(new ConnAssignment(
                connectorModels.size() + 1, connectorModel));
    }

    public boolean isCompletelyAssigned(Location location) {
        for (ConnAssignment c : connectorModels)
            if (c.connectorInstance == null) {
                SourceFileErrorReporter.showError(location,
                        String.format("Only %d of %d connectors assigned",
                                c.getPosition() + 1, connectorModels.size()));
                return false;
            }
        return true;
    }

    public void attachConnector(Location location, Connector connector) {
        for (ConnAssignment connModel : connectorModels) {
            if (connModel.getConnectorInstance() == null) {
                connModel.setConnectorInstance(connector);
                connector.attachDevice(this, connModel.getPosition());
                return;
            }
        }
        SourceFileErrorReporter.showError(location,
                String.format("Too many connectors assigned, only %d are defined for device",
                        connectorModels.size()));
    }

    private static class ConnAssignment {
        private final int position;
        private final ConnectorModel connectorModel;
        private Connector connectorInstance;

        public ConnAssignment(int position, ConnectorModel connectorModel) {
            this.position = position;
            this.connectorModel = connectorModel;
        }

        public int getPosition() {
            return position;
        }

        public void setConnectorInstance(Connector connectorInstance) {
            this.connectorInstance = connectorInstance;
        }

        public ConnectorModel getConnectorModel() {
            return connectorModel;
        }

        public Connector getConnectorInstance() {
            return connectorInstance;
        }
    }
}
