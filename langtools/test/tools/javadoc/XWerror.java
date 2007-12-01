/*
 * Copyright 2002 Sun Microsystems, Inc.  All Rights Reserved.
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
 * @bug 4099527
 * @summary javadoc tool: want flag to exit nonzero if there were warnings.
 * @author gafter
 * @run main XWerror
 */

import com.sun.javadoc.*;
import java.util.*;

public class XWerror extends Doclet
{
    public static void main(String[] args) {
        if (com.sun.tools.javadoc.Main.
            execute("javadoc", "XWerror",
                    new String[] {"-Xwerror",
                                  System.getProperty("test.src", ".") +
                                  java.io.File.separatorChar +
                                  "XWerror.java"}) == 0)
            throw new Error();
    }

    public static boolean start(com.sun.javadoc.RootDoc root) {
        root.printWarning(null, "warning message");
        return false;
    }
}
