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
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ServiceLoader;

public abstract class GeneratorBase<T extends FormatterPlugin> implements GeneratorPlugin {
    private File directory;
    private String filePrefix;
    private String format;
    protected T formatter;

    @Override
    public void setOutput(File directory, String filename, String format) {
        this.directory = directory;
        int i = filename.lastIndexOf('.');
        this.filePrefix = i > 0 ? filename.substring(0, i) : filename;
        this.format = format;
        this.formatter = findFormatter();
    }

    protected OutputStream getOutputFile(String suffix, String extension) {
        // compose name
        File file = new File(directory, String.format("%s%s.%s", filePrefix, suffix, extension));
        try {
            return Files.newOutputStream(file.toPath());
        } catch (Exception ex) {
            throw new CompilerFailure();
        }
    }

    @Override
    public Object getOutputObject() {
        return formatter.getOutputObject();
    }

    @Override
    public boolean setOutputObject(Object object) {
        return formatter.setOutputObject(object);
    }

    @Override
    public void finish(String suffix) {
        // store output
        copyStreams(formatter.getOutput(),
                getOutputFile(suffix, formatter.getExtension()));
    }

    protected abstract void generateContent();

    @Override
    public final void generate() {
        generateContent();
        if(formatter instanceof FinishingFormatter)
            ((FinishingFormatter) formatter).finish();
    }

    protected void copyStreams(InputStream input, OutputStream output) {
        byte[] buffer = new byte[1024];
        try {
            while (true) {
                int len = input.read(buffer);
                if (len < 1)
                    break;
                output.write(buffer, 0, len);
            }
            output.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void execCommand(InputStream input, OutputStream output, String... cmd)
            throws Exception {
        Process process = Runtime.getRuntime().exec(cmd);
        OutputStream stdin = process.getOutputStream();
        InputStream stdout = process.getInputStream();

        new Thread(() -> {
            copyStreams(input, stdin);
        }).start();

        new Thread(() -> {
            copyStreams(stdout, output);
        }).start();

        process.waitFor();
    }

    protected abstract Class<T> getFormatterClass();

    private T findFormatter() {
        Reflections query = new Reflections(getClass().getPackageName());
        T firstFound = null;
        for (Class<?> foundClass : query.get(Scanners.SubTypes.of(getFormatterClass()).asClass())) {
            // create formatter
            try {
                T formatter = (T) foundClass.getDeclaredConstructor().newInstance();
                if (formatter.getExtension().equals(format))
                    return formatter;
                if (firstFound == null)
                    firstFound = formatter;
            } catch (Exception ex) {
                // ignore
            }
        }
        return firstFound;
    }
}
