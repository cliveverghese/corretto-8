/*
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
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
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * @test
 * @bug 4814140
 * @summary AppInputStream: read can block a close
 * @author Brad Wetmore
 */

import java.io.*;
import java.net.*;
import javax.net.ssl.*;

public class ReadBlocksClose {

    /*
     * =============================================================
     * Set the various variables needed for the tests, then
     * specify what tests to run on each side.
     */

    /*
     * Should we run the client or server in a separate thread?
     * Both sides can throw exceptions, but do you have a preference
     * as to which side should be the main thread.
     */
    static boolean separateServerThread = false;

    /*
     * Where do we find the keystores?
     */
    static String pathToStores = "../../../../../../../etc";
    static String keyStoreFile = "keystore";
    static String trustStoreFile = "truststore";
    static String passwd = "passphrase";

    /*
     * Is the server ready to serve?
     */
    volatile static boolean serverReady = false;

    /*
     * Turn on SSL debugging?
     */
    static boolean debug = false;

    /*
     * If the client or server is doing some kind of object creation
     * that the other side depends on, and that thread prematurely
     * exits, you may experience a hang.  The test harness will
     * terminate all hung threads after its timeout has expired,
     * currently 3 minutes by default, but you might try to be
     * smart about it....
     */

    /*
     * Define the server side of the test.
     *
     * If the server prematurely exits, serverReady will be set to true
     * to avoid infinite hangs.
     */
    void doServerSide() throws Exception {
        SSLServerSocketFactory sslssf =
            (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket sslServerSocket =
            (SSLServerSocket) sslssf.createServerSocket(serverPort);

        serverPort = sslServerSocket.getLocalPort();

        /*
         * Signal Client, we're ready for his connect.
         */
        serverReady = true;

        SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
        InputStream sslIS = sslSocket.getInputStream();
        OutputStream sslOS = sslSocket.getOutputStream();

        try {
            sslIS.read();
        } catch (IOException e) {
            // this is ok, we expect this to time out anyway if the bug
            // is not fixed.  This is just to make sure that we
            // don't inadvertantly fail.
        }

        sslSocket.close();
    }

    /*
     * Define the client side of the test.
     *
     * If the server prematurely exits, serverReady will be set to true
     * to avoid infinite hangs.
     */
    void doClientSide() throws Exception {

        /*
         * Wait for server to get started.
         */
        while (!serverReady) {
            Thread.sleep(50);
        }

        SSLSocketFactory sslsf =
            (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslSocket = (SSLSocket)
            sslsf.createSocket("localhost", serverPort);

        final InputStream sslIS = sslSocket.getInputStream();
        final OutputStream sslOS = sslSocket.getOutputStream();

        new Thread(new Runnable() {
            public void run() {
                try {
                    System.out.println("Closing Thread started");
                    Thread.sleep(3000);
                    System.out.println("Closing Thread closing");
                    sslIS.close();
                } catch (Exception e) {
                    RuntimeException rte =
                        new RuntimeException("Check this out");
                    rte.initCause(e);
                    throw rte;
                }
            }
        }).start();

        try {
            /*
             * This should timeout and fail the test
             */
            System.out.println("Client starting read");
            sslIS.read();
        } catch (IOException e) {
            // this is ok, we expect this to time out anyway if the bug
            // is not fixed.  This is just to make sure that we
            // don't inadvertantly fail.
        }

        sslSocket.close();
    }

    /*
     * =============================================================
     * The remainder is just support stuff
     */

    // use any free port by default
    volatile int serverPort = 0;

    volatile Exception serverException = null;
    volatile Exception clientException = null;

    public static void main(String[] args) throws Exception {
        String keyFilename =
            System.getProperty("test.src", "./") + "/" + pathToStores +
                "/" + keyStoreFile;
        String trustFilename =
            System.getProperty("test.src", "./") + "/" + pathToStores +
                "/" + trustStoreFile;

        System.setProperty("javax.net.ssl.keyStore", keyFilename);
        System.setProperty("javax.net.ssl.keyStorePassword", passwd);
        System.setProperty("javax.net.ssl.trustStore", trustFilename);
        System.setProperty("javax.net.ssl.trustStorePassword", passwd);

        if (debug)
            System.setProperty("javax.net.debug", "all");

        /*
         * Start the tests.
         */
        new ReadBlocksClose();
    }

    Thread clientThread = null;
    Thread serverThread = null;

    /*
     * Primary constructor, used to drive remainder of the test.
     *
     * Fork off the other side, then do your work.
     */
    ReadBlocksClose() throws Exception {
        try {
            if (separateServerThread) {
                startServer(true);
                startClient(false);
            } else {
                startClient(true);
                startServer(false);
            }
        } catch (Exception e) {
            // swallow for now.  Show later
        }

        /*
         * Wait for other side to close down.
         */
        if (separateServerThread) {
            serverThread.join();
        } else {
            clientThread.join();
        }

        /*
         * When we get here, the test is pretty much over.
         * Which side threw the error?
         */
        Exception local;
        Exception remote;
        String whichRemote;

        if (separateServerThread) {
            remote = serverException;
            local = clientException;
            whichRemote = "server";
        } else {
            remote = clientException;
            local = serverException;
            whichRemote = "client";
        }

        /*
         * If both failed, return the curthread's exception, but also
         * print the remote side Exception
         */
        if ((local != null) && (remote != null)) {
            System.out.println(whichRemote + " also threw:");
            remote.printStackTrace();
            System.out.println();
            throw local;
        }

        if (remote != null) {
            throw remote;
        }

        if (local != null) {
            throw local;
        }
    }

    void startServer(boolean newThread) throws Exception {
        if (newThread) {
            serverThread = new Thread() {
                public void run() {
                    try {
                        doServerSide();
                    } catch (Exception e) {
                        /*
                         * Our server thread just died.
                         *
                         * Release the client, if not active already...
                         */
                        System.err.println("Server died...");
                        serverReady = true;
                        serverException = e;
                    }
                }
            };
            serverThread.start();
        } else {
            try {
                doServerSide();
            } catch (Exception e) {
                serverException = e;
            } finally {
                serverReady = true;
            }
        }
    }

    void startClient(boolean newThread) throws Exception {
        if (newThread) {
            clientThread = new Thread() {
                public void run() {
                    try {
                        doClientSide();
                    } catch (Exception e) {
                        /*
                         * Our client thread just died.
                         */
                        System.err.println("Client died...");
                        clientException = e;
                    }
                }
            };
            clientThread.start();
        } else {
            try {
                doClientSide();
            } catch (Exception e) {
                clientException = e;
            }
        }
    }
}
