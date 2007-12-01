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

/* @test
 * @bug 4763362
 * @summary Ensure that Process.waitFor returns the correct status code
 *          even for very short-running subprocesses
 */


public class Status {

    private static int N = 160;

    public static void main(String args[])
        throws Exception
    {
        if (!System.getProperty("os.name").equals("Linux"))
            return;
        for (int i = 0; i < N; i++) {
            Process p = Runtime.getRuntime().exec("false");
            int s = p.waitFor();
            System.out.print(s);
            System.out.print(' ');
            if (s != 1) {
                System.out.println();
                throw new Exception("Wrong status");
            }
        }
        System.out.println();
    }

}
