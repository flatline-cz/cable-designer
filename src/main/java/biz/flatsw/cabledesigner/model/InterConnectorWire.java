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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InterConnectorWire implements Wire {
    private final int length;
    private final List<Cable> cables;
    private final List<PathNode> nodes;

    public InterConnectorWire(PathNode firstNode, List<Cable> cables) {
        this.cables = cables;
        length=cables.stream().mapToInt(Cable::getLength).sum();
        nodes=new ArrayList<>();
        for(int i=0;i<(cables.size()-1);i++) {
            Cable cable=cables.get(i);
            PathNode node=cable.getOppositeNode(firstNode);
            nodes.add(node);
            firstNode=node;
        }
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public List<PathNode> getPath() {
        return Collections.unmodifiableList(nodes);
    }
}
