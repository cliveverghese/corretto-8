/*
 * Copyright 2006-2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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

/*
 * @test
 * @bug 6402062 6487891
 * @summary Tests BevelBorder encoding
 * @author Sergey Malenkov
 */

import java.awt.Color;
import javax.swing.border.BevelBorder;

public final class javax_swing_border_BevelBorder extends AbstractTest<BevelBorder> {
    public static void main(String[] args) {
        new javax_swing_border_BevelBorder().test(true);
    }

    protected BevelBorder getObject() {
        return new BevelBorder(BevelBorder.RAISED, Color.RED, Color.GREEN, Color.BLUE, Color.WHITE);
    }

    protected BevelBorder getAnotherObject() {
        return null; // TODO: could not update property
        // return new BevelBorder(BevelBorder.LOWERED);
    }
}
