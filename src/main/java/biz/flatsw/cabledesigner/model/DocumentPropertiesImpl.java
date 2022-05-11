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

package biz.flatsw.cabledesigner.model;

public class DocumentPropertiesImpl implements DocumentProperties {
    private String project;
    private String company;
    private String harness;
    private String revision;


    @Override
    public String getProject() {
        return project;
    }

    @Override
    public void setProject(String project) {
        this.project = project;
    }

    @Override
    public String getCompany() {
        return company;
    }

    @Override
    public void setCompany(String company) {
        this.company = company;
    }

    @Override
    public String getHarness() {
        return harness;
    }

    @Override
    public void setHarness(String harness) {
        this.harness = harness;
    }

    @Override
    public String getRevision() {
        return revision;
    }

    @Override
    public void setRevision(String revision) {
        this.revision = revision;
    }
}
