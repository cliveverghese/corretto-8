/*
 * Copyright 2003 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
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


package sun.io;

import sun.nio.cs.ext.JIS_X_0208_Solaris_Encoder;

/**
 * Tables and data to convert Unicode to JIS0208_Solaris
 *
 *
 * Vendor defined chars added for benefit of vendor defined character
 * supplemented mappings for EUC-JP-Solaris/PCK Solaris variants of EUC-JP
 * and SJIS/Shift_JIS (4765370)
 *
 * @author  ConverterGenerator tool
 */

public class CharToByteJIS0208_Solaris extends CharToByteDoubleByte {

    public String getCharacterEncoding() {
        return "JIS0208_Solaris";
    }

    public CharToByteJIS0208_Solaris() {
        super.index1 = JIS_X_0208_Solaris_Encoder.getIndex1();
        super.index2 = JIS_X_0208_Solaris_Encoder.getIndex2();
    }
}
