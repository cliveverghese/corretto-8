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

/*
 * @test
 * @bug 6329508
 * @summary GSSName created as GSSName.NT_ANONYMOUS return isAnonymous() == false
 */

import org.ietf.jgss.Oid;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;

public class CtorTests2 {

    /* standalone interface */
    public static void main(String[] argv) throws Exception {
        try {
            GSSManager manager = GSSManager.getInstance();
            GSSName name = manager.createName("anonymous", GSSName.NT_ANONYMOUS);
            boolean anonymous = name.isAnonymous();
            if (anonymous == false) {
                throw new RuntimeException("GSSName.isAnonymous() returns false for GSSName.NT_ANONYMOUS");
            }
        } catch (GSSException e) {
            System.out.println("Not supported, ignored!");
        }
    }

}
