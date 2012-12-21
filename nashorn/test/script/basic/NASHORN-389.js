/*
 * Copyright (c) 2010, 2012, Oracle and/or its affiliates. All rights reserved.
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
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * NASHORN-389 : number to string conversion issues.
 *
 * @test
 * @run
 */

print(1000000000000000000000);
print(0.000000000100000000000);
print(1e-5);
var x = -1.23456789e+21;
var y = -1.23456789e+20;
print(x.toFixed(9));
print(y.toFixed(9).indexOf(",") === -1); // no grouping
//print(y.toFixed(9)); // FIXME expected: -123456788999999995904.000000000
//print(1000000000000000128); // FIXME expected: 1000000000000000100
//print((1000000000000000128).toFixed(0)); // FIXME expected: 1000000000000000128
