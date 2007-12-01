/*
 * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
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
 * @bug 4222827 4506596 6538853
 * @summary Tests PropertyEditor for value of type Font
 * @author Sergey Malenkov
 */

import java.awt.Font;

public class TestFontClassValue {
    public static void main(String[] args) {
        TestEditor test = new TestEditor(Font.class);
        test.testValue(new Font("Helvetica", Font.BOLD | Font.ITALIC, 20), "Helvetica BOLDITALIC 20");
        test.testValue(null, null);
        test.testText("Helvetica 12", new Font("Helvetica", Font.PLAIN, 12));
        test.testText(null, null);
    }
}
