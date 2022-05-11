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

package biz.flatsw.cabledesigner;

import biz.flatsw.cabledesigner.model.*;
import biz.flatsw.cabledesigner.model.defs.DefinitionManager;
import biz.flatsw.cabledesigner.model.defs.DefinitionManagerImpl;
import biz.flatsw.cabledesigner.parser.FileManager;
import biz.flatsw.cabledesigner.parser.FileManagerImpl;

public class Services {
    private static final FileManager fileManager=new FileManagerImpl();
    private static final PathManager pathManager=new PathManagerImpl();
    private static final ConnectorManager connectorManager=new ConnectorManagerImpl();
    private static final DeviceManager deviceManager=new DeviceManagerImpl();
    private static final DefinitionManager definitionManager=new DefinitionManagerImpl();
    private static final SignalManager signalManager=new SignalManagerImpl();
    private static final CableManager cableManager=new CableManagerImpl();
    private static final DocumentProperties documentProperties=new DocumentPropertiesImpl();


    private Services() {}

    public static FileManager getFileManager() {
        return fileManager;
    }

    public static PathManager getPathManager() {
        return pathManager;
    }

    public static ConnectorManager getConnectorManager() {
        return connectorManager;
    }

    public static DeviceManager getDeviceManager() {
        return deviceManager;
    }

    public static DefinitionManager getDefinitionManager() {
        return definitionManager;
    }

    public static SignalManager getSignalManager() {
        return signalManager;
    }

    public static CableManager getCableManager() {
        return cableManager;
    }

    public static DocumentProperties getDocumentProperties() {
        return documentProperties;
    }

}
