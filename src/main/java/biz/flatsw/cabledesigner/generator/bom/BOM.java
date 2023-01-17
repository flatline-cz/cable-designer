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

package biz.flatsw.cabledesigner.generator.bom;

import biz.flatsw.cabledesigner.Services;
import biz.flatsw.cabledesigner.generator.FormatterPlugin;
import biz.flatsw.cabledesigner.generator.GeneratorBase;
import biz.flatsw.cabledesigner.model.Connector;
import biz.flatsw.cabledesigner.model.Pin;
import biz.flatsw.cabledesigner.model.WireChain;
import biz.flatsw.cabledesigner.model.WireChainSegment;
import biz.flatsw.cabledesigner.model.defs.Component;
import biz.flatsw.cabledesigner.model.defs.ConnectorModel;
import biz.flatsw.cabledesigner.model.defs.PartNumber;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BOM extends GeneratorBase<BOM.Formatter> {

    @Override
    protected void generateContent() {
        Map<String, Material> bom = new HashMap<>();
        fillBom(bom);
        bom.values()
                .stream()
                .sorted()
                .forEach(this::generateMaterialLine);
    }

    private void fillPinBom(Map<String, Material> bom, Connector connector) {
        ConnectorModel model = connector.getModel();
        List<Pin> pins = connector.listPins();
        for (int i = 0; i < pins.size(); i++) {
            Pin pin = pins.get(i);
            if (pin == null) {
                PartNumber partNumber = model.getPinCavityPlug(i);
                if (partNumber != null)
                    updateBom(
                            bom,
                            partNumber.getPartNumber(),
                            partNumber.getVendor(),
                            Component.Type.CAVITY_PLUG,
                            1);
            } else {
                if (pin.getPinType() != null)
                    updateBom(bom, pin.getPinType());
                if (pin.getSealType() != null)
                    updateBom(bom, pin.getSealType());
            }
        }
    }

    private void fillBOMWire(Map<String, Material> bom, WireChain wireChain) {
        wireChain.listParts()
                .stream()
                .filter(part -> part instanceof WireChainSegment)
                .forEach(part -> updateBom(
                        bom,
                        ((WireChainSegment) part).getWire().getSignal().getWireType().getPartNumber().getPartNumber(),
                        ((WireChainSegment) part).getWire().getSignal().getWireType().getPartNumber().getVendor(),
                        ((WireChainSegment) part).getWire().getLength()));
    }

    private boolean isConnectorComponent(Component component) {
        return component.getType() == Component.Type.HOUSING || component.getType() == Component.Type.ACCESSORY;
    }

    private void fillBom(Map<String, Material> bom) {
        // add all connector components
        Services.getConnectorManager().listConnectors().forEach(c -> c
                .getModel()
                .listComponents()
                .stream()
                .filter(this::isConnectorComponent).forEach(component -> updateBom(bom, component)));

        // add all pins & cavity plugs
        Services.getConnectorManager().listConnectors().forEach(
                c -> fillPinBom(bom, c));

        // add all wires
        Services.getSignalManager()
                .listWireChains()
                .forEach(chain -> fillBOMWire(bom, chain));
    }

    private void updateBom(Map<String, Material> bom, Component component) {
        updateBom(
                bom,
                component.getPartNumber().getPartNumber(),
                component.getPartNumber().getVendor(),
                component.getType(),
                component.getCount());
    }

    private void updateBom(Map<String, Material> bom, String partNumber,
                           String vendor, Component.Type type, int count) {
        addMaterial(bom, type, partNumber, vendor, "pcs", count);
    }

    private void addMaterial(Map<String, Material> bom,
                             Component.Type type,
                             String partNumber,
                             String vendor,
                             String units,
                             int count) {
        if("--none--".equals(partNumber))
            return;
        String key=partNumber+"##||##"+vendor;
        Material material=bom.get(key);
        if(material==null) {
            material=new Material(type, partNumber, vendor, units);
            bom.put(key, material);
        }
        material.qty+=count;
    }

    private void updateBom(Map<String, Material> bom, String partNumber,
                           String vendor, int count) {
        addMaterial(bom, null, partNumber, vendor, "mm", count);
    }

    private void generateMaterialLine(Material material) {
        formatter.formatMaterialLine(
                material.getType(),
                material.partNumber,
                material.vendor,
                material.units,
                material.qty);
    }

    @Override
    protected Class<Formatter> getFormatterClass() {
        return Formatter.class;
    }

    @Override
    public int getPriority() {
        return 10;
    }

    static class Material implements Comparable<Material> {
        private final Component.Type type;
        private final String partNumber;
        private final String vendor;
        private final String units;
        private int qty;

        public Material(Component.Type type, String partNumber, String vendor, String units) {
            this.type = type;
            this.partNumber = partNumber;
            this.units = units;
            this.vendor = vendor!=null?vendor:"";
        }

        @Override
        public int compareTo(Material o) {
            if (type == null && o.type != null)
                return 1;
            if (type != null && o.type == null)
                return -1;
            if (type != null) {
                if (type.ordinal() < o.type.ordinal())
                    return -1;
                if (type.ordinal() > o.type.ordinal())
                    return 1;
            }
            int i = partNumber.compareTo(o.partNumber);
            return i != 0 ? i : vendor.compareTo(o.vendor);
        }

        public String getType() {
            return type != null
                    ? type.name().toLowerCase().replace('_', ' ')
                    : "wire";
        }
    }

    public interface Formatter extends FormatterPlugin {
        // material consumption page
        void formatMaterialLine(String type, String partNumber, String vendor, String units, int qty);

    }

}
