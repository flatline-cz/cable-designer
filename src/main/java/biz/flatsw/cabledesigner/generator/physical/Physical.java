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

package biz.flatsw.cabledesigner.generator.physical;

import biz.flatsw.cabledesigner.Services;
import biz.flatsw.cabledesigner.generator.FormatterPlugin;
import biz.flatsw.cabledesigner.generator.GeneratorBase;
import biz.flatsw.cabledesigner.model.Cable;
import biz.flatsw.cabledesigner.model.Connector;
import biz.flatsw.cabledesigner.model.Junction;
import biz.flatsw.cabledesigner.model.PathNode;
import biz.flatsw.cabledesigner.parser.CompilerFailure;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.TreeSet;

public class Physical extends GeneratorBase<Physical.Formatter> {


    @Override
    protected void generateContent() {
        formatter.formatLayout();
    }

    @Override
    protected Class<Formatter> getFormatterClass() {
        return Formatter.class;
    }

    @Override
    public int getPriority() {
        return 2000;
    }

    interface Formatter extends FormatterPlugin {
        void formatLayout();
    }
}

//public class Physical extends GeneratorBase<> {
//
//    @Override
//    protected Class getFormatterClass() {
//        return null;
//    }
//
//    @Override
//    public int getPriority() {
//        return 300;
//    }
//
//    private void processJunction(Junction junction, Set<String> processedNodes,
//                                 StringBuilder output) {
//        if (processedNodes.contains(junction.getName()))
//            return;
//        processedNodes.add(junction.getName());
//        output.append("  {\n");
//        output.append("    rankdir=TB;\n");
//        output.append(String.format("    %s [label=\"%s\"; shape=circle];\n", junction.getName(), junction.getName()));
//        int length = junction.getCables().size() <= 3 ? 3 : 4;
//        for (Cable cable : junction.getCables()) {
//            PathNode node = cable.getOppositeNode(junction);
//            if (node instanceof Connector) {
//                String name = node.getName();
//                String desctiption = ((Connector) node).getDescription();
//                String label = String.format("<b>%s</b>", name);
//                if (desctiption != null)
//                    label += "<br/>" + wrapLabel(desctiption);
//                output.append(String.format("    %s [label= < %s >; shape=box];\n",
//                        name, label));
//            }
//        }
//        for (Cable cable : junction.getCables()) {
//            PathNode node = cable.getOppositeNode(junction);
//            if (node instanceof Connector) {
//                output.append(String.format("    %s -- %s [xlabel=< <table bgcolor=\"white\"><tr><td>%dmm</td></tr></table> >; len=%d];\n",
//                        junction.getName(),
//                        node.getName(),
//                        cable.getLength(),
//                        length));
//            }
//        }
//        output.append("  }\n");
//        for (Cable cable : junction.getCables()) {
//            PathNode node = cable.getOppositeNode(junction);
//            if (node instanceof Junction && !processedNodes.contains(node.getName())) {
//                output.append(String.format("  %s -- %s [xlabel= < <table bgcolor=\"white\"><tr><td>%dmm</td></tr></table> >; len=2];\n",
//                        junction.getName(),
//                        ((Junction) node).getName(),
//                        cable.getLength()));
//                processJunction((Junction) node, processedNodes, output);
//            }
//        }
//
//    }
//
//    private String wrapLabel(String text) {
//        StringBuilder builder = new StringBuilder();
//        int lineLength = 0;
//        for (String word : text.split(" ")) {
//            word = word.trim();
//            if (word.isEmpty())
//                continue;
//            if (lineLength + word.length() > 15) {
//                builder.append("<br/>");
//                lineLength = 0;
//            } else {
//                builder.append(' ');
//                lineLength++;
//            }
//            builder.append(word);
//            lineLength += word.length();
//        }
//        return builder.toString().trim();
//    }
//
//    private String generateDOT() {
//        StringBuilder content = new StringBuilder();
//        content.append("graph {\n");
//        content.append("    layout=\"neato\";\n");
//        content.append("    splines=line;\n");
//        Set<String> processedNodes = new TreeSet<>();
//        processJunction(Services.getPathManager().getFirstJunction(),
//                processedNodes, content);
//        content.append("}\n");
//
//
//        return content.toString();
//    }
//
//    private void splitGraph() {
//        // get a set of all nodes
//
//
//    }
//
//    @Override
//    public void generate() {
//        // get output
//        OutputStream output = getOutputFile("physical", "png");
//
//        // FIXME:
//        splitGraph();
//
//        // get input
//        ByteArrayInputStream baos = new ByteArrayInputStream(generateDOT().getBytes(StandardCharsets.UTF_8));
//
//        try {
//            execCommand(baos, output, "dot", "-Tpng");
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            throw new CompilerFailure();
//        } finally {
//            try {
//                output.close();
//            } catch (Exception ex) {
//                // ignore
//            }
//            try {
//                baos.close();
//            } catch (Exception ex) {
//                // ignore
//            }
//        }
//    }
//}
