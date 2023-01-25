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

package biz.flatsw.cabledesigner.parser;

import biz.flatsw.cabledesigner.Config;
import biz.flatsw.cabledesigner.Services;
import biz.flatsw.cabledesigner.model.*;
import biz.flatsw.cabledesigner.model.defs.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

public class DocumentParser implements InvocationHandler {
    private FileManager.InputFile currentFile;
    private CableDesignerParserListener listener;
    private final static String[] IGNORED_RULES = new String[]{
            "EveryRule", "Path_element", "Document",
            "Device_connector", "Device_signal",
            "DeviceAttachmentConnector",
            "SignalName", "PinReference", "PartNumber", "PinCount",
            "ConnectorModel", "ConnectorComponents", "ConnectorComponent",
            "Harness", "ConnectorModelRef", "SignalConnection",
            "WireCrossSection", "WireInsulation", "WireDefinitionColor", "WireColor",
            "CurrentRating", "SignalSpecification",
            "PinDefinition", "PinComponent", "PinPartNumber",
            "PinCrossSection", "PinInsulation",
            "InsulationDiameter", "SealPartNumber",
            "PinName", "PinNumber", "PinNaming", "PinName_section", "PinName_sectionName",
            "PinSection", "CavityPlugPartNumber", "SignalPath"
    };

