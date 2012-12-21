/*
 * Copyright (c) 2010, 2012, Oracle and/or its affiliates. All rights reserved.
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

package jdk.nashorn.internal.objects;

import jdk.nashorn.internal.objects.annotations.Attribute;
import jdk.nashorn.internal.objects.annotations.Constructor;
import jdk.nashorn.internal.objects.annotations.Function;
import jdk.nashorn.internal.objects.annotations.ScriptClass;
import jdk.nashorn.internal.runtime.NativeJavaPackage;
import jdk.nashorn.internal.runtime.ScriptObject;
import org.dynalang.dynalink.CallSiteDescriptor;
import org.dynalang.dynalink.beans.StaticClass;
import org.dynalang.dynalink.linker.GuardedInvocation;

/**
 * This is "JavaImporter" constructor. This constructor allows you to use Java types omitting explicit package names.
 * Objects of this constructor are used along with {@code "with"} statements and as such are not usable in ECMAScript
 * strict mode. Example:
 * <pre>
 *     var imports = new JavaImporter(java.util, java.io);
 *     with (imports) {
 *         var m = new HashMap(); // java.util.HashMap
 *         var f = new File("."); // java.io.File
 *         ...
 *     }
 * </pre>
 * Note however that the preferred way for accessing Java types in Nashorn is through the use of
 * {@link NativeJava#type(Object, Object) Java.type()} method.
 */
@ScriptClass("JavaImporter")
public class NativeJavaImporter extends ScriptObject {
    private final Object[] args;

    NativeJavaImporter(final Object[] args) {
        this.args = args;
        this.setProto(Global.instance().getJavaImporterPrototype());
    }

    @Override
    public String getClassName() {
        return "JavaImporter";
    }

    /**
     * Constructor
     * @param isNew is the new operator used for instantiating this NativeJavaImporter
     * @param self self reference
     * @param args arguments
     * @return NativeJavaImporter instance
     */
    @Constructor(arity = 1)
    public static Object constructor(final boolean isNew, final Object self, final Object... args) {
        return new NativeJavaImporter(args);
    }

    /**
     * "No such property" call placeholder.
     *
     * This can never be called as we override {@link ScriptObject#noSuchProperty}. We do declare it here as it's a signal
     * to {@link jdk.nashorn.internal.runtime.WithObject} that it's worth trying doing a {@code noSuchProperty} on this object.
     *
     * @param self self reference
     * @param name property name
     * @return never returns
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE)
    public static Object __noSuchProperty__(final Object self, final Object name) {
        throw new AssertionError("__noSuchProperty__ placeholder called");
    }

    /**
     * "No such method call" placeholder
     *
     * This can never be called as we override {@link ScriptObject#noSuchMethod}. We do declare it here as it's a signal
     * to {@link jdk.nashorn.internal.runtime.WithObject} that it's worth trying doing a noSuchProperty on this object.
     *
     * @param self self reference
     * @param args arguments to method
     * @return never returns
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE)
    public static Object __noSuchMethod__(final Object self, final Object... args) {
        throw new AssertionError("__noSuchMethod__ placeholder called");
    }

    @Override
    public GuardedInvocation noSuchProperty(final CallSiteDescriptor desc) {
        return createAndSetProperty(desc) ? super.lookup(desc) : super.noSuchProperty(desc);
    }

    @Override
    public GuardedInvocation noSuchMethod(final CallSiteDescriptor desc) {
        return createAndSetProperty(desc) ? super.lookup(desc) : super.noSuchMethod(desc);
    }

    private boolean createAndSetProperty(final CallSiteDescriptor desc) {
        final String name = desc.getNameToken(CallSiteDescriptor.NAME_OPERAND);
        final Object value = createProperty(name);
        if(value != null) {
            set(name, value, getContext()._strict);
            return true;
        }
        return false;
    }

    private Object createProperty(final String name) {
        final int len = args.length;

        for (int i = len - 1; i > -1; i--) {
            final Object obj = args[i];

            if (obj instanceof StaticClass) {
                if (((StaticClass)obj).getRepresentedClass().getSimpleName().equals(name)) {
                    return obj;
                }
            } else if (obj instanceof NativeJavaPackage) {
                final String pkgName  = ((NativeJavaPackage)obj).getName();
                final String fullName = pkgName.isEmpty() ? name : (pkgName + "." + name);
                try {
                    return StaticClass.forClass(Class.forName(fullName));
                } catch (final ClassNotFoundException e) {
                    // IGNORE
                }
            }
        }
        return null;
    }
}
