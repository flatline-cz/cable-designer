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

package biz.flatsw.cabledesigner.generator.wiring;

import biz.flatsw.cabledesigner.generator.XLSXFormatterBase;
import biz.flatsw.cabledesigner.parser.CompilerFailure;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class XLSFormatter extends XLSXFormatterBase implements Wiring.Formatter {
    private XSSFSheet sheet;
    private Section section = null;
    private String group = null;
    private XSSFCellStyle titleStyle;
    private XSSFCellStyle headerStyle;
    private XSSFCellStyle codeStyle;
    private int rowIndex;

    @Override
    public void formatSignal(String signalName) {
        setSection(Section.WIRES);
        if (rowIndex != 0)
            rowIndex++;
        int row = rowIndex;
        createHeaderRow(String.format("Wiring of '%s'", signalName));
        sheet.getRow(row).getCell(0).setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(row, row, 0, 6));
        createHeaderRow("Type", "Pin/Wire part number", "Seal part number", "Wire length", "Wire cross section", "Wire color", "Note");
    }

    @Override
    public void formatPin(String connectorName, String pinName,
                          String pinPartNumber, String sealPartNumber,
                          String wireJoint) {
        setSection(Section.WIRES);
        String note = String.format("Inserted into %s/%s", connectorName, pinName);
        if (wireJoint != null)
            note += ". " + wireJoint;
        createDataRow("pin",
                pinPartNumber, sealPartNumber,
                null, null, null,
                note);
    }

    @Override
    public void formatWire(int length, String colorCode, String partNumber, float crossSection, String pathNodes) {
        setSection(Section.WIRES);
        createDataRow("wire",
                partNumber, null,
                length, String.format("%.2fmm²", crossSection), colorCode,
                (pathNodes != null && !pathNodes.isEmpty())
                        ? String.format(String.format("Through %s", pathNodes))
                        : null);
    }

    @Override
    public void formatConnector(String name) {
        setSection(Section.CONNECTORS);
        if (rowIndex != 0)
            rowIndex++;
        int row = rowIndex;
        createHeaderRow(String.format("Connector '%s'", name));
        sheet.getRow(row).getCell(0).setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(row, row, 0, 3));
        createHeaderRow("Component", "Part number", "Quantity", "Note");
    }

    @Override
    public void formatConnectorComponent(String type, String partNumber, int qty) {
        setSection(Section.CONNECTORS);
        createDataRow(type, partNumber, qty, null);
    }

    @Override
    public void formatConnectorCavity(String partNumber, String pinName) {
        setSection(Section.CONNECTORS);
        createDataRow("cavity plug", partNumber, 1,
                String.format("at position %s", pinName));
    }

    @Override
    public InputStream getOutput() {
        setSection(null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            workbook.write(baos);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new CompilerFailure();
        }
        return new ByteArrayInputStream(baos.toByteArray());
    }


    private void setSection(Section section) {
        // nothing initialized?
        if (this.headerStyle == null) {
            Font defaultFont = workbook.createFont();
            defaultFont.setFontHeightInPoints((short) 10);
            defaultFont.setFontName(XSSFFont.DEFAULT_FONT_NAME);
            defaultFont.setBold(false);

            Font headerFont = workbook.createFont();
            headerFont.setFontHeightInPoints((short) 10);
            headerFont.setFontName(XSSFFont.DEFAULT_FONT_NAME);
            headerFont.setBold(true);

            Font titleFont = workbook.createFont();
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setFontName(XSSFFont.DEFAULT_FONT_NAME);
            titleFont.setBold(true);

            headerStyle = workbook.createCellStyle();
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setFont(headerFont);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            titleStyle = workbook.createCellStyle();
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            titleStyle.setFont(titleFont);
//            titleStyle.setBorderBottom(BorderStyle.THICK);
//            titleStyle.setBorderTop(BorderStyle.THICK);
//            titleStyle.setBorderLeft(BorderStyle.THIN);
//            titleStyle.setBorderRight(BorderStyle.THIN);

            codeStyle = workbook.createCellStyle();
            codeStyle.setAlignment(HorizontalAlignment.LEFT);
            codeStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            codeStyle.setFont(defaultFont);
            codeStyle.setBorderBottom(BorderStyle.THIN);
            codeStyle.setBorderTop(BorderStyle.THIN);
            codeStyle.setBorderLeft(BorderStyle.THIN);
            codeStyle.setBorderRight(BorderStyle.THIN);
        }

        // close existing section?
        if (this.section != null) {
            switch (this.section) {
                case WIRES:
                    sheet.autoSizeColumn(0);
                    sheet.autoSizeColumn(1);
                    sheet.autoSizeColumn(2);
                    sheet.autoSizeColumn(3);
                    sheet.autoSizeColumn(4);
                    sheet.autoSizeColumn(5);
                    sheet.autoSizeColumn(6);
                    break;
                case CONNECTORS:
                    sheet.autoSizeColumn(0);
                    sheet.autoSizeColumn(1);
                    sheet.autoSizeColumn(2);
                    sheet.autoSizeColumn(3);
                    break;
            }
        }

        // create a new section?
        if (section != null && section != this.section) {
            switch (section) {
                case WIRES:
                    sheet = workbook.createSheet("SOP - Wiring");
                    rowIndex = 0;
                    break;
                case CONNECTORS:
                    sheet = workbook.createSheet("SOP - Connectors");
                    rowIndex = 0;
                    break;
            }
        }
        this.section = section;
    }

    private void createHeaderRow(String... names) {
        XSSFRow row = sheet.getRow(rowIndex);
        if (row == null)
            row = sheet.createRow(rowIndex);
        rowIndex++;
        int column = 0;
        for (String name : names) {
            if (name != null) {
                XSSFCell cell = row.createCell(column);
                cell.setCellStyle(headerStyle);
                cell.setCellValue(name);
            }
            column++;
        }
    }

    private void createDataRow(Object... values) {
        XSSFRow row = sheet.getRow(rowIndex);
        if (row == null)
            row = sheet.createRow(rowIndex);
        rowIndex++;
        int column = 0;
        for (Object value : values) {
            if (value instanceof String) {
                XSSFCell cell = row.createCell(column);
                cell.setCellStyle(codeStyle);
                cell.setCellValue((String) value);
            }
            if (value instanceof Number) {
                XSSFCell cell = row.createCell(column);
                cell.setCellStyle(codeStyle);
                cell.setCellValue(((Number) value).intValue());
            }
            if (value == null) {
                XSSFCell cell = row.createCell(column);
                cell.setCellStyle(codeStyle);
                cell.setBlank();
            }
            column++;
        }
    }

    private enum Section {
        WIRES, CONNECTORS
    }

}
