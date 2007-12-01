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
 * @test
 * @bug 4944561
 * @summary Test hashCode() to have less than 10% of hash code conflicts.
 */

import java.util.*;

public class HashCodeTest {
    public static void main(String[] args) {
        Locale[] locales = Locale.getAvailableLocales();
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        Map map = new HashMap(locales.length);
        int conflicts = 0;

        for (int i = 0; i < locales.length; i++) {
            Locale loc = locales[i];
            int hc = loc.hashCode();
            min = Math.min(hc, min);
            max = Math.max(hc, max);
            Integer key = new Integer(hc);
            if (map.containsKey(key)) {
                conflicts++;
                System.out.println("conflict: " + (Locale) map.get(key) + ", " + loc);
            } else {
                map.put(key, loc);
            }
        }
        System.out.println(locales.length+" locales: conflicts="+conflicts
                           +", min="+min+", max="+max +", diff="+(max-min));
        if (conflicts >= (locales.length / 10)) {
            throw new RuntimeException("too many conflicts: " + conflicts
                                       + " per " + locales.length + " locales");
        }
    }
}
