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

import biz.flatsw.cabledesigner.tree.Tree;
import biz.flatsw.cabledesigner.tree.TreeImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileManagerImpl implements FileManager {
    private final List<File> roots=new ArrayList<>();
    private Tree<InputFile> files=null;

    @Override
    public void addRoot(File directory) {
        // check root existence
        try {
            File root = directory.getCanonicalFile();
            if (!root.exists()) {
                System.err.printf("Specified definition root '%s' doesn't exist%n", root.toString());
                throw new CompilerFailure();
            }
            if (!roots.contains(root))
                roots.add(root);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new CompilerFailure();
        }
    }

    @Override
    public Tree<InputFile> getFiles() {
        return files;
    }

    @Override
    public InputFile addFile(InputFile parentFile, String fileName) {
        // check file existence
        File file=null;
        for(File root : roots) {
            File f = new File(root, fileName);
            if (f.exists() && f.isFile()) {
                file = f;
                break;
            }
        }
        if(file==null) {
            System.err.printf("Requested file '%s' doesn't exist%n", fileName);
            throw new CompilerFailure();
        }

        // create record
        FileEntryImpl fileEntry=new FileEntryImpl(fileName, file);

        // add to the tree
        if(parentFile==null) {
            if(files!=null) {
                System.err.printf("Can't process '%s' as main file, '%s' is already the stating point%n",
                        file,
                        files.getData().getFile().toString());
                throw new CompilerFailure();
            }
            files=new TreeImpl<>(null, fileEntry);
        } else {
            // parent entry exists?
            TreeImpl<InputFile> parentEntry=(TreeImpl<InputFile>) files.find(parentFile);
            if(parentEntry==null) {
                System.err.printf("Can't find parent file '%s' for file '%s'%n", parentFile.getFile(), file);
                throw new CompilerFailure();
            }

            // check cyclic dependence
            for(Tree<InputFile> e=parentEntry;e!=null;e=e.getParent())
                if(e.getData().equals(fileEntry)) {
                    System.err.printf("Cyclic inclusion of '%s'%n", file);
                    throw new CompilerFailure();
                }
            new TreeImpl<>(parentEntry, fileEntry);
        }

        return fileEntry;
    }

    public static class FileEntryImpl implements FileManager.InputFile {
        private final String name;
        private final File file;

        public FileEntryImpl(String name, File file) {
            this.name = name;
            this.file = file;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof FileEntryImpl)
                return file.equals(((FileEntryImpl) obj).file);
            return false;
        }

        @Override
        public File getFile() {
            return file;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public int compareTo(InputFile o) {
            return name.compareTo(o.getName());
        }
    }
}
