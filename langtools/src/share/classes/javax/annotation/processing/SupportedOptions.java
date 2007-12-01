/*
 * Copyright 2005-2006 Sun Microsystems, Inc.  All Rights Reserved.
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

package javax.annotation.processing;

import java.lang.annotation.*;
import static java.lang.annotation.RetentionPolicy.*;
import static java.lang.annotation.ElementType.*;

/**
 * An annotation used to indicate what options an annotation processor
 * supports.  The {@link Processor#getSupportedOptions} method can
 * construct its result from the value of this annotation, as done by
 * {@link AbstractProcessor#getSupportedOptions}.  Only {@linkplain
 * Processor#getSupportedOptions strings conforming to the
 * grammar} should be used as values.
 *
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @author Peter von der Ah&eacute;
 * @since 1.6
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface SupportedOptions {
  String [] value();
}
