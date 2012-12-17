/*
 * Copyright (c) 1996, 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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

/*
 */

/*
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1999 - All Rights Reserved
 *
 * The original version of this source code and documentation
 * is copyrighted and owned by Taligent, Inc., a wholly-owned
 * subsidiary of IBM. These materials are provided under terms
 * of a License Agreement between Taligent and Sun. This technology
 * is protected by multiple US and International patents.
 *
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 *
 */

package sun.text.resources;

import java.util.ListResourceBundle;

public class FormatData extends ListResourceBundle {
    /**
     * Overrides ListResourceBundle
     */
    protected final Object[][] getContents() {
        final String[] buddhistEras = new String[] { // Thai Buddhist calendar era strings
            "BC",     // BC
            "B.E."    // Buddhist Era
        };

        // Japanese imperial calendar era abbreviations
        final String[] japaneseEraAbbrs = new String[] {
            "",
            "M",
            "T",
            "S",
            "H",
        };

        return new Object[][] {
            { "MonthNames",
                new String[] {
                    "January", // january
                    "February", // february
                    "March", // march
                    "April", // april
                    "May", // may
                    "June", // june
                    "July", // july
                    "August", // august
                    "September", // september
                    "October", // october
                    "November", // november
                    "December", // december
                    "" // month 13 if applicable
                }
            },
            { "MonthAbbreviations",
                new String[] {
                    "Jan", // abb january
                    "Feb", // abb february
                    "Mar", // abb march
                    "Apr", // abb april
                    "May", // abb may
                    "Jun", // abb june
                    "Jul", // abb july
                    "Aug", // abb august
                    "Sep", // abb september
                    "Oct", // abb october
                    "Nov", // abb november
                    "Dec", // abb december
                    "" // abb month 13 if applicable
                }
            },
            { "DayNames",
                new String[] {
                    "Sunday", // Sunday
                    "Monday", // Monday
                    "Tuesday", // Tuesday
                    "Wednesday", // Wednesday
                    "Thursday", // Thursday
                    "Friday", // Friday
                    "Saturday" // Saturday
                }
            },
            { "DayAbbreviations",
                new String[] {
                    "Sun", // abb Sunday
                    "Mon", // abb Monday
                    "Tue", // abb Tuesday
                    "Wed", // abb Wednesday
                    "Thu", // abb Thursday
                    "Fri", // abb Friday
                    "Sat" // abb Saturday
                }
            },
            { "DayNarrows",
                new String[] {
                    "S",
                    "M",
                    "T",
                    "W",
                    "T",
                    "F",
                    "S",
                }
            },
            { "AmPmMarkers",
                new String[] {
                    "AM", // am marker
                    "PM" // pm marker
                }
            },
            { "narrow.AmPmMarkers",
                new String[] {
                    "a", // am marker
                    "p"  // pm marker
                }
            },
            { "Eras",
                new String[] { // era strings for GregorianCalendar
                    "BC",
                    "AD"
                }
            },
            { "narrow.Eras",
                new String[] {
                    "B",
                    "A",
                }
            },
            { "buddhist.Eras",
              buddhistEras
            },
            { "buddhist.short.Eras",
              buddhistEras
            },
            { "buddhist.narrow.Eras",
              buddhistEras
            },
            { "japanese.Eras",
                new String[] { // Japanese imperial calendar era strings
                    "",
                    "Meiji",
                    "Taisho",
                    "Showa",
                    "Heisei",
                }
            },
            { "japanese.short.Eras",
              japaneseEraAbbrs
            },
            { "japanese.narrow.Eras",
              japaneseEraAbbrs
            },
            { "japanese.FirstYear",
                new String[] { // Japanese imperial calendar year name
                    // empty in English
                }
            },
            { "NumberPatterns",
                new String[] {
                    "#,##0.###;-#,##0.###", // decimal pattern
                    "\u00a4 #,##0.00;-\u00a4 #,##0.00", // currency pattern
                    "#,##0%" // percent pattern
                }
            },
            { "DefaultNumberingSystem", "" },
            { "NumberElements",
                new String[] {
                    ".", // decimal separator
                    ",", // group (thousands) separator
                    ";", // list separator
                    "%", // percent sign
                    "0", // native 0 digit
                    "#", // pattern digit
                    "-", // minus sign
                    "E", // exponential
                    "\u2030", // per mille
                    "\u221e", // infinity
                    "\ufffd" // NaN
                }
            },
            { "arab.NumberElements",
                new String[] {
                    "\u066b",
                    "\u066c",
                    "\u061b",
                    "\u066a",
                    "\u0660",
                    "#",
                    "-",
                    "\u0627\u0633",
                    "\u0609",
                    "\u221e",
                    "NaN",
                }
            },
            { "arabext.NumberElements",
                new String[] {
                    "\u066b",
                    "\u066c",
                    "\u061b",
                    "\u066a",
                    "\u06f0",
                    "#",
                    "-",
                    "\u00d7\u06f1\u06f0^",
                    "\u0609",
                    "\u221e",
                    "NaN",
                }
            },
            { "bali.NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "\u1b50",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "beng.NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "\u09e6",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "cham.NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "\uaa50",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "deva.NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "\u0966",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "fullwide.NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "\uff10",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "gujr.NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "\u0ae6",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "guru.NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "\u0a66",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "java.NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "\ua9d0",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "kali.NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "\ua900",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "khmr.NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "\u17e0",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "knda.NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "\u0ce6",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "laoo.NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "\u0ed0",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "lana.NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "\u1a80",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "lanatham.NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "\u1a90",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "latn.NumberElements",
                new String[] {
                    ".", // decimal separator
                    ",", // group (thousands) separator
                    ";", // list separator
                    "%", // percent sign
                    "0", // native 0 digit
                    "#", // pattern digit
                    "-", // minus sign
                    "E", // exponential
                    "\u2030", // per mille
                    "\u221e", // infinity
                    "\ufffd" // NaN
                }
            },
            { "lepc.NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "\u1c40",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "limb.NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "\u1946",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "mlym.NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "\u0d66",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "mong.NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "\u1810",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "mtei.NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "\uabf0",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "mymr.NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "\u1040",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "mymrshan.NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "\u1090",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "nkoo.NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "\u07c0",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "olck.NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "\u1c50",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "orya.NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "\u0b66",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "saur.NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "\ua8d0",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "sund.NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "\u1bb0",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "talu.NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "\u19d0",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "tamldec.NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "\u0be6",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "telu.NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "\u0c66",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "thai.NumberElements",
                new String[] {
                    ".", // decimal separator
                    ",", // group (thousands) separator
                    ";", // list separator
                    "%", // percent sign
                    "\u0E50", // native 0 digit
                    "#", // pattern digit
                    "-", // minus sign
                    "E", // exponential
                    "\u2030", // per mille
                    "\u221e", // infinity
                    "\ufffd" // NaN
                }
            },
            { "tibt.NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "\u0f20",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "vaii.NumberElements",
                new String[] {
                    ".",
                    ",",
                    ";",
                    "%",
                    "\ua620",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "TimePatterns",
                new String[] {
                    "h:mm:ss a z",        // full time pattern
                    "h:mm:ss a z",        // long time pattern
                    "h:mm:ss a",          // medium time pattern
                    "h:mm a",             // short time pattern
                }
            },
            { "DatePatterns",
                new String[] {
                    "EEEE, MMMM d, yyyy", // full date pattern
                    "MMMM d, yyyy",       // long date pattern
                    "MMM d, yyyy",        // medium date pattern
                    "M/d/yy",             // short date pattern
                }
            },
            { "DateTimePatterns",
                new String[] {
                    "{1} {0}"             // date-time pattern
                }
            },
            { "buddhist.TimePatterns",
                new String[] {
                    "H:mm:ss z",          // full time pattern
                    "H:mm:ss z",          // long time pattern
                    "H:mm:ss",            // medium time pattern
                    "H:mm",               // short time pattern
                }
            },
            { "buddhist.DatePatterns",
                new String[] {
                    "EEEE d MMMM G yyyy", // full date pattern
                    "d MMMM yyyy",        // long date pattern
                    "d MMM yyyy",         // medium date pattern
                    "d/M/yyyy",           // short date pattern
                }
            },
            { "buddhist.DateTimePatterns",
                new String[] {
                    "{1}, {0}"            // date-time pattern
                }
            },
            { "japanese.TimePatterns",
                new String[] {
                    "h:mm:ss a z",             // full time pattern
                    "h:mm:ss a z",             // long time pattern
                    "h:mm:ss a",               // medium time pattern
                    "h:mm a",                  // short time pattern
                }
            },
            { "japanese.DatePatterns",
                new String[] {
                    "GGGG yyyy MMMM d (EEEE)", // full date pattern
                    "GGGG yyyy MMMM d",        // long date pattern
                    "GGGG yyyy MMM d",         // medium date pattern
                    "Gy.MM.dd",                // short date pattern
                }
            },
            { "japanese.DateTimePatterns",
                new String[] {
                    "{1} {0}"                  // date-time pattern
                }
            },
            { "DateTimePatternChars", "GyMdkHmsSEDFwWahKzZ" },
        };
    }
}
