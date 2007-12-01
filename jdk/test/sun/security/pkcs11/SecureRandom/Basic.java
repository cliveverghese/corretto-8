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
 * @test
 * @bug 6246411
 * @summary basic test for PKCS#11 SecureRandom
 * @author Andreas Sterbenz
 * @library ..
 */

import java.io.*;
import java.util.*;

import java.security.*;

import javax.crypto.*;

public class Basic extends PKCS11Test {

    public void main(Provider p) throws Exception {
        SecureRandom random;
        try {
            random = SecureRandom.getInstance("PKCS11");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Provider " + p + " does not support SecureRandom, skipping");
            e.printStackTrace();
            return;
        }
        byte[] b = new byte[32];
        random.nextBytes(b);
        System.out.println(toString(b));
        random.setSeed(b);
        random.nextBytes(b);
        System.out.println(toString(b));
        System.out.println("OK");
    }

    public static void main(String[] args) throws Exception {
        main(new Basic());
    }

}
