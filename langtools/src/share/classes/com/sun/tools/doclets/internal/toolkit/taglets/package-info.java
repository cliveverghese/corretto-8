/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
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

/**
    This package has classes used to generate Javadoc tag documentation.
    Doclets no longer have to implement its own version of standard tags
    such as &#64;param and &#64;throws.  This is the single, doclet
    implementation of each standard tag that is shared by all
    doclets.  Each doclet must have a taglet writer that takes a taglet
    as input and writes doclet-dependent output. The taglet itself will
    do the tag processing. For example, suppose we are outputing
    &#64;throws tags. The taglet would:
    <ul>
        <li> Retrieve the list of throws tags to be documented.
        <li> Replace {&#64;inheritDoc} with the appropriate documentation.
        <li> Add throws documentation for exceptions that are declared in
             the signature of the method but
             not documented with the throws tags.
    </ul>
    After doing the steps above, the taglet would pass the information to
    the taglet writer for writing. The taglets are essentially builders for
    tags.

    <p><b>This is NOT part of any supported API.
    If you write code that depends on this, you do so at your own risk.
    This code and its internal interfaces are subject to change or
    deletion without notice.</b>
*/
@jdk.Exported(false)
package com.sun.tools.doclets.internal.toolkit.taglets;
