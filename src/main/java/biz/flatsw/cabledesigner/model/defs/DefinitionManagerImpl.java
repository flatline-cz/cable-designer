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
import biz.flatsw.cabledesigner.parser.Location;
import biz.flatsw.cabledesigner.parser.SourceFileErrorReporter;

import java.util.*;
import java.util.stream.Collectors;

public class DefinitionManagerImpl implements DefinitionManager {
    private final Map<String, ConnectorFamily> connectorFamilyMap = new TreeMap<>();
    private final List<WireType> wireTypes=new ArrayList<>();

    @Override
    public ConnectorModel createConnectorModel(
            Location location, ConnectorModelIdentification modelIdentification) {
        // get / create family
        ConnectorFamily connectorFamily = connectorFamilyMap.get(modelIdentification.getFamily());
        if (connectorFamily == null) {
            connectorFamily = new ConnectorFamilyImpl(modelIdentification.getFamily());
            connectorFamilyMap.put(modelIdentification.getFamily(), connectorFamily);
        }

        // create connector model
        return connectorFamily.createModel(
                location,
                modelIdentification.getPinCount(),
                modelIdentification.getGender());
    }

    @Override
    public ConnectorModel getConnectorModel(
            Location location,
            ConnectorModelIdentification model) {

        ConnectorFamily connectorFamily = connectorFamilyMap.get(model.getFamily());
        if(connectorFamily==null) {
            SourceFileErrorReporter.showError(
                    location,
                    String.format(
                            "Connector family '%s' is not known",
                            model.getFamily()));
            return null;
        }
        return connectorFamily.getModel(
                location,
                model.getPinCount(), model.getGender(), model.getVariant());
    }

    private boolean matches(
            ConnectorModel model,
            String family, int pins, ConnectorGender gender, String variant) {
        return false;
    }

    @Override
    public Collection<ConnectorFamily> listConnectorFamilies() {
        return new ArrayList<>(connectorFamilyMap.values());
    }

    @Override
    public void createWireType(float crossSection, float insulationDiameter, float currentRating, Color color, PartNumber partNumber) {
        WireType wireType=new WireTypeImpl(
                crossSection, insulationDiameter,
                currentRating, color, partNumber);
        wireTypes.add(wireType);
    }

    @Override
    public Set<WireType> findSuitableWireTypes(SignalSpecification signalSpecification) {
        return wireTypes
                .stream()
                .filter(wireType -> wireType.matches(signalSpecification))
                .collect(Collectors.toSet());
    }
}
