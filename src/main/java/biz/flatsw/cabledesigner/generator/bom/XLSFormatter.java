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

package biz.flatsw.cabledesigner.generator.bom;

import biz.flatsw.cabledesigner.generator.XLSXFormatterBase;
import biz.flatsw.cabledesigner.generator.overview.Overview;
import biz.flatsw.cabledesigner.model.defs.ConnectorGender;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.*;

import java.io.InputStream;

public class XLSFormatter extends XLSXFormatterBase implements BOM.Formatter {
    private XSSFSheet sheet;
    private Section section = null;
    private String group=null;
    private XSSFCellStyle headerStyle;
    private XSSFCellStyle codeStyle;
    private int rowIndex;



    @Override
    public void formatMaterialLine(String type, String partNumber, String units, int qty) {
        setSection(Section.BOM);
        if(group!=null && !group.equals(type))
            rowIndex++;
        group=type;
        createDataRow(type, partNumber, qty, units);
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
                case BOM:
                    sheet.autoSizeColumn(0);
                    sheet.autoSizeColumn(1);
                    sheet.autoSizeColumn(2);
                    sheet.autoSizeColumn(3);
                    break;
            }
        }

        // create a new section?
        if (section != null && section!=this.section) {
            switch (section) {
                case BOM:
                    sheet = workbook.createSheet("BOM");
                    createHeaderRow("Type", "Part number", "Quantity", "Units");
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
        BOM
    }

}
