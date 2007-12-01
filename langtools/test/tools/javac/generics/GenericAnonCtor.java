/*
 * Copyright 2004 Sun Microsystems, Inc.  All Rights Reserved.
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
 * @bug 4985520
 * @summary javac crash on parameterized anonymous constructor invocation
 * @author gafter
 *
 * @compile -source 1.5 GenericAnonCtor.java
 * @run main GenericAnonCtor
 */

class A<T1> {
    T1 obj1;
    Object obj2;
    <T2> A(T1 t1, T2 t2) {
        obj1 = t1;
        obj2 = t2;
    }
    public String toString() {
        return (obj1 + " " + obj2).intern();
    }
}

public class GenericAnonCtor {
    public static void main(String[] args) {
        A<Integer> a = new <String>A<Integer>(new Integer(11), "foo") {};
        if (a.toString() != "11 foo") throw new Error();
    }
}
