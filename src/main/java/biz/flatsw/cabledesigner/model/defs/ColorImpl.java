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

package biz.flatsw.cabledesigner.model.defs;

public class ColorImpl implements Color {
    private final SingleColor[] colors;

    public ColorImpl(String[] colorNames) {
        this.colors = new SingleColor[colorNames.length];
        for(int i=0;i<colorNames.length;i++)
            colors[i]=SingleColorImpl.getByName(colorNames[i]);
    }

    public ColorImpl(String colorName) {
        colors=new SingleColor[]{SingleColorImpl.getByName(colorName)};
    }

    @Override
    public String getCodes() {
        StringBuilder ret=new StringBuilder();
        boolean first=true;
        for(SingleColor color : colors) {
            if(first)
                first=false;
            else
                ret.append('/');
            ret.append(color.getCode());
        }
        return ret.toString();
    }

    @Override
    public String getNames() {
        StringBuilder ret=new StringBuilder();
        boolean first=true;
        for(SingleColor color : colors) {
            if(first)
                first=false;
            else
                ret.append('/');
            ret.append(color.getName());
        }
        return ret.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Color) && ((Color)obj).getCodes().equals(getCodes());
    }

    enum SingleColorImpl implements SingleColor {
        red("RD"),
        blue("BU"),
        green("GR"),
        white("WT"),
        black("BK"),
        gray("GY"),
        yellow("YE"),
        orange("OG"),
        brown("BR"),
        violet("VI"),
        pink("PK"),
        ;

        private final String code;

        SingleColorImpl(String code) {
            this.code = code;
        }

        @Override
        public String getName() {
            return name();
        }

        @Override
        public String getCode() {
            return code;
        }

        static SingleColor getByName(String name) {
            for(SingleColorImpl color : values()) {
                if(color.getName().equals(name))
                    return color;
            }
            return null;
        }
    }
}
