/*
 * Copyright 1997-2002 Sun Microsystems, Inc.  All Rights Reserved.
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
package javax.swing.text;

import java.io.Serializable;

/**
 * This class encapsulates a single tab stop (basically as tab stops
 * are thought of by RTF). A tab stop is at a specified distance from the
 * left margin, aligns text in a specified way, and has a specified leader.
 * TabStops are immutable, and usually contained in TabSets.
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running
 * the same version of Swing.  As of 1.4, support for long term storage
 * of all JavaBeans<sup><font size="-2">TM</font></sup>
 * has been added to the <code>java.beans</code> package.
 * Please see {@link java.beans.XMLEncoder}.
 *
 */
public class TabStop implements Serializable {

    /** Character following tab is positioned at location. */
    public static final int ALIGN_LEFT    = 0;
    /** Characters following tab are positioned such that all following
     * characters up to next tab/newline end at location. */
    public static final int ALIGN_RIGHT   = 1;
    /** Characters following tab are positioned such that all following
     * characters up to next tab/newline are centered around the tabs
     * location. */
    public static final int ALIGN_CENTER  = 2;
    /** Characters following tab are aligned such that next
     * decimal/tab/newline is at the tab location, very similar to
     * RIGHT_TAB, just includes decimal as additional character to look for.
     */
    public static final int ALIGN_DECIMAL = 4;
    public static final int ALIGN_BAR     = 5;

    /* Bar tabs (whatever they are) are actually a separate kind of tab
       in the RTF spec. However, being a bar tab and having alignment
       properties are mutually exclusive, so the reader treats barness
       as being a kind of alignment. */

    public static final int LEAD_NONE      = 0;
    public static final int LEAD_DOTS      = 1;
    public static final int LEAD_HYPHENS   = 2;
    public static final int LEAD_UNDERLINE = 3;
    public static final int LEAD_THICKLINE = 4;
    public static final int LEAD_EQUALS    = 5;

    /** Tab type. */
    private int alignment;
    /** Location, from the left margin, that tab is at. */
    private float position;
    private int leader;

    /**
     * Creates a tab at position <code>pos</code> with a default alignment
     * and default leader.
     */
    public TabStop(float pos) {
        this(pos, ALIGN_LEFT, LEAD_NONE);
    }

    /**
     * Creates a tab with the specified position <code>pos</code>,
     * alignment <code>align</code> and leader <code>leader</code>.
     */
    public TabStop(float pos, int align, int leader) {
        alignment = align;
        this.leader = leader;
        position = pos;
    }

    /**
     * Returns the position, as a float, of the tab.
     * @return the position of the tab
     */
    public float getPosition() {
        return position;
    }

    /**
     * Returns the alignment, as an integer, of the tab.
     * @return the alignment of the tab
     */
    public int getAlignment() {
        return alignment;
    }

    /**
     * Returns the leader of the tab.
     * @return the leader of the tab
     */
    public int getLeader() {
        return leader;
    }

    /**
     * Returns true if the tabs are equal.
     * @return true if the tabs are equal, otherwise false
     */
    public boolean equals(Object other)
    {
        if (other == this) {
            return true;
        }
        if (other instanceof TabStop) {
            TabStop o = (TabStop)other;
            return ( (alignment == o.alignment) &&
                     (leader == o.leader) &&
                     (position == o.position) );  /* TODO: epsilon */
        }
        return false;
    }

    /**
     * Returns the hashCode for the object.  This must be defined
     * here to ensure 100% pure.
     *
     * @return the hashCode for the object
     */
    public int hashCode() {
        return alignment ^ leader ^ Math.round(position);
    }

    /* This is for debugging; perhaps it should be removed before release */
    public String toString() {
        String buf;
        switch(alignment) {
          default:
          case ALIGN_LEFT:
            buf = "";
            break;
          case ALIGN_RIGHT:
            buf = "right ";
            break;
          case ALIGN_CENTER:
            buf = "center ";
            break;
          case ALIGN_DECIMAL:
            buf = "decimal ";
            break;
          case ALIGN_BAR:
            buf = "bar ";
            break;
        }
        buf = buf + "tab @" + String.valueOf(position);
        if (leader != LEAD_NONE)
            buf = buf + " (w/leaders)";
        return buf;
    }
}
