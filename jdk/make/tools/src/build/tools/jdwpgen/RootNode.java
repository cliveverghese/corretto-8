/*
 * Copyright (c) 1998, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package build.tools.jdwpgen;

import java.util.*;
import java.io.*;

class RootNode extends AbstractNamedNode {

    void constrainComponent(Context ctx, Node node) {
        if (node instanceof CommandSetNode ||
                    node instanceof ConstantSetNode) {
            node.constrain(ctx);
        } else {
            error("Expected 'CommandSet' item, got: " + node);
        }
    }

    void document(PrintWriter writer) {
        writer.println("<html><head><title>" + comment() + "</title></head>");
        writer.println("<body bgcolor=\"white\">");
        for (Node node : components) {
            node.documentIndex(writer);
        }
        for (Node node : components) {
            node.document(writer);
        }
        writer.println("</body></html>");
    }

    void genJava(PrintWriter writer, int depth) {
        writer.println("package com.sun.tools.jdi;");
        writer.println();
        writer.println("import com.sun.jdi.*;");
        writer.println("import java.util.*;");
        writer.println();

        genJavaClass(writer, depth);
    }
}
