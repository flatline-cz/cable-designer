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

import biz.flatsw.cabledesigner.parser.CompilerFailure;

import java.util.Map;
import java.util.TreeMap;

public class CableManagerImpl implements CableManager {
    private final Map<String, Cable> cableMap = new TreeMap<>();

    @Override
    public void addCable(Cable cable) {
        cableMap.put(
                getKey(
                        cable.getStartNode().getName(),
                        cable.getEndNode().getName()),
                cable);

    }

    private String getKey(String from, String to) {
        if (from.compareTo(to) > 0) {
            String t = from;
            from = to;
            to = t;
        }
        return from + "##||##" + to;
    }

    @Override
    public Cable findByEnds(String from, String to) {
        Cable cable=cableMap.get(getKey(from, to));
        if(cable==null)
            throw new CompilerFailure();
        return cable;
    }
}
