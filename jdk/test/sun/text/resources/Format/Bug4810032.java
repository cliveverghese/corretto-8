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
 *@bug 4810032
 *@summary verify the ja full time pattern parsing
*/

import java.text.*;
import java.util.*;

public class Bug4810032
{
        public static void main(String[] arg)
        {
                String s = "2003\u5e749\u670826\u65e5"; // "2003y9m26d"
                DateFormat df =
                   DateFormat.getDateInstance(DateFormat.FULL,Locale.JAPANESE);

                try {
                        if ( !s.equals(df.format(df.parse(s))) )
                           throw new RuntimeException();
                } catch (ParseException e) {
                        throw new RuntimeException();
                }
        }
}
