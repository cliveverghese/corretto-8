/*
 * Copyright 1998-2000 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.jdi.request;

import com.sun.jdi.*;

/**
 * Request for notification when a class is unloaded in the target VM.
 * When an enabled ClassUnloadRequest is satisfied, a
 * {@link com.sun.jdi.event.EventSet event set} containing an
 * {@link com.sun.jdi.event.ClassUnloadEvent ClassUnloadEvent} will
 * be placed on the {@link com.sun.jdi.event.EventQueue EventQueue}.
 * The collection of existing ClassUnloadRequests is
 * managed by the {@link EventRequestManager}
 * <p>
 * Refer to the Java Virtual Machine Specification for more information
 * on class unloading.
 *
 * @see com.sun.jdi.event.ClassUnloadEvent
 * @see com.sun.jdi.event.EventQueue
 * @see EventRequestManager
 *
 * @author Robert Field
 * @since  1.3
 */
public interface ClassUnloadRequest extends EventRequest {

    /**
     * Restricts the events generated by this request to the
     * unloading of reference types whose name matches a restricted
     * regular expression. Regular expressions are limited to exact
     * matches and patterns that begin with '*' or end with '*'; for
     * example, "*.Foo" or "java.*".
     * @param classPattern the pattern String to filter for.
     * @throws InvalidRequestStateException if this request is currently
     * enabled or has been deleted.
     * Filters may be added only to disabled requests.
     */
    void addClassFilter(String classPattern);

    /**
     * Restricts the events generated by this request to the
     * unloading of reference types whose name does <b>not</b> match
     * a restricted regular expression. Regular expressions are limited
     * to exact matches and patterns that begin with '*' or end with '*';
     * for example, "*.Foo" or "java.*".
     * @param classPattern the pattern String to filter against.
     * @throws InvalidRequestStateException if this request is currently
     * enabled or has been deleted.
     * Filters may be added only to disabled requests.
     */
    void addClassExclusionFilter(String classPattern);
}
