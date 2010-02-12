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
 *
 */

/**
 * @test
 * @bug 6910605
 * @summary C2: NullPointerException/ClassCaseException is thrown when C2 with DeoptimizeALot is used
 *
 * @run main/othervm -XX:+IgnoreUnrecognizedVMOptions -XX:+DeoptimizeALot -Xbatch Test
 *
 * original test: nsk/coverage/runtime/runtime007
 */

import java.io.*;

public class Test {
        public static int buf=0;

        public static void main( String argv[] ) {
                System.exit(run(argv, System.out)+95);
        }

        public static int run(String argv[],PrintStream out) {
                int ret=0, retx=0, bad=0;

                for( int i=0; (i < 100000) && (bad < 10) ; i++ ) {
                        retx = OptoRuntime_f2i_Type(out);
                        ret += retx;
                        if( retx !=0 ) {
                                out.println("i="+i);
                                bad++;
                        }
                }
                return ret==0 ? 0 : 2 ;
        }

        public static int OptoRuntime_f2i_Type(PrintStream out) {
                int c1=2, c2=3, c3=4, c4=5, c5=6;
                int j=0, k=0;
                try {
                        int[][] iii=(int[][])(new int[c1][c2]);

                        for( j=0; j<c1; j++ ) {
                                for( k=0; k<c2; k++ ) {
                                        iii[j][k]=(int)((float)(j+1)/(float)(k+1));
                                }
                        }
                } catch (Throwable e) {
                        out.println("Unexpected exception " + e);
                        e.printStackTrace(out);
                        out.println("j="+j+", k="+k);
                        return 1;
                }
                return 0;
        }

}
