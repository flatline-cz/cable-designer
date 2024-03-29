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

import biz.flatsw.cabledesigner.model.defs.ConnectorPinComponent;

import java.util.Collection;

public interface Pin {

    // identification
    Connector getConnector();
    int getPosition();
    String getName();

    // type
    ConnectorPinComponent getPinType();
    void setPinType(ConnectorPinComponent type);
    ConnectorPinComponent getSealType();
    void setSealType(ConnectorPinComponent type);

    // signal
    String getSignalName();
    Signal getSignal();

    Collection<SignalPath> getSignalPaths();
}
