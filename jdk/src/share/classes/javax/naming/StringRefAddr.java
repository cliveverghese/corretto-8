/*
 * Copyright 1999 Sun Microsystems, Inc.  All Rights Reserved.
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

package javax.naming;

/**
 * This class represents the string form of the address of
 * a communications end-point.
 * It consists of a type that describes the communication mechanism
 * and a string contents specific to that communication mechanism.
 * The format and interpretation of
 * the address type and the contents of the address are based on
 * the agreement of three parties: the client that uses the address,
 * the object/server that can be reached using the address, and the
 * administrator or program that creates the address.
 *
 * <p> An example of a string reference address is a host name.
 * Another example of a string reference address is a URL.
 *
 * <p> A string reference address is immutable:
 * once created, it cannot be changed.  Multithreaded access to
 * a single StringRefAddr need not be synchronized.
 *
 * @author Rosanna Lee
 * @author Scott Seligman
 *
 * @see RefAddr
 * @see BinaryRefAddr
 * @since 1.3
 */

public class StringRefAddr extends RefAddr {
    /**
     * Contains the contents of this address.
     * Can be null.
     * @serial
     */
    private String contents;
    /**
      * Constructs a new instance of StringRefAddr using its address type
      * and contents.
      *
      * @param addrType A non-null string describing the type of the address.
      * @param addr The possibly null contents of the address in the form of a string.
      */
    public StringRefAddr(String addrType, String addr) {
        super(addrType);
        contents = addr;
    }

    /**
      * Retrieves the contents of this address. The result is a string.
      *
      * @return The possibly null address contents.
      */
    public Object getContent() {
        return contents;
    }

    /**
     * Use serialVersionUID from JNDI 1.1.1 for interoperability
     */
    private static final long serialVersionUID = -8913762495138505527L;
}
