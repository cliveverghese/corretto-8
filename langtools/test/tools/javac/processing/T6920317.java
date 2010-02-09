/*
 * Copyright 2010 Sun Microsystems, Inc.  All Rights Reserved.
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
 * @bug 6920317
 * @summary package-info.java file has to be specified on the javac cmdline, else it will not be avail
 */

import java.io.*;
import java.util.*;
import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.util.*;
import javax.tools.*;

/**
 * The test exercises different ways of providing annotations for a package.
 * Each way provides an annotation with a unique argument. For each test
 * case, the test verifies that the annotation with the correct argument is
 * found by the compiler.
 */
public class T6920317 {
    public static void main(String... args) throws Exception {
        new T6920317().run(args);
    }

    // Used to describe properties of files to be put on command line, source path, class path
    enum Kind {
        /** File is not used. */
        NONE,
        /** File is used. */
        OLD,
        /** Only applies to files on classpath/sourcepath, when there is another file on the
         *  other path of type OLD, in which case, this file must be newer than the other one. */
        NEW,
        /** Only applies to files on classpath/sourcepath, when there is no file in any other
         *  location, in which case, this file will be generated by the annotation processor. */
        GEN
    }

    void run(String... args) throws Exception {
        // if no args given, all test cases are run
        // if args given, they indicate the test cases to be run
        for (int i = 0; i < args.length; i++) {
            tests.add(Integer.valueOf(args[i]));
        }

        setup();

        // Run tests for all combinations of files on command line, source path and class path.
        // Invalid combinations are skipped in the test method
        for (Kind cmdLine: EnumSet.of(Kind.NONE, Kind.OLD)) {
            for (Kind srcPath: Kind.values()) {
                for (Kind clsPath: Kind.values()) {
                    try {
                        test(cmdLine, srcPath, clsPath);
                    } catch (Exception e) {
                        e.printStackTrace();
                        error("Exception " + e);
                        // uncomment to stop on first failed test case
                        // throw e;
                    }
                }
            }
        }

        if (errors > 0)
            throw new Exception(errors + " errors occurred");
    }

    /** One time setup for files and directories to be used in the various test cases. */
    void setup() throws Exception {
        // Annotation used in test cases to annotate package. This file is
        // given on the command line in test cases.
        test_java = writeFile("Test.java", "package p; @interface Test { String value(); }");
        // Compile the annotation for use later in setup
        File tmpClasses = new File("tmp.classes");
        compile(tmpClasses, new String[] { }, test_java);

        // package-info file to use on the command line when requied
        cl_pkgInfo_java = writeFile("cl/p/package-info.java", "@Test(\"CL\") package p;");

        // source path containing package-info
        sp_old = new File("src.old");
        writeFile("src.old/p/package-info.java", "@Test(\"SP_OLD\") package p;");

        // class path containing package-info
        cp_old = new File("classes.old");
        compile(cp_old, new String[] { "-classpath", tmpClasses.getPath() },
                writeFile("tmp.old/p/package-info.java", "@Test(\"CP_OLD\") package p;"));

        // source path containing package-info which is newer than the one in cp-old
        sp_new = new File("src.new");
        File old_class = new File(cp_old, "p/package-info.class");
        writeFile("src.new/p/package-info.java", "@Test(\"SP_NEW\") package p;", old_class);

        // class path containing package-info which is newer than the one in sp-old
        cp_new = new File("classes.new");
        File old_java = new File(sp_old, "p/package-info.java");
        compile(cp_new, new String[] { "-classpath", tmpClasses.getPath() },
                writeFile("tmp.new/p/package-info.java", "@Test(\"CP_NEW\") package p;", old_java));

        // directory containing package-info.java to be "generated" later by annotation processor
        sp_gen = new File("src.gen");
        writeFile("src.gen/p/package-info.java", "@Test(\"SP_GEN\") package p;");

        // directory containing package-info.class to be "generated" later by annotation processor
        cp_gen = new File("classes.gen");
        compile(cp_gen, new String[] { "-classpath", tmpClasses.getPath() },
                writeFile("tmp.gen/p/package-info.java", "@Test(\"CP_GEN\") package p;"));
    }

