/*
 * Copyright 2005 Sun Microsystems, Inc.  All Rights Reserved.
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
 *
 *
 *  This isn't the test case: ImmutableResourceTest.sh is.
 *  Refer to ImmutableResourceTest.sh when running this test.
 *
 *  @bug        6287579
 *  @summary    SubClasses of ListResourceBundle should fix getContents()
 *
 *  @author Tim Bell
 *
 */
import java.util.ResourceBundle;

public class ImmutableResourceTest {

    public static void main(String[] args) throws Exception {
        /* Reach under the covers and get the message strings */
        sun.tools.native2ascii.resources.MsgNative2ascii msgs =
            new sun.tools.native2ascii.resources.MsgNative2ascii ();
        Object [][] testData = msgs.getContents();

        /* Shred our copy of the message strings */
        for (int ii = 0; ii < testData.length; ii++) {
            testData[ii][0] = "T6287579";
            testData[ii][1] = "yyy";
        }

        /*
         * Try to lookup the shredded key.
         * If this is successful we have a problem.
         */
        String ss = null;
        try {
            ss = msgs.getString("T6287579");
        } catch (java.util.MissingResourceException mre) {
            /*
             * Ignore the expected MissingResourceException since key
             * "T6287579" is not in the canonical MsgNative2ascii.
             */
        }
        if ("yyy".equals(ss)) {
            throw new Exception ("SubClasses of ListResourceBundle should fix getContents()");
        }
        System.out.println("...Finished.");
    }
}
