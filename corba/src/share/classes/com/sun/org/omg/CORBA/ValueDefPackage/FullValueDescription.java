/*
 * Copyright 1999 Sun Microsystems, Inc.  All Rights Reserved.
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
package com.sun.org.omg.CORBA.ValueDefPackage;


/**
* com/sun/org/omg/CORBA/ValueDefPackage/FullValueDescription.java
* Generated by the IDL-to-Java compiler (portable), version "3.0"
* from ir.idl
* Thursday, May 6, 1999 1:51:52 AM PDT
*/

// This file has been manually _CHANGED_

public final class FullValueDescription implements org.omg.CORBA.portable.IDLEntity
{
    public String name = null;
    public String id = null;
    public boolean is_abstract = false;
    public boolean is_custom = false;
    public String defined_in = null;
    public String version = null;
    public com.sun.org.omg.CORBA.OperationDescription operations[] = null;
    public com.sun.org.omg.CORBA.AttributeDescription attributes[] = null;

    // _CHANGED_
    //public com.sun.org.omg.CORBA.ValueMember members[] = null;
    public org.omg.CORBA.ValueMember members[] = null;

    public com.sun.org.omg.CORBA.Initializer initializers[] = null;
    public String supported_interfaces[] = null;
    public String abstract_base_values[] = null;
    public boolean is_truncatable = false;
    public String base_value = null;
    public org.omg.CORBA.TypeCode type = null;

    public FullValueDescription ()
    {
    } // ctor

    // _CHANGED_
    //public FullValueDescription (String _name, String _id, boolean _is_abstract, boolean _is_custom, String _defined_in, String _version, com.sun.org.omg.CORBA.OperationDescription[] _operations, com.sun.org.omg.CORBA.AttributeDescription[] _attributes, com.sun.org.omg.CORBA.ValueMember[] _members, com.sun.org.omg.CORBA.Initializer[] _initializers, String[] _supported_interfaces, String[] _abstract_base_values, boolean _is_truncatable, String _base_value, org.omg.CORBA.TypeCode _type)
    public FullValueDescription (String _name, String _id, boolean _is_abstract, boolean _is_custom, String _defined_in, String _version, com.sun.org.omg.CORBA.OperationDescription[] _operations, com.sun.org.omg.CORBA.AttributeDescription[] _attributes, org.omg.CORBA.ValueMember[] _members, com.sun.org.omg.CORBA.Initializer[] _initializers, String[] _supported_interfaces, String[] _abstract_base_values, boolean _is_truncatable, String _base_value, org.omg.CORBA.TypeCode _type)
    {
        name = _name;
        id = _id;
        is_abstract = _is_abstract;
        is_custom = _is_custom;
        defined_in = _defined_in;
        version = _version;
        operations = _operations;
        attributes = _attributes;
        members = _members;
        initializers = _initializers;
        supported_interfaces = _supported_interfaces;
        abstract_base_values = _abstract_base_values;
        is_truncatable = _is_truncatable;
        base_value = _base_value;
        type = _type;
    } // ctor

} // class FullValueDescription
