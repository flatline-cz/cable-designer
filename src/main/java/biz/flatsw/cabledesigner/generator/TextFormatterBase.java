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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public abstract class TextFormatterBase implements FormatterPlugin {
    protected StringBuilder output=null;
    private String currentPage;

    protected void setPage(String page) {
        if (page.equals(currentPage))
            return;
        if (currentPage != null)
            output.append("\n\n");
        currentPage = page;
        output.append("\n\n");
        output.append(page);
        output.append("\n");
    }


    @Override
    public Object getOutputObject() {
        return output;
    }

    @Override
    public boolean setOutputObject(Object object) {
        if(object instanceof StringBuilder) {
            output=(StringBuilder) object;
            return true;
        }
        output=new StringBuilder();
        return false;
    }

    @Override
    public InputStream getOutput() {
        return new ByteArrayInputStream(output.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String getExtension() {
        return "txt";
    }

    protected String fill(String text, int length) {
        return fill(text, length, false);
    }

    protected String fill(String text, int length, boolean right) {
        length--;
        while (text.length() < length)
            text = right ? (" " + text) : (text + " ");
        if (text.length() > length) {
            text = text.substring(0, text.length() - 2) + '…';
        }
        return text + " ";
    }

}