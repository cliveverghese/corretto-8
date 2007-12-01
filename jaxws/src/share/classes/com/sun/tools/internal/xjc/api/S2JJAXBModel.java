/*
 * Copyright 2006 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.tools.internal.xjc.api;

import java.util.Collection;

import javax.xml.namespace.QName;

import com.sun.codemodel.internal.CodeWriter;
import com.sun.codemodel.internal.JCodeModel;
import com.sun.tools.internal.xjc.Options;
import com.sun.tools.internal.xjc.Plugin;

/**
 * {@link JAXBModel} that exposes additional information available
 * only for the schema->java direction.
 *
 * @author Kohsuke Kawaguchi
 */
public interface S2JJAXBModel extends JAXBModel {

    /**
     * Gets a {@link Mapping} object for the given global element.
     *
     * @return
     *      null if the element name is not a defined global element in the schema.
     */
    Mapping get( QName elementName );

    /**
     * Gets a read-only view of all the {@link Mapping}s.
     */
    Collection<? extends Mapping> getMappings();

    /**
     * Returns the fully-qualified name of the Java type that is bound to the
     * specified XML type.
     *
     * @param xmlTypeName
     *      must not be null.
     * @return
     *      null if the XML type is not bound to any Java type.
     */
    TypeAndAnnotation getJavaType(QName xmlTypeName);

    /**
     * Generates artifacts.
     *
     * <p>
     * TODO: if JAXB supports various modes of code generations
     * (such as public interface only or implementation only or
     * etc), we should define bit flags to control those.
     *
     * <p>
     * This operation is only supported for a model built from a schema.
     *
     * @param extensions
     *      The JAXB RI extensions to run. This can be null or empty
     *      array if the caller wishes not to run any extension.
     *      <br>
     *
     *      Those specified extensions
     *      will participate in the code generation. Specifying an extension
     *      in this list has the same effect of turning that extension on
     *      via command line.
     *      <br>
     *
     *      It is the caller's responsibility to configure each augmenter
     *      properly by using {@link Plugin#parseArgument(Options, String[], int)}.
     *
     * @return
     *      object filled with the generated code. Use
     *      {@link JCodeModel#build(CodeWriter)} to write them
     *      to a disk.
     */
    JCodeModel generateCode( Plugin[] extensions, ErrorListener errorListener );
}
