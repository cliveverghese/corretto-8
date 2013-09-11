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

import sun.management.ManagementFactoryHelper;
import com.sun.management.HotSpotDiagnosticMXBean;
import com.sun.management.VMOption;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Arrays;

import com.oracle.java.testlibrary.*;
import sun.hotspot.WhiteBox;

class DetermineMaxHeapForCompressedOops {
  public static void main(String[] args) throws Exception {
    WhiteBox wb = WhiteBox.getWhiteBox();
    System.out.print(wb.getCompressedOopsMaxHeapSize());
  }
}

class TestUseCompressedOopsErgoTools {

  private static long getClassMetaspaceSize() {
    HotSpotDiagnosticMXBean diagnostic = ManagementFactoryHelper.getDiagnosticMXBean();

    VMOption option = diagnostic.getVMOption("ClassMetaspaceSize");
    return Long.parseLong(option.getValue());
  }


  public static long getMaxHeapForCompressedOops(String[] vmargs) throws Exception {
    OutputAnalyzer output = runWhiteBoxTest(vmargs, DetermineMaxHeapForCompressedOops.class.getName(), new String[] {}, false);
    return Long.parseLong(output.getStdout());
  }

  public static boolean is64bitVM() {
    String val = System.getProperty("sun.arch.data.model");
    if (val == null) {
      throw new RuntimeException("Could not read sun.arch.data.model");
    }
    if (val.equals("64")) {
      return true;
    } else if (val.equals("32")) {
      return false;
    }
    throw new RuntimeException("Unexpected value " + val + " of sun.arch.data.model");
  }

  /**
   * Executes a new VM process with the given class and parameters.
   * @param vmargs Arguments to the VM to run
   * @param classname Name of the class to run
   * @param arguments Arguments to the class
   * @param useTestDotJavaDotOpts Use test.java.opts as part of the VM argument string
   * @return The OutputAnalyzer with the results for the invocation.
   */
  public static OutputAnalyzer runWhiteBoxTest(String[] vmargs, String classname, String[] arguments, boolean useTestDotJavaDotOpts) throws Exception {
    ArrayList<String> finalargs = new ArrayList<String>();

    String[] whiteboxOpts = new String[] {
      "-Xbootclasspath/a:.",
      "-XX:+UnlockDiagnosticVMOptions", "-XX:+WhiteBoxAPI",
      "-cp", System.getProperty("java.class.path"),
    };

    if (useTestDotJavaDotOpts) {
      // System.getProperty("test.java.opts") is '' if no options is set,
      // we need to skip such a result
      String[] externalVMOpts = new String[0];
      if (System.getProperty("test.java.opts") != null && System.getProperty("test.java.opts").length() != 0) {
        externalVMOpts = System.getProperty("test.java.opts").split(" ");
      }
      finalargs.addAll(Arrays.asList(externalVMOpts));
    }

    finalargs.addAll(Arrays.asList(vmargs));
    finalargs.addAll(Arrays.asList(whiteboxOpts));
    finalargs.add(classname);
    finalargs.addAll(Arrays.asList(arguments));

    ProcessBuilder pb = ProcessTools.createJavaProcessBuilder(finalargs.toArray(new String[0]));
    OutputAnalyzer output = new OutputAnalyzer(pb.start());
    output.shouldHaveExitValue(0);
    return output;
  }

  private static String[] join(String[] part1, String part2) {
    ArrayList<String> result = new ArrayList<String>();
    result.addAll(Arrays.asList(part1));
    result.add(part2);
    return result.toArray(new String[0]);
  }

  public static void checkCompressedOopsErgo(String[] gcflags) throws Exception {
    long maxHeapForCompressedOops = getMaxHeapForCompressedOops(gcflags);

    checkUseCompressedOops(gcflags, maxHeapForCompressedOops, true);
    checkUseCompressedOops(gcflags, maxHeapForCompressedOops - 1, true);
    checkUseCompressedOops(gcflags, maxHeapForCompressedOops + 1, false);

    // the use of HeapBaseMinAddress should not change the outcome
    checkUseCompressedOops(join(gcflags, "-XX:HeapBaseMinAddress=32G"), maxHeapForCompressedOops, true);
    checkUseCompressedOops(join(gcflags, "-XX:HeapBaseMinAddress=32G"), maxHeapForCompressedOops - 1, true);
    checkUseCompressedOops(join(gcflags, "-XX:HeapBaseMinAddress=32G"), maxHeapForCompressedOops + 1, false);

    // use a different object alignment
    maxHeapForCompressedOops = getMaxHeapForCompressedOops(join(gcflags, "-XX:ObjectAlignmentInBytes=16"));

    checkUseCompressedOops(join(gcflags, "-XX:ObjectAlignmentInBytes=16"), maxHeapForCompressedOops, true);
    checkUseCompressedOops(join(gcflags, "-XX:ObjectAlignmentInBytes=16"), maxHeapForCompressedOops - 1, true);
    checkUseCompressedOops(join(gcflags, "-XX:ObjectAlignmentInBytes=16"), maxHeapForCompressedOops + 1, false);

    // use a different ClassMetaspaceSize
    String classMetaspaceSizeArg = "-XX:ClassMetaspaceSize=" + 2 * getClassMetaspaceSize();
    maxHeapForCompressedOops = getMaxHeapForCompressedOops(join(gcflags, classMetaspaceSizeArg));

    checkUseCompressedOops(join(gcflags, classMetaspaceSizeArg), maxHeapForCompressedOops, true);
    checkUseCompressedOops(join(gcflags, classMetaspaceSizeArg), maxHeapForCompressedOops - 1, true);
    checkUseCompressedOops(join(gcflags, classMetaspaceSizeArg), maxHeapForCompressedOops + 1, false);
  }

  private static void checkUseCompressedOops(String[] args, long heapsize, boolean expectUseCompressedOops) throws Exception {
     ArrayList<String> finalargs = new ArrayList<String>();
     finalargs.addAll(Arrays.asList(args));
     finalargs.add("-Xmx" + heapsize);
     finalargs.add("-XX:+PrintFlagsFinal");
     finalargs.add("-version");

     String output = expectValid(finalargs.toArray(new String[0]));

     boolean actualUseCompressedOops = getFlagBoolValue(" UseCompressedOops", output);

     if (expectUseCompressedOops != actualUseCompressedOops) {
       throw new RuntimeException("Expected use of compressed oops: " + expectUseCompressedOops + " but was: " + actualUseCompressedOops);
     }
  }

  private static boolean getFlagBoolValue(String flag, String where) {
    Matcher m = Pattern.compile(flag + "\\s+:?= (true|false)").matcher(where);
    if (!m.find()) {
      throw new RuntimeException("Could not find value for flag " + flag + " in output string");
    }
    String match = m.group(1).equals("true");
  }

  private static String expect(String[] flags, boolean hasWarning, boolean hasError, int errorcode) throws Exception {
    ProcessBuilder pb = ProcessTools.createJavaProcessBuilder(flags);
    OutputAnalyzer output = new OutputAnalyzer(pb.start());
    output.shouldHaveExitValue(errorcode);
    return output.getStdout();
  }

  private static String expectValid(String[] flags) throws Exception {
    return expect(flags, false, false, 0);
  }
}

