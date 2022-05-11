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
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public abstract class PDFFormatterBase implements FormatterPlugin {
    static {
        System.setProperty("org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.NoOpLog");
    }

    protected PDDocument document;
    protected PDDocumentOutline documentOutline;

    @Override
    public String getExtension() {
        return "pdf";
    }

    @Override
    public Object getOutputObject() {
        return document;
    }

    @Override
    public boolean setOutputObject(Object object) {
        if (object instanceof PDDocument) {
            document = (PDDocument) object;
            documentOutline=document.getDocumentCatalog().getDocumentOutline();
            return true;
        }
        document = new PDDocument();
        documentOutline = new PDDocumentOutline();
        document.getDocumentCatalog().setDocumentOutline(documentOutline);
        return false;
    }

    protected float fromCentimeters(float value) {
        return value * 72.f / 2.54f;
    }

    @Override
    public InputStream getOutput() {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            document.save(output);
            document.close();
            return new ByteArrayInputStream(output.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new CompilerFailure();
        }
    }
}
