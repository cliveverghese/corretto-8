/*
 * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */
package com.sun.media.sound;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.SoundbankResource;

/**
 * Soundfont layer.
 *
 * @author Karl Helgason
 */
public class SF2Layer extends SoundbankResource {

    protected String name = "";
    protected SF2GlobalRegion globalregion = null;
    protected List<SF2LayerRegion> regions = new ArrayList<SF2LayerRegion>();

    public SF2Layer(SF2Soundbank soundBank) {
        super(soundBank, null, null);
    }

    public SF2Layer() {
        super(null, null, null);
    }

    public Object getData() {
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SF2LayerRegion> getRegions() {
        return regions;
    }

    public SF2GlobalRegion getGlobalRegion() {
        return globalregion;
    }

    public void setGlobalZone(SF2GlobalRegion zone) {
        globalregion = zone;
    }

    public String toString() {
        return "Layer: " + name;
    }
}
