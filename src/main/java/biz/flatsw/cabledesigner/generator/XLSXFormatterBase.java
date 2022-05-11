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
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class XLSXFormatterBase implements FormatterPlugin {
    protected XSSFWorkbook workbook=null;

    @Override
    public Object getOutputObject() {
        return workbook;
    }

    @Override
    public boolean setOutputObject(Object object) {
        if(object instanceof XSSFWorkbook) {
            workbook=(XSSFWorkbook) object;
            return true;
        }
        workbook=new XSSFWorkbook();
        return false;
    }

    @Override
    public InputStream getOutput() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            workbook.write(baos);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new CompilerFailure();
        }
        return new ByteArrayInputStream(baos.toByteArray());
    }

    @Override
    public String getExtension() {
        return "xlsx";
    }



}
