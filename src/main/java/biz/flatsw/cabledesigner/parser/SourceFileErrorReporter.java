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

package biz.flatsw.cabledesigner.parser;

import java.io.BufferedReader;
import java.io.FileReader;

public class SourceFileErrorReporter {
    private final static int contextLines = 1;

    public static void showError(Location location, String message) {
        showError(location.getSourceFile(),
                location.getLine(), location.getColumn(),
                message);

    }

    public static void showSymbolReferenceError(Location location, String symbolType) {
        printError(location.getSourceFile(), location.getLine(), location.getColumn(),
                String.format("%s is not found", symbolType));
        throw new CompilerFailure();
    }

    public static void showSymbolRedefinitionError(Location location,
                                                   Symbol symbol,
                                                   String symbolType) {
        showSymbolDefinitionError(location, symbol,
                String.format("Redefinition of %s '%s'", symbolType, symbol.getName()));
    }

    public static void showSymbolDefinitionError(Location location,
                                                 Symbol symbol,
                                                 String message) {
        printError(location.getSourceFile(), location.getLine(), location.getColumn(),
                message);
        printError(symbol.getDefinitionLocation().getSourceFile(),
                symbol.getDefinitionLocation().getLine(),
                symbol.getDefinitionLocation().getColumn(),
                "Originally defined here");
        throw new CompilerFailure();
    }


    public static void showError(FileManager.InputFile inputFile, int line, int column,
                                 String message) {
        printError(inputFile, line, column, message);
        throw new CompilerFailure();
    }

    private static void printError(FileManager.InputFile inputFile, int line, int column,
                                   String message) {

        // compute first context line
        int firstLine = (line >= contextLines) ? (line - contextLines - 1) : 1;

        // report file
        System.err.printf("\nFile: %s, Line %d:%n", inputFile.getName(), line);

        // read context
        StringBuilder context = new StringBuilder();
        try {
            int i = 1;
            try (BufferedReader br = new BufferedReader(new FileReader(inputFile.getFile()))) {
                for (String lineText; (lineText = br.readLine()) != null; i++) {
                    if (i == line) {
                        context.append(lineText);
                        context.append('\n');
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            System.err.printf("file: %s, line %d, column %d: %s%n",
                    inputFile.getName(), line, column, message);
        }
        if (column > 1)
            for (int i = 0; i < column; i++)
                context.append(' ');
        context.append("^--- ");
        context.append(message);
        context.append("\n");
        System.err.print(context);
    }
}
