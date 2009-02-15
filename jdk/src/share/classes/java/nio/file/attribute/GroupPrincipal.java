/*
 * Copyright 2007-2009 Sun Microsystems, Inc.  All Rights Reserved.
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

package java.nio.file.attribute;

/**
 * A {@code UserPrincipal} representing a <em>group identity</em>, used to
 * determine access rights to objects in a file system. The exact definition of
 * a group is implementation specific, but typically, it represents an identity
 * created for administrative purposes so as to determine the access rights for
 * the members of the group. Whether an entity can be a member of multiple
 * groups, and whether groups can be nested, are implementation specified and
 * therefore not specified.
 *
 * @since 1.7
 *
 * @see UserPrincipalLookupService#lookupPrincipalByGroupName
 */

public interface GroupPrincipal extends UserPrincipal { }
