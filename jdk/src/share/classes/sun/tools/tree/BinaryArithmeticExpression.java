/*
 * Copyright (c) 1994, 2003, Oracle and/or its affiliates. All rights reserved.
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

package sun.tools.tree;

import sun.tools.java.*;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class BinaryArithmeticExpression extends BinaryExpression {
    /**
     * constructor
     */
    public BinaryArithmeticExpression(int op, long where, Expression left, Expression right) {
        super(op, where, left.type, left, right);
    }

    /**
     * Select the type
     */
    void selectType(Environment env, Context ctx, int tm) {
        if ((tm & TM_DOUBLE) != 0) {
            type = Type.tDouble;
        } else if ((tm & TM_FLOAT) != 0) {
            type = Type.tFloat;
        } else if ((tm & TM_LONG) != 0) {
            type = Type.tLong;
        } else {
            type = Type.tInt;
        }
        left = convert(env, ctx, type, left);
        right = convert(env, ctx, type, right);
    }
}
