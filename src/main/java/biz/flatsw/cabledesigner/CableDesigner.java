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

import biz.flatsw.cabledesigner.generator.Generator;
import biz.flatsw.cabledesigner.generator.GeneratorManager;
import biz.flatsw.cabledesigner.model.Connector;
import biz.flatsw.cabledesigner.model.Pin;
import biz.flatsw.cabledesigner.model.Signal;
import biz.flatsw.cabledesigner.model.SignalPath;
import biz.flatsw.cabledesigner.model.defs.ConnectorPinComponent;
import biz.flatsw.cabledesigner.model.defs.WireType;
import biz.flatsw.cabledesigner.parser.CompilerFailure;
import biz.flatsw.cabledesigner.parser.DocumentParser;
import biz.flatsw.cabledesigner.parser.SourceFileErrorReporter;
import biz.flatsw.cabledesigner.routing.Router;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

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

    private boolean wireTypeSuits(SignalPath signalPath, WireType wireType, List<String> status) {
        // TODO: rework
        return true;
//        return signal.listTerminals()
//                .stream().allMatch(p -> {
//                    if (p.getConnector()
//                            .getModel()
//                            .findSuitablePinType(p.getPosition(), wireType) == null) {
//                        status.add("Unable to find suitable pin on "+p.getConnector().getName()+"/"+p.getName());
//                        return false;
//                    }
//                    if (p.getConnector()
//                            .getModel()
//                            .needPinSeal(p.getPosition(), wireType)) {
//                        if(p.getConnector()
//                                .getModel()
//                                .findSuitablePinSeal(p.getPosition(), wireType) == null) {
//                            status.add("Unable to find suitable pin seal on "+p.getConnector().getName()+"/"+p.getName());
//                            return false;
//                        }
//                        return true;
//                    } else
//                        return true;
//                });
    }

    private void assignComponents(SignalPath signalPath) {
        // get suitable wire types
        Set<WireType> wireTypeSet = Services.getDefinitionManager()
                .findSuitableWireTypes(signalPath.getSpecification());

        if (wireTypeSet.isEmpty()) {
            SourceFileErrorReporter.showError(
                    signalPath.getSignal().getDefinitionLocation(),
                    "Unable to find suitable wire type");
            throw new CompilerFailure();
        }

        List<WireType> wireTypes=new ArrayList<>(wireTypeSet)
                .stream()
                .sorted(Comparator
                        .comparing(WireType::getWireSection)
                        .thenComparing(WireType::getColorCodes))
                .collect(Collectors.toList());

        // check each wire type
        List<String> matchingProgress=new ArrayList<>();
        for (WireType wireType : wireTypes) {
            matchingProgress.add("Wire type: "+wireType.getPartNumber().getPartNumber());
            List<String> matchingComponentsProgress=new ArrayList<>();
            if (wireTypeSuits(signalPath, wireType, matchingComponentsProgress)) {
                // assign wire type to signal
                signalPath.setWireType(wireType);
                // TODO: rework
//                // choose all pins & other stuff
//                for (Pin pin : signal.listTerminals()) {
//                    // pin type
//                    ConnectorPinComponent pinType = pin
//                            .getConnector()
//                            .getModel()
//                            .findSuitablePinType(
//                                    pin.getPosition(),
//                                    wireType);
//                    pin.setPinType(pinType);
//                    // seal type
//                    ConnectorPinComponent sealType = pin
//                            .getConnector()
//                            .getModel()
//                            .findSuitablePinSeal(
//                                    pin.getPosition(),
//                                    wireType);
//                    pin.setSealType(sealType);
//                }
                return;
            } else {
                matchingProgress.addAll(
                        matchingComponentsProgress
                                .stream()
                                .map(s -> " - "+s)
                                .collect(Collectors.toList()));
            }
        }

        System.err.printf("Unable to assign components for signal '%s'%n",
                signalPath.getSignal().getName());
        matchingProgress.forEach(System.err::println);
        throw new CompilerFailure();


    }

    private void assignComponents(Connector connector) {

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
//        Services.getSignalManager().listSignals().forEach(s -> s.getSignalPaths().forEach(this::assignComponents));
//        Services.getConnectorManager().listConnectors().forEach(this::assignComponents);

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
