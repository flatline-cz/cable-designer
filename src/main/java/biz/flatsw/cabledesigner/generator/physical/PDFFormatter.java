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
import biz.flatsw.cabledesigner.generator.PDFFormatterBase;
import biz.flatsw.cabledesigner.model.Cable;
import biz.flatsw.cabledesigner.model.Connector;
import biz.flatsw.cabledesigner.model.Junction;
import biz.flatsw.cabledesigner.model.PathNode;
import biz.flatsw.cabledesigner.parser.CompilerFailure;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.util.Matrix;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class PDFFormatter extends PDFFormatterBase implements Physical.Formatter {
    private PDOutlineItem pagesOutline;
    private PDPage page;
    private PDPageContentStream contentStream;
    private float pageBorder = 1f;
    private float titleHeight;
    private float junctionFontSize = 18f;

    private float getPageHeight() {
        return page.getMediaBox().getWidth();
    }

    private float getPageWidth() {
        return page.getMediaBox().getHeight();
    }

    private void drawTitle(String pageTitle) {
        try {
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24);
            float width = PDType1Font.HELVETICA_BOLD.getStringWidth(pageTitle) / 1000 * 24;
            contentStream.newLineAtOffset(
                    (getPageWidth() - width) / 2,
                    getPageHeight() - (fromCentimeters(pageBorder) + 24));
            contentStream.showText(pageTitle);
            contentStream.endText();
            titleHeight = 30;

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new CompilerFailure();
        }
    }

    private void drawJunction(String text, float cx, float cy, boolean bold) {
        try {
            PDType1Font font = bold ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA;
            cy = getPageHeight() - cy;
            final float k = 0.552284749831f;
            float r = junctionFontSize * 1.5f;

            // draw circle
            contentStream.moveTo(cx - r, cy);
            contentStream.curveTo(cx - r, cy + k * r, cx - k * r, cy + r, cx, cy + r);
            contentStream.curveTo(cx + k * r, cy + r, cx + r, cy + k * r, cx + r, cy);
            contentStream.curveTo(cx + r, cy - k * r, cx + k * r, cy - r, cx, cy - r);
            contentStream.curveTo(cx - k * r, cy - r, cx - r, cy - k * r, cx - r, cy);
            contentStream.stroke();

            // draw text
            contentStream.beginText();
            contentStream.setFont(font, junctionFontSize);
            float width = font.getStringWidth(text) / 1000 * junctionFontSize;
            float textYoffset = junctionFontSize -
                    (font.getFontDescriptor().getAscent()
                            / 1000.f) * junctionFontSize;
            contentStream.newLineAtOffset(
                    cx - width / 2,
                    cy - textYoffset);
            contentStream.showText(text);
            contentStream.endText();

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new CompilerFailure();
        }
    }

    private void drawJunctions(float centerY, float positionX,
                               Junction main, List<Cable> cables,
                               boolean previousNodes) {
        if (cables.isEmpty())
            return;
        float startY, deltaY;
        if (cables.size() == 1) {
            startY = centerY;
            deltaY = 0;
        } else {
            float height = getPageHeight() - 2 * fromCentimeters(pageBorder) - titleHeight;
            float usableHeight = height * 0.5f;
            startY = centerY - usableHeight / 2;
            deltaY = usableHeight / (cables.size() - 1);
        }
        for (Cable cable : cables) {
            Junction j = (Junction) cable.getOppositeNode(main);
            drawJunction(j.getName(), positionX, startY, !previousNodes);
            drawConnectionJunctionJunction(
                    getPageWidth() / 2, centerY,
                    positionX, startY, true,
                    String.format("%dmm", cable.getLength()));
            startY += deltaY;
        }
    }

    private void drawConnectionJunctionJunction(
            float x1, float y1,
            float x2, float y2,
            boolean point2junction,
            String text) {
        boolean point1junction;
        if (x1 > x2) {
            float tempX = x1, tempY = y1;
            x1 = x2;
            y1 = y2;
            x2 = tempX;
            y2 = tempY;
            point1junction = point2junction;
            point2junction = true;
        } else {
            point1junction = true;
        }
        // compute coordinates
        float dx = x2 - x1;
        float dy = y2 - y1;
        double phi = Math.atan2(dy, dx);
        double d = Math.hypot(dx, dy);
        float r = junctionFontSize * 1.5f;

        float p1x = point1junction ? (x1 + (float) (r * Math.cos(phi))) : x1;
        float p1y = point1junction ? (y1 + (float) (r * Math.sin(phi))) : y1;
        float p2x = point2junction ? (x1 + (float) ((d - r) * Math.cos(phi))) : x2;
        float p2y = point2junction ? (y1 + (float) ((d - r) * Math.sin(phi))) : y2;

        try {
            contentStream.moveTo(p1x, getPageHeight() - p1y);
            contentStream.lineTo(p2x, getPageHeight() - p2y);
            contentStream.stroke();

            float phiText = (float) -phi;

            contentStream.beginText();
            Matrix matrix = Matrix.getRotateInstance(phiText,
                    (x1 + x2) / 2,
                    getPageHeight() - (y1 + y2) / 2 + 5);
            contentStream.setTextMatrix(matrix);
            contentStream.setFont(PDType1Font.HELVETICA, 10);
            contentStream.newLineAtOffset(0, 0);
            contentStream.showText(text);
            contentStream.setTextMatrix(new Matrix());
            contentStream.endText();

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new CompilerFailure();
        }
    }


    private void drawConnector(Cable cable, Junction junction,
                               float cx, float cy,
                               float fullWidth,
                               float centerY, boolean above) {
        Connector connector=(Connector) cable.getOppositeNode(junction);
        String name= connector.getName();
        String description=connector.getDescription();
        float maxWidth = fullWidth - 10;
        try {
            // compute title width
            PDType1Font nameFont = PDType1Font.HELVETICA_BOLD;
            float nameFontSize = 12;
            float nameWidth = nameFont.getStringWidth(name) / 1000f * nameFontSize;

            // layout description
            PDType1Font textFont = PDType1Font.HELVETICA;
            float textFontSize = 10;
            List<String> lines = new ArrayList<>();
            String textLine = "";
            for (String word : description.split(" ")) {
                if (textLine.isEmpty()) {
                    textLine = word;
                    continue;
                }
                String nextLine = textLine + " " + word;
                float width = textFont.getStringWidth(nextLine) / 1000f * textFontSize;
                if (width <= maxWidth) {
                    textLine = nextLine;
                    continue;
                }
                lines.add(textLine);
                textLine = word;
            }
            if (!textLine.isEmpty())
                lines.add(textLine);

            // get box size
            float padding = 5;
            float textMaxWidth = 0;
            for (String line : lines) {
                float width = textFont.getStringWidth(line) / 1000f * textFontSize;
                if (width > textMaxWidth)
                    textMaxWidth = width;
            }
            float boxWidth = fullWidth;
            float boxHeight = 2 * padding + lines.size() * (textFontSize + 3) + nameFontSize + 3;

            // draw border
            contentStream.moveTo(cx - boxWidth / 2, getPageHeight() - (cy - boxHeight / 2));
            contentStream.lineTo(cx - boxWidth / 2, getPageHeight() - (cy + boxHeight / 2));
            contentStream.lineTo(cx + boxWidth / 2, getPageHeight() - (cy + boxHeight / 2));
            contentStream.lineTo(cx + boxWidth / 2, getPageHeight() - (cy - boxHeight / 2));
            contentStream.lineTo(cx - boxWidth / 2, getPageHeight() - (cy - boxHeight / 2));
            contentStream.stroke();

            // draw text
            float startY = cy - boxHeight / 2 + padding + nameFontSize;
            contentStream.beginText();
            contentStream.setFont(nameFont, nameFontSize);
            contentStream.newLineAtOffset(cx - nameWidth / 2, getPageHeight() - startY);
            contentStream.showText(name);
            contentStream.endText();
            startY += 3 + nameFontSize;
            for (String line : lines) {
                float width = textFont.getStringWidth(line) / 1000f * textFontSize;
                contentStream.beginText();
                contentStream.setFont(textFont, textFontSize);
                contentStream.newLineAtOffset(cx - width / 2, getPageHeight() - startY);
                contentStream.showText(line);
                contentStream.endText();
                startY += 3 + textFontSize;
            }

            // draw connection line
            drawConnectionJunctionJunction(
                    getPageWidth()/2, centerY,
                    cx, above?(cy+boxHeight/2):(cy-boxHeight/2),
                    false,
                    String.format("%dmm", cable.getLength()));

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new CompilerFailure();
        }
    }

    private void drawConnectors(List<Cable> cables, Junction junction,
                                float centerY, float areaHeight,
                                boolean above) {
        if (cables.isEmpty())
            return;

        // create layout
        float positionY = centerY;
        if (above)
            positionY -= areaHeight / 2 - fromCentimeters(3);
        else
            positionY += areaHeight / 2 - fromCentimeters(3);
        float deltaX = getPageWidth() / 8;
        float startX = getPageWidth() / 2 - ((cables.size() - 1) * deltaX / 2);

        for (Cable cable : cables) {
            drawConnector(cable, junction,
                    startX, positionY, deltaX * 0.8f, centerY, above);
            startX += deltaX;
        }

    }

    private void generatePage(Junction junction, Set<String> generatedNames) {
        // prepare page node
        String pageName = "Layout of " + junction.getName() + " - " + junction
                .getCables()
                .stream()
                .map(node -> node.getOppositeNode(junction))
                .map(PathNode::getName)
                .filter(name -> !generatedNames.contains(name))
                .collect(Collectors.joining(", "));

        // create page
        page = new PDPage(PDRectangle.A4);
        page.setRotation(90);
        document.addPage(page);
        try {
            contentStream = new PDPageContentStream(document,
                    page, PDPageContentStream.AppendMode.OVERWRITE, false);
            contentStream.transform(new Matrix(0, 1, -1, 0,
                    page.getMediaBox().getWidth(), 0));
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new CompilerFailure();
        }
        PDPageDestination dest = new PDPageFitWidthDestination();
        dest.setPage(page);
        PDOutlineItem bookmark = new PDOutlineItem();
        bookmark.setDestination(dest);
        bookmark.setTitle(pageName);
        pagesOutline.addLast(bookmark);

        // draw title
        drawTitle(pageName);

        // find center of page
        float centerX = getPageWidth() / 2;
        float height = getPageHeight() - 2 * fromCentimeters(pageBorder) - titleHeight;
        float centerY = height / 2 + fromCentimeters(pageBorder) + titleHeight;

        // draw junction
        drawJunction(junction.getName(), centerX, centerY, true);

        // draw already processed junctions
        List<Cable> cables = new ArrayList<>();
        for (Cable cable : junction.getCables()) {
            PathNode node = cable.getOppositeNode(junction);
            if (!(node instanceof Junction))
                continue;
            if (!generatedNames.contains(node.getName()))
                continue;
            cables.add(cable);
        }
        drawJunctions(
                centerY,
                fromCentimeters(pageBorder) + fromCentimeters(2),
                junction, cables, true);
        // draw next junctions
        cables.clear();
        for (Cable cable : junction.getCables()) {
            PathNode node = cable.getOppositeNode(junction);
            if (!(node instanceof Junction))
                continue;
            if (generatedNames.contains(node.getName()))
                continue;
            cables.add(cable);
        }
        drawJunctions(
                centerY,
                getPageWidth() - fromCentimeters(pageBorder) - fromCentimeters(2),
                junction, cables, false);

        // create 2 lists, below and above
        List<Cable> connectorsAbove = new ArrayList<>();
        List<Cable> connectorsBelow = new ArrayList<>();
        for (Cable cable : junction.getCables()) {
            PathNode node = cable.getOppositeNode(junction);
            if (node instanceof Connector) {
                if (connectorsBelow.size() < connectorsAbove.size())
                    connectorsBelow.add(cable);
                else
                    connectorsAbove.add(cable);
            }
        }
        drawConnectors(connectorsAbove, junction, centerY, height, true);
        drawConnectors(connectorsBelow, junction, centerY, height, false);

        // terminate page
        try {
            contentStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new CompilerFailure();
        }
    }

    private void processJunction(Junction junction, Set<String> generatedNames) {
        // render page
        generatePage(junction, generatedNames);

        // mark all junctions connect
        generatedNames.add(junction.getName());
        List<Junction> nextJunctions = new ArrayList<>();
        junction.getCables()
                .stream()
                .map(node -> node.getOppositeNode(junction))
                .forEach(node -> {
                    if (node instanceof Junction && !generatedNames.contains(node.getName()))
                        nextJunctions.add((Junction) node);
                    generatedNames.add(node.getName());
                });

        // generate all others junctions
        nextJunctions.forEach(j -> processJunction(j, generatedNames));
    }

    @Override
    public void formatLayout() {
        pagesOutline = new PDOutlineItem();
        pagesOutline.setTitle("Physical layout");
        documentOutline.addLast(pagesOutline);

        Set<String> generatedJunctions = new HashSet<>();
        processJunction(Services.getPathManager().getFirstJunction(),
                generatedJunctions);
    }
}
