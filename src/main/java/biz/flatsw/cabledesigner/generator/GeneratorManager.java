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

import biz.flatsw.cabledesigner.CableDesigner;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GeneratorManager implements Generator {
    private File directory;
    private String definitionFileName;
    private String format;
    private final Set<String> suffixes=new HashSet<>();
    private final Set<String> selectedGenerators=new HashSet<>();
    private final Set<String> excludedGenerators=new HashSet<>();

    @Override
    public void setOutput(File directory, String definitionFileName, String format) {
        this.directory = directory;
        this.definitionFileName = definitionFileName;
        this.format = format;
    }

    public void setGenerators(Set<String> selectedGenerators, Set<String> excludedGenerators) {
        this.selectedGenerators.clear();
        this.selectedGenerators.addAll(selectedGenerators.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList()));
        this.excludedGenerators.clear();
        this.excludedGenerators.addAll(excludedGenerators.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList()));
    }

    @Override
    public void generate() {
        // find all generators
        Reflections query = new Reflections(getClass().getPackageName());
        List<GeneratorPlugin> plugins = query
                .get(Scanners.SubTypes.of(GeneratorPlugin.class).asClass())
                .stream()
                .filter(p -> !p.equals(GeneratorManager.class))
                .filter(p -> !excludedGenerators.contains(p.getSimpleName().toLowerCase()))
                .filter(p -> selectedGenerators.isEmpty() || selectedGenerators.contains(p.getSimpleName().toLowerCase()))
                .map(cls -> {
                    try {
                        return (GeneratorPlugin) cls.getDeclaredConstructor().newInstance();
                    } catch (Exception ex) {
                        return (GeneratorPlugin) null;
                    }
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(GeneratorPlugin::getPriority))
                .collect(Collectors.toList());

        // process each plugin one by one
        Object context = null;
        GeneratorPlugin lastPlugin = null;
        boolean merged=false;
        for (GeneratorPlugin plugin : plugins) {
            // configure output
            plugin.setOutput(directory, definitionFileName, format);
            // can pass object?
            if (!plugin.setOutputObject(context)) {
                // finalize previous output
                if (lastPlugin != null && context != null) {
                    lastPlugin.finish(getSuffix(lastPlugin, merged));
                    merged=false;
                }
            } else {
                merged=true;
            }
            // execute plugin
            CableDesigner.printMessage(String.format("Output plugin: %s...", plugin.getClass().getSimpleName()));
            plugin.generate();
            // pass context to the next plugin?
            lastPlugin = plugin;
            context = plugin.getOutputObject();
            if (context == null) {
                lastPlugin.finish(getSuffix(lastPlugin, merged));
                lastPlugin=null;
                merged=false;
            }
        }
        if (lastPlugin != null && context != null) {
            lastPlugin.finish(getSuffix(lastPlugin, merged));
        }
    }

    private String getSuffix(GeneratorPlugin plugin, boolean merged) {
        if(merged) {
            for(int i=0;;i++) {
                String name=(i==0)?"":("-"+i);
                if(suffixes.contains(name))
                    continue;
                suffixes.add(name);
                return name;
            }
        } else
            return "-"+plugin.getClass().getSimpleName();
    }

}
