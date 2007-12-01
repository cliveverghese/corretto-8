/*
 * Copyright (c) 2007 Sun Microsystems, Inc.  All Rights Reserved.
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
 *@test
 *@bug 6442006
 *@summary Test case for verifying timezone display name for Asia/Taipei
 */

import java.util.Locale;
import java.util.TimeZone;

public class Bug6442006 {

    public static void main(String[] args) {

        TimeZone tz = TimeZone.getTimeZone("Asia/Taipei");
        Locale tzLocale = new Locale("ja");
        String jaStdName = "\u4e2d\u56fd\u6a19\u6e96\u6642";
        String jaDstName = "\u4e2d\u56fd\u590f\u6642\u9593";

        if (!tz.getDisplayName(false, TimeZone.LONG, tzLocale).equals
           (jaStdName))
             throw new RuntimeException("\n" + tzLocale + ": LONG, " +
                                        "non-daylight saving name for " +
                                        tz.getID() +
                                        " should be " +
                                        jaStdName);
        if (!tz.getDisplayName(true, TimeZone.LONG, tzLocale).equals
           (jaDstName))
             throw new RuntimeException("\n" + tzLocale + ": LONG, " +
                                        "daylight saving name for " +
                                        tz.getID() +
                                        " should be " +
                                        jaDstName);
    }
}
