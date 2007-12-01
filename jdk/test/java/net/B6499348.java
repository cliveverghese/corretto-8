/*
 * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
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
 * @bug 6499348
 * @summary java.net socket classes should implement java.io.Closeable
 */

import java.net.*;
import java.io.Closeable;

public class B6499348 {
    public static void main(String[] args) throws java.io.IOException {
        Socket s = new Socket();
        ServerSocket ss = new ServerSocket();
        DatagramSocket ds =  new DatagramSocket((SocketAddress) null);

        if (! (s instanceof Closeable))
            throw new RuntimeException("Socket is not a java.io.Closeable");
        if (! (ss instanceof Closeable))
            throw new RuntimeException("ServerSocket is not a java.io.Closeable");
        if (! (ds instanceof Closeable))
            throw new RuntimeException("DatagramSocket is not a java.io.Closeable");
        s.close();
        ss.close();
        ds.close();
    }
}
