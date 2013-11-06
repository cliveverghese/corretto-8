/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.management.DiagnosticCommandMBean;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import sun.management.ManagementFactoryHelper;

/**
 * When an exception is thrown, the JVM collects just enough information
 * about the stack trace to be able to create a full fledged stack trace
 * (StackTraceElement[]). The backtrace contains this information and the
 * JVM  must make sure that the data in the backtrace is still usable after
 * a class redefinition.
 *
 * After the PermGen removal there was a bug when the last reference to a Method
 * was in the backtrace. The class of the method was kept alive, because of the
 * mirror stored in the backtrace, but the old versions of the redefined method
 * could be freed, since class redefinition didn't know about the backtraces.
 */
public class RedefineMethodInBacktraceApp {
    public static void main(String args[]) throws Exception {
        System.out.println("Hello from RedefineMethodInBacktraceApp!");

        new RedefineMethodInBacktraceApp().doTest();

        System.exit(0);
    }

    public static CountDownLatch stop = new CountDownLatch(1);
    public static CountDownLatch called = new CountDownLatch(1);

    private void doTest() throws Exception {
        doMethodInBacktraceTest();
        doMethodInBacktraceTestB();
    }

    private void doMethodInBacktraceTest() throws Exception {
        Throwable t = getThrowableFromMethodToRedefine();

        doRedefine(RedefineMethodInBacktraceTarget.class);

        doClassUnloading();

        touchRedefinedMethodInBacktrace(t);
    }

    private void doMethodInBacktraceTestB() throws Exception {
        // Start a thread which blocks in method
        Thread t = new Thread(RedefineMethodInBacktraceTargetB::methodToRedefine);
        t.setDaemon(true);
        t.start();

        // Wait here until the new thread is in the method we want to redefine
        called.await();

        // Now redefine the class while the method is still on the stack of the new thread
        doRedefine(RedefineMethodInBacktraceTargetB.class);

        // Do thread dumps in two different ways (to exercise different code paths)
        // while the old class is still on the stack

        ThreadInfo[] tis = ManagementFactory.getThreadMXBean().dumpAllThreads(false, false);
        for(ThreadInfo ti : tis) {
            System.out.println(ti);
        }

        String[] threadPrintArgs = {};
        Object[] dcmdArgs = {threadPrintArgs};
        String[] signature = {String[].class.getName()};
        DiagnosticCommandMBean dcmd = ManagementFactoryHelper.getDiagnosticCommandMBean();
        System.out.println(dcmd.invoke("threadPrint", dcmdArgs, signature));

        // release the thread
        stop.countDown();
    }

    private static Throwable getThrowableFromMethodToRedefine() throws Exception {
        Class<RedefineMethodInBacktraceTarget> c =
                RedefineMethodInBacktraceTarget.class;
        Method method = c.getMethod("methodToRedefine");

        Throwable thrownFromMethodToRedefine = null;
        try {
            method.invoke(null);
        } catch (InvocationTargetException e) {
            thrownFromMethodToRedefine = e.getCause();
            if (!(thrownFromMethodToRedefine instanceof RuntimeException)) {
                throw e;
            }
        }
        method = null;
        c = null;

        return thrownFromMethodToRedefine;
    }

    private static void doClassUnloading() {
        // This will clean out old, unused redefined methods.
        System.gc();
    }

    private static void touchRedefinedMethodInBacktrace(Throwable throwable) {
        // Make sure that we can convert the backtrace, which is referring to
        // the redefined method, to a  StrackTraceElement[] without crashing.
        throwable.getStackTrace();
    }

    private static void doRedefine(Class<?> clazz) throws Exception {
        // Load the second version of this class.
        File f = new File(clazz.getName() + ".class");
        System.out.println("Reading test class from " + f.getAbsolutePath());
        InputStream redefineStream = new FileInputStream(f);

        byte[] redefineBuffer = NamedBuffer.loadBufferFromStream(redefineStream);

        ClassDefinition redefineParamBlock = new ClassDefinition(
                clazz, redefineBuffer);

        RedefineMethodInBacktraceAgent.getInstrumentation().redefineClasses(
                new ClassDefinition[] {redefineParamBlock});
    }
}
