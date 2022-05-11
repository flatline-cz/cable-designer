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

package biz.flatsw.cabledesigner.generator.title;

import biz.flatsw.cabledesigner.Services;
import biz.flatsw.cabledesigner.generator.PDFFormatterBase;
import biz.flatsw.cabledesigner.model.DocumentProperties;
import biz.flatsw.cabledesigner.parser.CompilerFailure;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.util.Matrix;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PDFFormatter extends PDFFormatterBase implements Title.Formatter {
    private PDPage page;
    private PDPageContentStream contentStream;
    private float positionY;


    private void renderTitle(String text, float fontSize) {
        PDType1Font font = PDType1Font.TIMES_BOLD;
        try {
            float maxWidth = page.getMediaBox().getWidth();
            float centerX = maxWidth / 2;
            maxWidth -= fromCentimeters(6);

            List<String> lines = new ArrayList<>();
            String textLine = "";
            for (String word : text.split(" ")) {
                if (text.isEmpty()) {
                    textLine = word;
                    continue;
                }
                String nextLine = textLine + " " + word;
                float width = font.getStringWidth(nextLine) / 1000 * fontSize;
                if (width <= maxWidth) {
                    textLine = nextLine;
                    continue;
                }
                lines.add(textLine);
                textLine = word;
            }
            if (!textLine.isEmpty())
                lines.add(textLine);

            for (String line : lines) {
                float width = font.getStringWidth(line) / 1000 * fontSize;

                contentStream.beginText();
                contentStream.setFont(font, fontSize);
                contentStream.newLineAtOffset(centerX - width / 2,
                        page.getMediaBox().getHeight() - (positionY + fontSize));
                contentStream.showText(line);
                contentStream.endText();
                positionY += fontSize * 1.2;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new CompilerFailure();
        }
    }

    @Override
    public void generate() {
        try {
            // create page
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            try {
                contentStream = new PDPageContentStream(document,
                        page, PDPageContentStream.AppendMode.OVERWRITE, false);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new CompilerFailure();
            }
            PDPageDestination dest = new PDPageFitWidthDestination();
            dest.setPage(page);
            PDOutlineItem bookmark = new PDOutlineItem();
            bookmark.setDestination(dest);
            bookmark.setTitle("Title");
            documentOutline.addLast(bookmark);

            positionY = fromCentimeters(5);

            boolean anything = false;
            String value;
            DocumentProperties properties = Services.getDocumentProperties();

            if (properties.getProject() != null) {
                anything = true;
                renderTitle(properties.getProject(), 40);
            }
            if (properties.getHarness() != null) {
                anything = true;
                renderTitle(properties.getHarness(), 40);
            }
            if (properties.getRevision() != null) {
                anything = true;
                renderTitle(String.format(
                                "(revision %s)", properties.getRevision()),
                        24);
            }
            if(anything)
                positionY += fromCentimeters(2);
            if(properties.getCompany()!=null) {
                renderTitle(properties.getCompany(), 24);
            }
            String date = SimpleDateFormat.getDateInstance(DateFormat.LONG)
                    .format(new Date());
            renderTitle("Created: " + date, 12);


            contentStream.close();

        } catch (Exception ex) {

        }
    }
}
