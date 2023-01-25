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

import biz.flatsw.cabledesigner.generator.GeneratorManager;
import biz.flatsw.cabledesigner.model.Signal;
import biz.flatsw.cabledesigner.parser.CompilerFailure;
import biz.flatsw.cabledesigner.parser.DocumentParser;
import biz.flatsw.cabledesigner.routing.Router;

import java.io.File;
import java.util.*;

public class CableDesigner {
    private String fileName;
    private File outputDir;
    private String outputFormat="txt";
    private final static long startTime=System.currentTimeMillis();
    private final Set<String> selectedGenerators=new HashSet<>();
    private final Set<String> excludedGenerators=new HashSet<>();

    private CableDesigner() {

    }

    public static void printMessage(String text) {
        long time=System.currentTimeMillis()-startTime;
        System.out.printf("%.3f  %s%n", ((float)time)/1000f, text);
    }

    private void generateOutput() {
        GeneratorManager generator=new GeneratorManager();
        generator.setGenerators(selectedGenerators, excludedGenerators);
        generator.setOutput(outputDir, fileName, outputFormat);
        generator.generate();
    }

    private void processData() {
        DocumentParser parser = new DocumentParser();

        printMessage("Parsing definitions...");
        // load definitions
        try {
            parser.parseFile(fileName, null);
        } catch (CompilerFailure ex) {
            // ignore
            ex.printStackTrace();
            return;
        }

        printMessage("Routing signals...");
        // route signals
        for (Signal signal : Services.getSignalManager().listSignals()) {
            Router router = new Router();
            router.route(signal);
        }

        // assign components
        printMessage("Assigning components...");
        ComponentAssigner assigner=new ComponentAssigner();
        assigner.assignComponents();

        printMessage("Model created.");
    }

    public static void main(String[] args) {
        printMessage("Started...");
        CableDesigner designer = new CableDesigner();

        // parse arguments
        for(int i=0;i<args.length;i++) {
            if("-I".equals(args[i]) && i+1< args.length) {
                // add root
                Services.getFileManager().addRoot(new File(args[i+1]));
                i++;
                continue;
            }
            if("-O".equals(args[i]) && i+1< args.length) {
                designer.outputDir=new File(args[i+1]).getAbsoluteFile();
                i++;
                continue;
            }
            if("-F".equals(args[i]) && i+1<args.length) {
                designer.outputFormat=args[i+1];
                i++;
                continue;
            }
            if("-G".equals(args[i]) && i+1<args.length) {
                if(args[i+1].startsWith("-")) {
                    designer.excludedGenerators.add(args[i+1].substring(1));
                } else {
                    designer.selectedGenerators.add(args[i+1]);
                }
                i++;
                continue;
            }


            designer.fileName=args[i];
            break;
        }

        // parse definition
        designer.processData();


        // generate output
        designer.generateOutput();

        printMessage("Finished.");
    }

}