    public void parseFile(String file, FileManager.InputFile parent) {
        // file management
        currentFile = Services.getFileManager().addFile(parent, file);
        listener = (CableDesignerParserListener) Proxy.newProxyInstance(
                CableDesignerParserListener.class.getClassLoader(),
                new Class[]{CableDesignerParserListener.class},
                this);

        // error reporters
        SyntaxErrorReporter syntaxErrorReporter = new SyntaxErrorReporter(currentFile);
        CharStream inputStream;
        try {
            inputStream = CharStreams.fromPath(currentFile.getFile().toPath());
        } catch (IOException ex) {
            throw new CompilerFailure();
        }
        CableDesignerLexer lexer = new CableDesignerLexer(inputStream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(syntaxErrorReporter);
        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        CableDesignerParser parser = new CableDesignerParser(commonTokenStream);
        parser.removeErrorListeners();
        parser.addErrorListener(syntaxErrorReporter);
        CableDesignerParser.DocumentContext definitionContext = parser.document();
        new ParseTreeWalker().walk(listener, definitionContext);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        if (methodName.startsWith("enter") && args.length == 1 && args[0] instanceof ParserRuleContext) {
            // extract parameters
            methodName = methodName.substring(5);
            if (methodName.endsWith("_rule"))
                methodName = methodName.substring(0, methodName.length() - 5);
            ParserRuleContext ctx = (ParserRuleContext) args[0];

            // skip ignored
            for (String name : IGNORED_RULES)
                if (name.equals(methodName))
                    return null;


            if ("Include".equals(methodName)) {
                processInclude((CableDesignerParser.IncludeContext) ctx);
                return null;
            }

            if ("Path".equals(methodName)) {
                processPath((CableDesignerParser.PathContext) ctx);
                return null;
            }

            if ("Connector".equals(methodName)) {
                processConnector((CableDesignerParser.ConnectorContext) ctx);
                return null;
            }

//            if ("DeviceAttachment".equals(methodName)) {
//                processDeviceAttachment((CableDesignerParser.DeviceAttachmentContext) ctx);
//                return null;
//            }

            if ("Signal".equals(methodName)) {
                processSignal((CableDesignerParser.SignalContext) ctx);
                return null;
            }

            if ("ConnectorDefinition".equals(methodName)) {
                processConnectorDefinition((CableDesignerParser.ConnectorDefinitionContext) ctx);
                return null;
            }

            if ("WireDefinition".equals(methodName)) {
                processWireDefinition((CableDesignerParser.WireDefinitionContext) ctx);
                return null;
            }

            if ("DocumentProperty".equals(methodName)) {
                processDocumentProperty((CableDesignerParser.DocumentPropertyContext) ctx);
                return null;
            }

            System.out.println("Enter: " + methodName + ", ctx=" + ctx);
        }
        return null;
    }

    private String extractText(String text) {
        return text.substring(1, text.length() - 1);
    }

    private Location getLocation(ParserRuleContext ctx, int type) {
        return new Location(currentFile,
                ctx.getToken(type, 0).getSymbol().getLine(),
                ctx.getToken(type, 0).getSymbol().getCharPositionInLine());
    }

    private Location getLocation(ParserRuleContext ctx) {
        return new Location(currentFile,
                ctx.start.getLine(),
                ctx.start.getCharPositionInLine());
    }

    private void processInclude(CableDesignerParser.IncludeContext ctx) {
        String filename = extractText(ctx.file.getText());
        new DocumentParser().parseFile(filename, currentFile);
    }

    private void processPath(CableDesignerParser.PathContext ctx) {
        // create a junction
        String junctionName = ctx.junction.getText();
        Junction junction = Services.getPathManager().getJunction(junctionName);

        for (CableDesignerParser.Path_elementContext party : ctx.path_element()) {
            // create path node
            PathNode pathNode;
            if (party.path_element_junction != null)
                pathNode = Services
                        .getPathManager()
                        .getJunction(party.name.getText());
            else
                pathNode = Services
                        .getConnectorManager()
                        .referenceConnector(
                                getLocation(ctx),
                                party.name.getText());

            // extract length
            String lengthText = party.length.getText().toLowerCase().trim();
            if (!lengthText.matches("^[0-9]+[cm]m$"))
                throw new CompilerFailure();
            int multiplier = 1;
            if (lengthText.endsWith("cm"))
                multiplier = 10;
            lengthText = lengthText.substring(0, lengthText.length() - 2);
            int cableLength = Integer.parseInt(lengthText) * multiplier;

            // create cable
            Cable cable = new CableImpl(cableLength, junction, pathNode);
            Services.getCableManager().addCable(cable);

        }
    }

    private void processConnector(CableDesignerParser.ConnectorContext ctx) {
        String name = ctx.name.getText();
        String description = extractText(ctx.description.getText());
        ConnectorModelIdentification modelIdentification = parseConnectorModel(
                name, ctx.connectorModelRef());
        Services.getConnectorManager()
                .createConnector(
                        getLocation(ctx),
                        name, description,
                        modelIdentification);
    }


//    private void processDeviceAttachment(CableDesignerParser.DeviceAttachmentContext ctx) {
//        String deviceName = extractText(ctx.devName.getText());
//
//        // TODO: get device definition
//
//        // process connector names
//        List<CableDesignerParser.DeviceAttachmentConnectorContext> connContexts =
//                ctx.deviceAttachmentConnector();
//        for (int i = 0; i < connContexts.size(); i++) {
//            CableDesignerParser.DeviceAttachmentConnectorContext connCtx = connContexts.get(i);
//            String connName = connCtx.name.getText();
//            String connDescription = deviceName;
//            if (connContexts.size() > 1)
//                connDescription += "/" + (i + 1);
//
//            // create connector
//            Services.getConnectorManager()
//                    .createConnector(
//                            getLocation(connCtx),
//                            connName, connDescription);
//        }
//    }


    private void parsePinNameSequence(CableDesignerParser.PinName_ruleContext ctx,
                                      List<String> pinNames) {
        // single name?
        if (ctx.to == null) {
            pinNames.add(extractText(ctx.from.getText()));
            return;
        }

        // get range
        String startText = extractText(ctx.from.getText());
        String endText = extractText(ctx.to.getText());

        // numeric?
        if (startText.matches("[0-9]+") && endText.matches("[0-9]+")) {
            int i = Integer.parseInt(startText);
            int last = Integer.parseInt(endText);
            for (; i <= last; i++)
                pinNames.add(String.valueOf(i));
            return;
        }

        // find character that differs
        if (startText.length() != endText.length()) {
            throw new CompilerFailure();
        }
        int pos = -1;
        for (int i = 0; i < startText.length(); i++) {
            if (startText.charAt(i) == endText.charAt(i))
                continue;
            if (pos != -1)
                throw new CompilerFailure();
            pos = i;
        }
        if (pos == -1)
            throw new CompilerFailure();

        // number?
        char startCh = startText.charAt(pos);
        char endCh = endText.charAt(pos);
        if (startCh > endCh)
            throw new CompilerFailure();

        // generate sequence
        while (startCh <= endCh) {
            StringBuilder name = new StringBuilder();
            if (pos != 0)
                name.append(startText, 0, pos);
            name.append(startCh);
            if (pos < startText.length() - 1)
                name.append(startText, pos + 1, startText.length() - 1);
            pinNames.add(name.toString());
            startCh++;
        }


    }


    private void processConnectorDefinition(
            CableDesignerParser.ConnectorDefinitionContext ctx) {
        // create connector model
        ConnectorModelIdentification connectorModelIdentification = parseConnectorModel(ctx.connectorModel());
        ConnectorModel connectorModel = Services
                .getDefinitionManager()
                .createConnectorModel(
                        getLocation(ctx),
                        connectorModelIdentification);

        // parse pin naming
        if (ctx.pinNaming_rule() != null) {
            for (CableDesignerParser.PinName_section_ruleContext sectionCtx
                    : ctx.pinNaming_rule().pinName_section_rule()) {
                List<String> pins = new ArrayList<>();
                parsePinNameSequence(sectionCtx.pinName_rule(), pins);
                connectorModel.setPinNames(sectionCtx.section != null ?
                        extractText(sectionCtx.section.name.getText()) : null, pins);
            }
        } else {
            connectorModel.setDefaultPinNames();
        }

        // parse components
        for (CableDesignerParser.ConnectorComponentContext componentContext
                : ctx.connectorComponent()) {
            // parse component
            PartNumber partNumber = parsePartNumber(componentContext.partNumber_rule());
            int count = (componentContext.count != null)
                    ? Integer.parseInt(componentContext.count.getText())
                    : 1;
            Component.Type type = null;
            if (componentContext.KEYWORD_HOUSING() != null)
                type = Component.Type.HOUSING;
            if (componentContext.KEYWORD_ACCESSORY() != null)
                type = Component.Type.ACCESSORY;
            if (componentContext.KEYWORD_CAVITY() != null)
                type = Component.Type.CAVITY_PLUG;
            connectorModel.addComponent(type, partNumber, count);
        }

        // parse pins
        for (CableDesignerParser.PinDefinition_ruleContext sectionCtx
                : ctx.pinDefinition_rule()) {
            // extract section
            String sectionName = sectionCtx.section != null
                    ? extractText(sectionCtx.section.TEXT().getText())
                    : null;
            Location location = getLocation(sectionCtx);

            if (sectionCtx.cavityPlugPartNumber_rule() != null) {
                connectorModel.addCavityPlug(
                        location,
                        sectionName,
                        parsePartNumber(
                                sectionCtx.cavityPlugPartNumber_rule().partNumber_rule()));
            }

            for (CableDesignerParser.PinComponent_ruleContext pinComponentCtx
                    : sectionCtx.pinComponent_rule()) {
                if (pinComponentCtx.pinPartNumber_rule() != null) {
                    connectorModel.addPinType(
                            location, sectionName,
                            parsePartNumber(
                                    pinComponentCtx.pinPartNumber_rule()
                                            .partNumber_rule()),
                            Float.parseFloat(
                                    pinComponentCtx.pinPartNumber_rule()
                                            .pinCrossSection_rule()
                                            .crossFrom
                                            .crossSection
                                            .getText()),
                            Float.parseFloat(
                                    pinComponentCtx.pinPartNumber_rule()
                                            .pinCrossSection_rule()
                                            .crossTo
                                            .crossSection
                                            .getText()),
                            Float.parseFloat(
                                    pinComponentCtx.pinPartNumber_rule()
                                            .pinInsulation_rule()
                                            .insulationFrom
                                            .diameter
                                            .getText()),
                            Float.parseFloat(
                                    pinComponentCtx.pinPartNumber_rule()
                                            .pinInsulation_rule()
                                            .insulationTo
                                            .diameter
                                            .getText()));
                }
                if (pinComponentCtx.sealPartNumber_rule() != null) {
                    connectorModel.addPinSeal(
                            location, sectionName,
                            parsePartNumber(
                                    pinComponentCtx.sealPartNumber_rule()
                                            .partNumber_rule()),
                            Float.parseFloat(
                                    pinComponentCtx.sealPartNumber_rule()
                                            .pinInsulation_rule()
                                            .insulationFrom
                                            .diameter
                                            .getText()),
                            Float.parseFloat(
                                    pinComponentCtx.sealPartNumber_rule()
                                            .pinInsulation_rule()
                                            .insulationTo
                                            .diameter
                                            .getText()));
                }
            }

        }

    }


    private ConnectorModelIdentification parseConnectorModel(
            CableDesignerParser.ConnectorModelContext ctx) {
        int pinCount = parsePinCount(ctx.pinCount_rule());
        ConnectorGender gender = ConnectorGender.NEUTRAL;
        if (ctx.male != null)
            gender = ConnectorGender.MALE;
        if (ctx.female != null)
            gender = ConnectorGender.FEMALE;
        return new ConnectorModelIdentification(
                extractText(ctx.family.getText()),
                pinCount,
                gender);
    }

    private ConnectorModelIdentification parseConnectorModel(
            String name,
            CableDesignerParser.ConnectorModelRefContext ctx) {
        int pinCount = parsePinCount(ctx.pinCount_rule());
        ConnectorGender gender = ConnectorGender.NEUTRAL;
        if (name.endsWith("M"))
            gender = ConnectorGender.MALE;
        if (name.endsWith("F"))
            gender = ConnectorGender.FEMALE;
        return new ConnectorModelIdentification(
                extractText(ctx.family.getText()),
                pinCount,
                gender);
    }

    private int parsePinCount(CableDesignerParser.PinCount_ruleContext ctx) {
        return (ctx.pinCount != null) ? Integer.parseInt(ctx.pinCount.getText()) : 1;
    }

    private void processSignal(CableDesignerParser.SignalContext ctx) {

        // create signal
        Signal signal = Services.getSignalManager().createSignal(
                getLocation(ctx),
                parseSignalName(ctx.signalName()),
                extractText(ctx.description.getText()));


        //        parseSignalSpecification(ctx.signalSpecification_rule()));

        // process each signal path
        for (CableDesignerParser.SignalPathContext signalPathContext
                : ctx.signalPath()) {
            SignalSpecification signalSpecification = parseSignalSpecification(
                    signalPathContext.signalSpecification_rule());

            SignalPath signalPath=Services.getSignalManager().createSignalPath(
                    getLocation(signalPathContext),
                    signal,
                    signalSpecification,
                    signalPathContext.KEYWORD_ORDERED()!=null);

            // process each connector pin
            for (CableDesignerParser.SignalConnectionContext connContext
                    : signalPathContext.signalConnection()) {
                Connector connector = Services
                        .getConnectorManager()
                        .referenceConnector(
                                getLocation(connContext),
                                connContext.conn.getText());

                connector.attachSignalPath(
                        getLocation(connContext),
                        parsePinName(connContext.pinRef),
                        signalPath);
            }
        }
    }

    private String parsePinName(CableDesignerParser.PinRef_ruleContext ctx) {
        if (ctx instanceof CableDesignerParser.PinNameContext)
            return extractText(ctx.getText());
        if (ctx instanceof CableDesignerParser.PinNumberContext)
            return ctx.getText();
        return null;
    }

    private String parseSignalName(CableDesignerParser.SignalNameContext ctx) {
        return extractText(ctx.TEXT().getText());
    }

    private SignalSpecification parseSignalSpecification(
            CableDesignerParser.SignalSpecification_ruleContext ctx) {
        float currentRating = parseCurrentRating(ctx.currentRating_rule());
        Color color = (ctx.wireColor_rule() != null)
                ? parseColor(ctx.wireColor_rule())
                : new ColorImpl(Config.getDefaultColor());
        return new SignalSpecification(currentRating, color);
    }

    private void processWireDefinition(CableDesignerParser.WireDefinitionContext ctx) {
        float crossSection = Float.parseFloat(ctx.wireCrossSection_rule().crossSection.getText());
        float insulationDiameter = Float.parseFloat(ctx.wireInsulation_rule().diameter.getText());
        float currentRating = parseCurrentRating(ctx.currentRating_rule());

        for (CableDesignerParser.WireDefinitionColorContext colorCtx : ctx.wireDefinitionColor()) {
            Services.getDefinitionManager().createWireType(
                    crossSection, insulationDiameter, currentRating,
                    parseColor(colorCtx.color),
                    parsePartNumber(colorCtx.partNumber_rule()));
        }
    }


    private float parseCurrentRating(CableDesignerParser.CurrentRating_ruleContext ctx) {
        return Float.parseFloat(ctx.current.getText());
    }

    private Color parseColor(CableDesignerParser.WireColor_ruleContext ctx) {
        if (ctx.color2 == null) {
            return new ColorImpl(ctx.color1.getText());
        } else {
            return new ColorImpl(new String[]{ctx.color1.getText(), ctx.color2.getText()});
        }
    }

    private PartNumber parsePartNumber(CableDesignerParser.PartNumber_ruleContext ctx) {
        return new PartNumber(
                extractText(ctx.pn.getText()),
                ctx.vendor != null
                        ? extractText(ctx.vendor.getText())
                        : null);
    }

    private void processDocumentProperty(CableDesignerParser.DocumentPropertyContext ctx) {
        String name = extractText(ctx.name.getText()).toLowerCase();
        String value = extractText(ctx.value.getText());
        if ("project".equals(name))
            Services.getDocumentProperties().setProject(value);
        if ("harness".equals(name))
            Services.getDocumentProperties().setHarness(value);
        if ("company".equals(name))
            Services.getDocumentProperties().setCompany(value);
        if ("revision".equals(name))
            Services.getDocumentProperties().setRevision(value);
    }
}
