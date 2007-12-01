/*
 * Copyright 2003 Sun Microsystems, Inc.  All Rights Reserved.
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
 * @bug 4854354
 * @summary Test vector write faster than can be read
 * @library ..
 */

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import sun.misc.*;


public class Write {

    static int port = 40170;

    static Random generator = new Random();

    static int testSize = 15;

    public static void main(String[] args) throws Exception {
        WriteServer sv = new WriteServer();
        sv.start();
        do {
            try {
                Thread.currentThread().sleep(200);
            } catch (InterruptedException x) {
                if (sv.finish(8000) == 0)
                    throw new Exception("Failed: Error in server thread");
            }
        } while (!sv.ready);
        bufferTest();
        if (sv.finish(8000) == 0)
            throw new Exception("Failed" );
    }

    static void bufferTest() throws Exception {
        ByteBuffer[] bufs = new ByteBuffer[testSize];
        for(int i=0; i<testSize; i++) {
            String source =
                "a muchmuchmuchmuchmuchmuchmuchmuch larger buffer numbered " +
                i;
            bufs[i] = ByteBuffer.allocateDirect(source.length());
        }

        // Get a connection to the server
        InetAddress lh = InetAddress.getLocalHost();
        InetSocketAddress isa = new InetSocketAddress(lh, port);
        SocketChannel sc = SocketChannel.open();
        sc.connect(isa);
        sc.configureBlocking(false);

        // Try to overflow the socket buffer
        long total = 0;
        for (int i=0; i<100; i++) {
            long bytesWritten = sc.write(bufs);
            if (bytesWritten > 0)
                total += bytesWritten;
            for(int j=0; j<testSize; j++)
                bufs[j].rewind();
        }

        // Clean up
        sc.close();
    }

}


class WriteServer extends TestThread {

    static int port = 40170;

    static Random generator = new Random();

    volatile boolean ready = false;

    WriteServer() {
        super("WriteServer");
    }

    void go() throws Exception {
        bufferTest();
    }

    void bufferTest() throws Exception {
        ByteBuffer buf = ByteBuffer.allocateDirect(5);

        // Get a connection from client
        ServerSocketChannel ssc = ServerSocketChannel.open();
        SocketChannel sc = null;

        try {
            ssc.configureBlocking(false);
            InetAddress lh = InetAddress.getLocalHost();
            InetSocketAddress isa = new InetSocketAddress(lh, port);
            ssc.socket().bind(isa);
            ready = true;

            for (;;) {
                sc = ssc.accept();
                if (sc != null)
                    break;
                Thread.sleep(50);
            }

            // I'm a slow reader...
            Thread.sleep(3000);

        } finally {
            // Clean up
            ssc.close();
            if (sc != null)
                sc.close();
        }

    }

}
