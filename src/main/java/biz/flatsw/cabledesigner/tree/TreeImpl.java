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

package biz.flatsw.cabledesigner.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class TreeImpl<T extends Comparable<T>> implements Tree<T> {
    private final TreeImpl<T> parent;
    private final T data;
    private final List<TreeImpl<T>> children=new ArrayList<>();

    public TreeImpl(TreeImpl<T> parent, T data) {
        this.parent=parent;
        this.data=data;
        if(parent!=null)
            parent.children.add(this);
    }

    public Tree<T> getParent() {
        return parent;
    }

    public T getData() {
        return data;
    }

    public List<Tree<T>> getChildren() {
        return Collections.unmodifiableList(children);
    }


    public Tree<T> find(T data) {
        if(data.equals(this.data))
            return this;
        return children
                .stream()
                .map(child -> child.find(data))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Tree<T> findDirectChild(Predicate<T> predicate) {
        return children
                .stream()
                .filter(data -> predicate.test(data.getData()))
                .findFirst()
                .orElse(null);
    }
}