    void test(Kind cl, Kind sp, Kind cp) throws Exception {
        if (skip(cl, sp, cp))
            return;

        ++count;
        // if test cases specified, skip this test case if not selected
        if (tests.size() > 0 && !tests.contains(count))
            return;

        System.err.println("Test " + count + " cl:" + cl + " sp:" + sp + " cp:" + cp);

        // test specific tmp directory
        File test_tmp = new File("tmp.test" + count);
        test_tmp.mkdirs();

        // build up list of options and files to be compiled
        List<String> opts = new ArrayList<String>();
        List<File> files = new ArrayList<File>();

        // expected value for annotation
        String expect = null;

        opts.add("-processorpath");
        opts.add(System.getProperty("test.classes"));
        opts.add("-processor");
        opts.add(Processor.class.getName());
        opts.add("-proc:only");
        opts.add("-d");
        opts.add(test_tmp.getPath());
        //opts.add("-verbose");
        files.add(test_java);

        /*
         * Analyze each of cl, cp, sp, building up the options and files to
         * be compiled, and determining the expected outcome fo the test case.
         */

        // command line file: either omitted or given
        if (cl == Kind.OLD) {
            files.add(cl_pkgInfo_java);
            // command line files always supercede files on paths
            expect = "CL";
        }

        // source path:
        switch (sp) {
        case NONE:
            break;

        case OLD:
            opts.add("-sourcepath");
            opts.add(sp_old.getPath());
            if (expect == null && cp == Kind.NONE) {
                assert cl == Kind.NONE && cp == Kind.NONE;
                expect = "SP_OLD";
            }
            break;

        case NEW:
            opts.add("-sourcepath");
            opts.add(sp_new.getPath());
            if (expect == null) {
                assert cl == Kind.NONE && cp == Kind.OLD;
                expect = "SP_NEW";
            }
            break;

        case GEN:
            opts.add("-Agen=" + new File(sp_gen, "p/package-info.java"));
            assert cl == Kind.NONE && cp == Kind.NONE;
            expect = "SP_GEN";
            break;
        }

        // class path:
        switch (cp) {
        case NONE:
            break;

        case OLD:
            opts.add("-classpath");
            opts.add(cp_old.getPath());
            if (expect == null && sp == Kind.NONE) {
                assert cl == Kind.NONE && sp == Kind.NONE;
                expect = "CP_OLD";
            }
            break;

        case NEW:
            opts.add("-classpath");
            opts.add(cp_new.getPath());
            if (expect == null) {
                assert cl == Kind.NONE && sp == Kind.OLD;
                expect = "CP_NEW";
            }
            break;

        case GEN:
            opts.add("-Agen=" + new File(cp_gen, "p/package-info.class"));
            assert cl == Kind.NONE && sp == Kind.NONE;
            expect = "CP_GEN";
            break;
        }

        // pass expected value to annotation processor
        assert expect != null;
        opts.add("-Aexpect=" + expect);

        // compile the files with the options that have been built up
        compile(opts, files);
    }

    /**
     * Return true if this combination of parameters does not identify a useful test case.
     */
    boolean skip(Kind cl, Kind sp, Kind cp) {
        // skip if no package files required
        if (cl == Kind.NONE && sp == Kind.NONE && cp == Kind.NONE)
            return true;

        // skip if both sp and sp are OLD, since results may be indeterminate
        if (sp == Kind.OLD && cp == Kind.OLD)
            return true;

        // skip if sp or cp is NEW but the other is not OLD
        if ((sp == Kind.NEW && cp != Kind.OLD) || (cp == Kind.NEW && sp != Kind.OLD))
            return true;

        // only use GEN if no other package-info files present
        if (sp == Kind.GEN && !(cl == Kind.NONE && cp == Kind.NONE) ||
            cp == Kind.GEN && !(cl == Kind.NONE && sp == Kind.NONE)) {
            return true;
        }

        // remaining combinations are valid
        return false;
    }

    /** Write a file with a given body. */
    File writeFile(String path, String body) throws Exception {
        File f = new File(path);
        if (f.getParentFile() != null)
            f.getParentFile().mkdirs();
        Writer out = new FileWriter(path);
        try {
            out.write(body);
        } finally {
            out.close();
        }
        return f;
    }

    /** Write a file with a given body, ensuring that the file is newer than a reference file. */
    File writeFile(String path, String body, File ref) throws Exception {
        for (int i = 0; i < 5; i++) {
            File f = writeFile(path, body);
            if (f.lastModified() > ref.lastModified())
                return f;
            Thread.sleep(2000);
        }
        throw new Exception("cannot create file " + path + " newer than " + ref);
    }

    /** Compile a file to a given directory, with options provided. */
    void compile(File dir, String[] opts, File src) throws Exception {
        dir.mkdirs();
        List<String> opts2 = new ArrayList<String>();
        opts2.addAll(Arrays.asList("-d", dir.getPath()));
        opts2.addAll(Arrays.asList(opts));
        compile(opts2, Collections.singletonList(src));
    }

