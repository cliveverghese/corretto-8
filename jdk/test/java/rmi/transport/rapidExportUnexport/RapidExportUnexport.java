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

/* @test
 * @bug 6275081
 * @summary Rapidly alternating the number of remote objects exported
 * on an explicit port between zero and greater than zero (thus
 * causing the associated server socket to be created and closed
 * repeatedly) should not encounter substantial synchronous delays
 * because of the server socket accept loop's failure throttling
 * procedure (which sleeps 10 seconds after 10 rapid failures).
 * @author Peter Jones
 *
 * @build RapidExportUnexport
 * @run main/othervm RapidExportUnexport
 */

import java.rmi.Remote;
import java.rmi.server.UnicastRemoteObject;

public class RapidExportUnexport {

    private static final int PORT = 2055;

    private static final int REPS = 100;
    private static final long TIMEOUT = 60000;

    public static void main(String[] args) throws Exception {
	System.err.println("\nRegression test for bug 6275081\n");

	Remote impl = new Remote() { };
	long start = System.currentTimeMillis();
	for (int i = 0; i < REPS; i++) {
	    System.err.println(i);
	    UnicastRemoteObject.exportObject(impl, PORT);
	    UnicastRemoteObject.unexportObject(impl, true);
	    Thread.sleep(1);	// work around BindException (bug?)
	}
	long delta = System.currentTimeMillis() - start;
	System.err.println(REPS + " export/unexport operations took " +
			   delta + "ms");
	if (delta > TIMEOUT) {
	    throw new Error("TEST FAILED: took over " + TIMEOUT + "ms");
	}
	System.err.println("TEST PASSED");
    }
}
