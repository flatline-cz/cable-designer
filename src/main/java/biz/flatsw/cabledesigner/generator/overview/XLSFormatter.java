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

package biz.flatsw.cabledesigner.generator.overview;

import biz.flatsw.cabledesigner.generator.XLSXFormatterBase;
import biz.flatsw.cabledesigner.model.defs.ConnectorGender;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.InputStream;

public class XLSFormatter extends XLSXFormatterBase implements Overview.Formatter {
    private XSSFSheet sheet;
    private Section section = null;
    private String group=null;
    private XSSFCellStyle headerStyle;
    private XSSFCellStyle codeStyle;
    private int rowIndex;




    @Override
    public void formatConnector(String connectorName,
                                String connectorDescription,
                                ConnectorGender gender, int pinCount,
                                String partNumbers) {
        setSection(Section.CONNECTORS);
        createDataRow(
                connectorName,
                connectorDescription,
                gender.name().toLowerCase(),
                pinCount,
                partNumbers);
    }

    @Override
    public void formatConnectorSignal(String connectorName, String pinName,
                                      String signalName, String signalDescription,
                                      String wireDescription) {
        setSection(Section.CONNECTOR_SIGNALS);
        if(group!=null && !group.equals(connectorName))
            rowIndex++;
        group=connectorName;
        createDataRow(
                connectorName, pinName,
                signalName, signalDescription,
//                String.format("%.2fmm²", crossSection),
                wireDescription);
    }

    @Override
    public void formatConnectorSignal(String connectorName, String pinName) {
        setSection(Section.CONNECTOR_SIGNALS);
        if(group!=null && !group.equals(connectorName))
            rowIndex++;
        group=connectorName;
        createDataRow(
                connectorName, pinName,
                "n.c.", "--- Not connected ---", null, null);
    }

    @Override
    public InputStream getOutput() {
        setSection(null);
        return super.getOutput();
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

            headerStyle = workbook.createCellStyle();
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setFont(headerFont);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            codeStyle = workbook.createCellStyle();
            codeStyle.setAlignment(HorizontalAlignment.CENTER);
            codeStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            codeStyle.setFont(defaultFont);
            codeStyle.setWrapText(true);
            codeStyle.setBorderBottom(BorderStyle.THIN);
            codeStyle.setBorderTop(BorderStyle.THIN);
            codeStyle.setBorderLeft(BorderStyle.THIN);
            codeStyle.setBorderRight(BorderStyle.THIN);
        }

        // close existing section?
        if (this.section != null) {
            switch (this.section) {
                case SIGNALS:
                    sheet.autoSizeColumn(0);
                    sheet.setColumnWidth(1, 10000);
                    sheet.autoSizeColumn(2);
                    sheet.setColumnWidth(3, 8000);
                    break;
                case CONNECTORS:
                    sheet.autoSizeColumn(0);
                    sheet.autoSizeColumn(1);
                    sheet.autoSizeColumn(2);
                    sheet.autoSizeColumn(3);
                    sheet.autoSizeColumn(4);
                    break;
                case CONNECTOR_SIGNALS:
                    sheet.autoSizeColumn(0);
                    sheet.autoSizeColumn(1);
                    sheet.autoSizeColumn(2);
                    sheet.autoSizeColumn(3);
                    sheet.autoSizeColumn(4);
                    sheet.autoSizeColumn(5);
                    break;
            }
        }

        // create a new section?
        if (section != null && section!=this.section) {
            switch (section) {
                case SIGNALS:
                    sheet = workbook.createSheet("Overview - Signals");
                    createHeaderRow("Signal", "Description", "Wire", "Connectors");
                    rowIndex = 2;
                    break;
                case CONNECTORS:
                    sheet = workbook.createSheet("Overview - Connectors");
                    createHeaderRow("Connector", "Description", "Gender", "Pins", "Part number");
                    rowIndex = 2;
                    break;
                case CONNECTOR_SIGNALS:
                    sheet = workbook.createSheet("Overview - Connector signals");
                    createHeaderRow("Connector", "Pin", "Signal", "Description", "Wire cross section", "Wire color");
                    rowIndex = 2;
                    group=null;
                    break;
            }
        }
        this.section=section;
    }

    private void createHeaderRow(String... names) {
        XSSFRow row = sheet.getRow(0);
        if(row==null)
            row=sheet.createRow(0);
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
        if(row==null)
            row=sheet.createRow(rowIndex);
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
            if(value==null) {
                XSSFCell cell = row.createCell(column);
                cell.setCellStyle(codeStyle);
                cell.setBlank();
            }
            column++;
        }
    }

    private enum Section {
        SIGNALS, CONNECTORS, CONNECTOR_SIGNALS
    }

}