    /** Compile files with options provided. */
    void compile(List<String> opts, List<File> files) throws Exception {
        System.err.println("javac: " + opts + " " + files);
        List<String> args = new ArrayList<String>();
        args.addAll(opts);
        for (File f: files)
            args.add(f.getPath());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        int rc = com.sun.tools.javac.Main.compile(args.toArray(new String[args.size()]), pw);
        pw.flush();
        if (sw.getBuffer().length() > 0)
            System.err.println(sw.toString());
        if (rc != 0)
            throw new Exception("compilation failed: rc=" + rc);
    }

    /** Report an error. */
    void error(String msg) {
        System.err.println("Error: " + msg);
        errors++;
    }

    /** Test case counter. */
    int count;

    /** Number of errors found. */
    int errors;

    /** Optional set of test cases to be run; empty implies all test cases. */
    Set<Integer> tests = new HashSet<Integer>();

    /*  Files created by setup. */
    File test_java;
    File sp_old;
    File sp_new;
    File sp_gen;
    File cp_old;
    File cp_new;
    File cp_gen;
    File cl_pkgInfo_java;

    /** Annotation processor used to verify the expected value for the
        package annotations found by javac. */
    @SupportedOptions({ "gen", "expect" })
    @SupportedAnnotationTypes({"*"})
    public static class Processor extends AbstractProcessor {
        public SourceVersion getSupportedSourceVersion() {
            return SourceVersion.latest();
        }

        public boolean process(Set<? extends TypeElement> annots, RoundEnvironment renv) {
            round++;
            System.err.println("Round " + round + " annots:" + annots + " rootElems:" + renv.getRootElements());

            // if this is the first round and the gen option is given, use the filer to create
            // a copy of the file specified by the gen option.
            String gen = getOption("gen");
            if (round == 1 && gen != null) {
                try {
                    Filer filer = processingEnv.getFiler();
                    JavaFileObject f;
                    if (gen.endsWith(".java"))
                        f = filer.createSourceFile("p.package-info");
                    else
                        f = filer.createClassFile("p.package-info");
                    System.err.println("copy " + gen + " to " + f.getName());
                    write(f, read(new File(gen)));
                } catch (IOException e) {
                    error("Cannot create package-info file: " + e);
                }
            }

            // if annotation processing is complete, verify the package annotation
            // found by the compiler.
            if (renv.processingOver()) {
                System.err.println("final round");
                Elements eu = processingEnv.getElementUtils();
                TypeElement te = eu.getTypeElement("p.Test");
                PackageElement pe = eu.getPackageOf(te);
                System.err.println("final: te:" + te + " pe:" + pe);
                List<? extends AnnotationMirror> annos = pe.getAnnotationMirrors();
                System.err.println("final: annos:" + annos);
                if (annos.size() == 1) {
                    String expect = "@" + te + "(\"" + getOption("expect") + "\")";
                    String actual = annos.get(0).toString();
                    checkEqual("package annotations", actual, expect);
                } else {
                    error("Wrong number of annotations found: (" + annos.size() + ") " + annos);
                }
            }

            return true;
        }

        /** Get an option given to the annotation processor. */
        String getOption(String name) {
            return processingEnv.getOptions().get(name);
        }

        /** Read a file. */
        byte[] read(File file) {
            byte[] bytes = new byte[(int) file.length()];
            DataInputStream in = null;
            try {
                in = new DataInputStream(new FileInputStream(file));
                in.readFully(bytes);
            } catch (IOException e) {
                error("Error reading file: " + e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        error("Error closing file: " + e);
                    }
                }
            }
            return  bytes;
        }

        /** Write a file. */
        void write(JavaFileObject file, byte[] bytes) {
            OutputStream out = null;
            try {
                out = file.openOutputStream();
                out.write(bytes, 0, bytes.length);
            } catch (IOException e) {
                error("Error writing file: " + e);
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        error("Error closing file: " + e);
                    }
                }
            }
        }

        /** Check two strings are equal, and report an error if they are not. */
        private void checkEqual(String label, String actual, String expect) {
            if (!actual.equals(expect)) {
                error("Unexpected value for " + label + "; actual=" + actual + ", expected=" + expect);
            }
        }

        /** Report an error to the annotation processing system. */
        void error(String msg) {
            Messager messager = processingEnv.getMessager();
            messager.printMessage(Diagnostic.Kind.ERROR, msg);
        }

        int round;
    }
}
