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

package biz.flatsw.cabledesigner.generator;

import biz.flatsw.cabledesigner.parser.CompilerFailure;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.util.Matrix;


import java.util.ArrayList;
import java.util.List;

public abstract class PDFTableFormatter
        extends PDFFormatterBase
        implements FinishingFormatter {
    protected final int LEFT = 0;
    protected final int CENTER = 1;
    protected final int RIGHT = 2;
    private final List<Table> tables = new ArrayList<>();
    private Table actualTable;
    private final float pageBorder = 1f;
    private Context context;
    private PDFont fontNormal;
    private PDOutlineItem pagesOutline;

    protected abstract LayoutStyle getLayoutStyle();

    protected void addTable(float... columnWidths) {
        actualTable = null;
        switch (getLayoutStyle()) {
            case PORTRAIT:
                actualTable = new Table(
                        PDRectangle.A4.getWidth() - 2 * fromCentimeters(pageBorder),
                        columnWidths);
                break;
            case LANDSCAPE:
                actualTable = new Table(
                        PDRectangle.A4.getHeight() - 2 * fromCentimeters(pageBorder),
                        columnWidths);
                break;
            case LANDSCAPE_2COLUMNS:
                actualTable = new Table(
                        (PDRectangle.A4.getHeight() - 3 * fromCentimeters(pageBorder)) / 2,
                        columnWidths);
                break;
        }
        if (actualTable != null)
            tables.add(actualTable);
    }


    protected void addRow() {
        actualTable.addRow(false);
    }

    protected void addHeaderRow() {
        actualTable.addRow(true);
    }

    protected void addCell(String text) {
        actualTable.addCell(text);
    }

    protected void addCell(String text, int alignment) {
        actualTable.addCell(text, alignment);
    }

    protected void addCell(String text, int alignment, boolean wordWrap) {
        actualTable.addCell(text, alignment, wordWrap);
    }

    protected void setTitle(String title) {
        actualTable.setTitle(title, true);
    }

    protected void setTitle(String title, boolean pageBreak) {
        actualTable.setTitle(title, pageBreak);
    }


    protected String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void finish() {
        pagesOutline = new PDOutlineItem();
        pagesOutline.setTitle(getName());
        documentOutline.addLast(pagesOutline);
        tables.forEach(Table::render);
        if (context.contentStream != null) {
            try {
                context.contentStream.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new CompilerFailure();
            }
        }
    }

    protected enum LayoutStyle {
        PORTRAIT, LANDSCAPE, LANDSCAPE_2COLUMNS
    }

    class Context {
        private float x, y, width, height, pageHeight;
        private int areaNumber;
        private PDPageContentStream contentStream;
        private PDPage page;
        private boolean pageBreak;
    }

    private void newPage() {
        if (context.contentStream != null) {
            if (getLayoutStyle() == LayoutStyle.LANDSCAPE_2COLUMNS && context.areaNumber == 0) {
                context.areaNumber++;
                setArea(context);
                return;
            }
            try {
                context.contentStream.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new CompilerFailure();
            }
        }
        context.page = new PDPage(PDRectangle.A4);
        context.areaNumber = 0;
        if (getLayoutStyle() == LayoutStyle.LANDSCAPE || getLayoutStyle() == LayoutStyle.LANDSCAPE_2COLUMNS) {
            context.page.setRotation(90);
        }
        document.addPage(context.page);
        try {
            context.contentStream = new PDPageContentStream(document,
                    context.page, PDPageContentStream.AppendMode.OVERWRITE, false);
            if (getLayoutStyle() == LayoutStyle.LANDSCAPE || getLayoutStyle() == LayoutStyle.LANDSCAPE_2COLUMNS) {
                context.contentStream.transform(new Matrix(0, 1, -1, 0,
                        context.page.getMediaBox().getWidth(), 0));
            }
        } catch (Exception ex) {
            throw new CompilerFailure();
        }
        setArea(context);
    }

    private void updateContext() {
        // no context?
        if (context == null) {
            context = new Context();
            newPage();
        }

        // new page?
        if (context.pageBreak) {
            context.pageBreak = false;
            newPage();
        }
    }



    private void setArea(Context context) {
        switch (getLayoutStyle()) {
            case PORTRAIT:
                context.x = 0;
                context.width = PDRectangle.A4.getUpperRightX() - PDRectangle.A4.getLowerLeftX();
                context.y = 0;
                context.height = PDRectangle.A4.getUpperRightY() - PDRectangle.A4.getLowerLeftY();

                context.x += fromCentimeters(pageBorder);
                context.y += fromCentimeters(pageBorder);
                context.width -= 2 * fromCentimeters(pageBorder);
                context.height -= 2 * fromCentimeters(pageBorder);
                break;
            case LANDSCAPE:
                context.x = 0;
                context.width = PDRectangle.A4.getUpperRightY() - PDRectangle.A4.getLowerLeftY();
                context.y = 0;
                context.height = PDRectangle.A4.getUpperRightX() - PDRectangle.A4.getLowerLeftX();

                context.x += fromCentimeters(pageBorder);
                context.y += fromCentimeters(pageBorder);
                context.width -= 2 * fromCentimeters(pageBorder);
                context.height -= 2 * fromCentimeters(pageBorder);
                break;
            case LANDSCAPE_2COLUMNS:
                context.width = context.page.getMediaBox().getHeight() / 2;
                context.x = context.areaNumber * context.width;
                context.y = 0;
                context.height = context.page.getMediaBox().getWidth();

                context.x += context.areaNumber == 0
                        ? fromCentimeters(pageBorder)
                        : fromCentimeters(pageBorder / 2);
                context.y += fromCentimeters(pageBorder);
                context.width -= 1.5 * fromCentimeters(pageBorder);
                context.height -= 2 * fromCentimeters(pageBorder);
                break;

        }
        context.pageHeight = context.height;
    }

    private float getPageHeight() {
        return getLayoutStyle() == LayoutStyle.PORTRAIT
                ? context.page.getMediaBox().getHeight()
                : context.page.getMediaBox().getWidth();
    }

    private void drawLine(float x1, float y1, float x2, float y2) {
        try {
            context.contentStream.moveTo(x1, getPageHeight() - y1);
            context.contentStream.lineTo(x2, getPageHeight() - y2);
            context.contentStream.stroke();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new CompilerFailure();
        }
    }

    private void drawText(float x, float y, String text, Table.Cell cell) {
        try {
            context.contentStream.beginText();
            context.contentStream.setFont(cell.font, cell.fontSize);
            context.contentStream.newLineAtOffset(x, getPageHeight() - y);
            context.contentStream.showText(text);
            context.contentStream.endText();

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new CompilerFailure();
        }
    }

    class Table {
        private final float[] columnPositions;
        private final float[] columnWidths;
        private final float fullWidth;
        private final float padding = 5.f;
        private final List<Row> rows = new ArrayList<>();
        private Row actualRow;
        private int actualColumn;
        private Cell title;
        private boolean pageBreak;
        private Row headerRow = null;

        public Table(float pageWidth, float[] columnRatios) {
            float x = 0;
            columnPositions = new float[columnRatios.length + 1];
            columnWidths = new float[columnRatios.length];
            for (int i = 0; i < columnRatios.length; i++) {
                columnPositions[i] = x;
                columnWidths[i] = columnRatios[i] * pageWidth;
                x += columnWidths[i];
            }
            columnPositions[columnWidths.length] = x;
            fullWidth = x;
        }

        void setTitle(String text, boolean pageBreak) {
            this.pageBreak = pageBreak;
            try {
                title = new Cell();
                title.font = PDType1Font.HELVETICA_BOLD;
                title.fontSize = 24;
                title.cellHeight = 40;
                title.alignment = CENTER;
                title.lines = new ArrayList<>();
                float width = title.font.getStringWidth(text) / 1000 * title.fontSize;
                CellLine line = new CellLine();
                line.text = text;
                line.offsetY = padding + title.fontSize - (title.font.getFontDescriptor().getLeading() / 1000.f) * title.fontSize;
                line.offsetX = padding + (fullWidth - width) / 2;
                title.lines.add(line);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new CompilerFailure();
            }
        }

        protected void addRow(boolean header) {
            Row row = new Row();
            row.cells = new Cell[columnWidths.length];
            actualRow = row;
            if (header)
                headerRow = row;
            else
                rows.add(row);
            actualColumn = 0;
        }

        protected void addCell(String text) {
            addCell(text, 12, CENTER, true);
        }

        protected void addCell(String text, int alignment) {
            addCell(text, 12, alignment, true);
        }

        protected void addCell(String text, int alignment, boolean wordWrap) {
            addCell(text, 12, alignment, wordWrap);
        }

        protected void addCell(String text, int fontSize, int alignment, boolean wordWrap) {
            try {
                Cell cell = new Cell();
                actualRow.cells[actualColumn] = cell;
                float columnWidth = columnWidths[actualColumn] - 2 * padding;
                actualColumn++;

                cell.font = actualRow == headerRow ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA;
                cell.fontSize = fontSize;

                float textYOffset = +cell.fontSize - (cell.font.getFontDescriptor().getLeading() / 1000.f) * cell.fontSize;

                cell.lines = new ArrayList<>();

                String[] words = wordWrap?text.split("\\s"):new String[]{text};
                String lineText = null;
                for (String word : words) {
                    word = word.trim();
                    if (lineText == null) {
                        lineText = word;
                    } else {
                        String next = lineText + " " + word;
                        float width = cell.font.getStringWidth(next) / 1000 * cell.fontSize;
                        if (width < columnWidth) {
                            lineText = next;
                            continue;
                        }
                        width = cell.font.getStringWidth(lineText) / 1000 * cell.fontSize;
                        CellLine line = new CellLine();
                        line.text = lineText;
                        line.offsetY = padding + textYOffset;
                        textYOffset += cell.fontSize;
                        line.offsetX = padding + alignment * (columnWidth - width) / 2;
                        cell.lines.add(line);
                        lineText = word;
                    }
                }
                if (lineText != null) {
                    float width = cell.font.getStringWidth(lineText) / 1000 * cell.fontSize;

                    CellLine line = new CellLine();
                    line.text = lineText;
                    line.offsetY = padding + textYOffset;
                    textYOffset += cell.fontSize;
                    line.offsetX = padding + alignment * (columnWidth - width) / 2;
                    cell.lines.add(line);
                }

                float rowHeight = 2 * padding + cell.fontSize * cell.lines.size();
                cell.cellHeight = rowHeight;
                if (actualRow.height < rowHeight)
                    actualRow.height = rowHeight;
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new CompilerFailure();
            }
        }

        void renderHeader() {
            // draw title
            if (title != null) {
                drawText(
                        context.x + title.lines.get(0).offsetX,
                        context.y + title.lines.get(0).offsetY,
                        title.lines.get(0).text, title);
                context.y += title.cellHeight;
                context.height -= title.cellHeight;
            }
            // draw top line
            drawLine(context.x, context.y, context.x + fullWidth, context.y);
            if (headerRow != null)
                renderRow(headerRow);
        }

        void renderRow(Row row) {
            // draw borders
            for (float x : columnPositions)
                drawLine(context.x + x,
                        context.y,
                        context.x + x,
                        context.y + row.height);
            drawLine(context.x,
                    context.y + row.height,
                    context.x + fullWidth,
                    context.y + row.height);

            // draw content
            for (int column = 0; column < columnWidths.length; column++) {
                Cell cell = row.cells[column];
                if (cell == null)
                    continue;
                for (CellLine line : cell.lines) {
                    drawText(
                            context.x + columnPositions[column] + line.offsetX,
                            context.y + line.offsetY + (row.height - cell.cellHeight) / 2,
                            line.text, cell);
                }
            }

            // update context
            context.y += row.height;
            context.height -= row.height;

        }

        void render() {
            updateContext();

            // not at the start of the page?
            if (context.height < context.pageHeight) {
                if (title != null && pageBreak) {
                    context.pageBreak = true;
                    updateContext();
                } else {
                    context.height -= 20;
                    context.y += 20;
                }
            }

            // will fit on page?
            float totalHeight = 20;
            if (title != null)
                totalHeight += title.cellHeight;
            if (headerRow != null)
                totalHeight += headerRow.height;
            float oneRowHeight = totalHeight;
            boolean first = true;
            for (Row row : rows) {
                if (first) {
                    first = false;
                    oneRowHeight += row.height;
                }
                totalHeight += row.height;
            }
            // in any case: will at least one line fit?
            if (context.height < oneRowHeight) {
                context.pageBreak = true;
                updateContext();
            } else if (context.height < context.pageHeight && totalHeight > context.height) {
                context.pageBreak = true;
                updateContext();
            }

            // create outline item
            PDPageDestination dest = new PDPageFitWidthDestination();
            dest.setPage(context.page);
            PDOutlineItem bookmark = new PDOutlineItem();
            bookmark.setDestination(dest);
            bookmark.setTitle(title != null
                    ? title.lines.get(0).text
                    : getName());
            pagesOutline.addLast(bookmark);

            renderHeader();

            for (Row row : rows) {
                // will fit?
                if (context.height < row.height) {
                    context.pageBreak = true;
                    updateContext();
                    renderHeader();
                }

                renderRow(row);


            }


        }

        class Row {
            private float height = 12 + 2 * padding;
            private Cell[] cells;
        }

        class Cell {
            private List<CellLine> lines;
            private PDFont font;
            private float fontSize;
            private float cellHeight;
            private int alignment = 0;
        }

        class CellLine {
            String text;
            float offsetX;
            float offsetY;
        }

    }


}
