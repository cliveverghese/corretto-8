/*
 * Copyright 2002 Sun Microsystems, Inc.  All Rights Reserved.
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

/**
 *  @test
 *  @bug 4628726
 *  @summary Test class redefinition at start only (they use breakpoint
 *  or resumeTo()) cross tested with other tests.
 *  ExceptionEvents/StackOverflowUncaughtTarg are here because they hit
 *  an unrelated crash in event testing.
 *
 *  @author Robert Field
 *
 *  @run build TestScaffold VMConnection TargetListener TargetAdapter
 *  @run compile -g CountEvent.java
 *  @run compile -g CountFilterTest.java
 *  @run compile -g FramesTest.java
 *  @run compile -g InvokeTest.java
 *
 *  @run main CountEvent -redefstart
 *  @run main CountFilterTest -redefstart
 *  @run main FramesTest -redefstart
 *  @run main InvokeTest -redefstart
 *
 *  @run main/othervm ExceptionEvents -redefstart U A StackOverflowUncaughtTarg null
 *  @run main/othervm ExceptionEvents -redefstart U A StackOverflowUncaughtTarg java.lang.Error
 *  @run main/othervm ExceptionEvents -redefstart U A StackOverflowUncaughtTarg java.lang.StackOverflowError
 *  @run main PopSynchronousTest -redefstart
 */
