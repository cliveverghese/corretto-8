/*
 * Copyright (c) 2000, 2006, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jmx.snmp.agent;

// java imports
//
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

// jmx imports
//
import com.sun.jmx.snmp.SnmpOid;
import com.sun.jmx.snmp.SnmpValue;
import com.sun.jmx.snmp.SnmpVarBind;
import com.sun.jmx.snmp.SnmpStatusException;

// SNMP Runtime imports
//

/**
 * <p>
 * This class is a utility class that transform SNMP GET / SET requests
 * into series of get<i>AttributeName</i>() set<i>AttributeName</i>()
 * invoked on the MBean.
 * </p>
 *
 * <p>
 * The transformation relies on the metadata information provided by the
 * {@link com.sun.jmx.snmp.agent.SnmpStandardMetaServer} object which is
 * passed as first parameter to every method. This SnmpStandardMetaServer
 * object is usually a Metadata object generated by <code>mibgen</code>.
 * </p>
 *
 * <p>
 * The MBean is not invoked directly by this class but through the
 * metadata object which holds a reference on it.
 * </p>
 *
 * <p><b><i>
 * This class is used internally by mibgen generated metadata objects and
 * you should never need to use it directly.
 * </b></i></p>
 * <p><b>This API is a Sun Microsystems internal API  and is subject
 * to change without notice.</b></p>
 **/

public class SnmpStandardObjectServer implements Serializable {
    private static final long serialVersionUID = -4641068116505308488L;

    /**
     * Generic handling of the <CODE>get</CODE> operation.
     * <p> The default implementation of this method is to loop over the
     * varbind list associated with the sub-request and to call
     * <CODE>get(var.oid.getOidArc(depth), data);</CODE>
     * <pre>
     * public void get(SnmpStandardMetaServer meta, SnmpMibSubRequest req,
     *                 int depth)
     *    throws SnmpStatusException {
     *
     *    final Object data = req.getUserData();
     *
     *    for (Enumeration e= req.getElements(); e.hasMoreElements();) {
     *
     *        final SnmpVarBind var= (SnmpVarBind) e.nextElement();
     *
     *        try {
     *            // This method will generate a SnmpStatusException
     *            // if `depth' is out of bounds.
     *            //
     *            final long id = var.oid.getOidArc(depth);
     *            var.value = meta.get(id, data);
     *        } catch(SnmpStatusException x) {
     *            req.registerGetException(var,x);
     *        }
     *    }
     * }
     * </pre>
     * <p> You can override this method if you need to implement some
     * specific policies for minimizing the accesses made to some remote
     * underlying resources.
     * <p>
     *
     * @param meta  A pointer to the generated meta-data object which
     *              implements the <code>SnmpStandardMetaServer</code>
     *              interface.
     *
     * @param req   The sub-request that must be handled by this node.
     *
     * @param depth The depth reached in the OID tree.
     *
     * @exception SnmpStatusException An error occurred while accessing
     *  the MIB node.
     */
    public void get(SnmpStandardMetaServer meta, SnmpMibSubRequest req,
                    int depth)
        throws SnmpStatusException {

        final Object data = req.getUserData();

        for (Enumeration e= req.getElements(); e.hasMoreElements();) {
            final SnmpVarBind var= (SnmpVarBind) e.nextElement();
            try {
                final long id = var.oid.getOidArc(depth);
                var.value = meta.get(id, data);
            } catch(SnmpStatusException x) {
                req.registerGetException(var,x);
            }
        }
    }

    /**
     * Generic handling of the <CODE>set</CODE> operation.
     * <p> The default implementation of this method is to loop over the
     * varbind list associated with the sub-request and to call
     * <CODE>set(var.value, var.oid.getOidArc(depth), data);</CODE>
     * <pre>
     * public void set(SnmpStandardMetaServer meta, SnmpMibSubRequest req,
     *                 int depth)
     *    throws SnmpStatusException {
     *
     *    final Object data = req.getUserData();
     *
     *    for (Enumeration e= req.getElements(); e.hasMoreElements();) {
     *
     *        final SnmpVarBind var= (SnmpVarBind) e.nextElement();
     *
     *        try {
     *            // This method will generate a SnmpStatusException
     *            // if `depth' is out of bounds.
     *            //
     *            final long id = var.oid.getOidArc(depth);
     *            var.value = meta.set(var.value, id, data);
     *        } catch(SnmpStatusException x) {
     *            req.registerSetException(var,x);
     *        }
     *    }
     * }
     * </pre>
     * <p> You can override this method if you need to implement some
     * specific policies for minimizing the accesses made to some remote
     * underlying resources.
     * <p>
     *
     * @param meta  A pointer to the generated meta-data object which
     *              implements the <code>SnmpStandardMetaServer</code>
     *              interface.
     *
     * @param req   The sub-request that must be handled by this node.
     *
     * @param depth The depth reached in the OID tree.
     *
     * @exception SnmpStatusException An error occurred while accessing
     *  the MIB node.
     */
    public void set(SnmpStandardMetaServer meta, SnmpMibSubRequest req,
                    int depth)
        throws SnmpStatusException {

        final Object data = req.getUserData();

        for (Enumeration e= req.getElements(); e.hasMoreElements();) {
            SnmpVarBind var = null;
            var = (SnmpVarBind) e.nextElement();
            try {
                // This method will generate a SnmpStatusException
                // if `depth' is out of bounds.
                //
                final long id = var.oid.getOidArc(depth);
                var.value = meta.set(var.value, id, data);
            } catch(SnmpStatusException x) {
                req.registerSetException(var,x);
            }
        }
    }

    /**
     * Generic handling of the <CODE>check</CODE> operation.
     * <p> The default implementation of this method is to loop over the
     * varbind list associated with the sub-request and to call
     * <CODE>check(var.value, var.oid.getOidArc(depth), data);</CODE>
     * <pre>
     * public void check(SnmpStandardMetaServer meta, SnmpMibSubRequest req,
     *                   int depth)
     *    throws SnmpStatusException {
     *
     *    final Object data = req.getUserData();
     *
     *    for (Enumeration e= req.getElements(); e.hasMoreElements();) {
     *
     *        final SnmpVarBind var= (SnmpVarBind) e.nextElement();
     *
     *        try {
     *            // This method will generate a SnmpStatusException
     *            // if `depth' is out of bounds.
     *            //
     *            final long id = var.oid.getOidArc(depth);
     *            meta.check(var.value, id, data);
     *        } catch(SnmpStatusException x) {
     *            req.registerCheckException(var,x);
     *        }
     *    }
     * }
     * </pre>
     * <p> You can override this method if you need to implement some
     * specific policies for minimizing the accesses made to some remote
     * underlying resources, or if you need to implement some consistency
     * checks between the different values provided in the varbind list.
     * <p>
     *
     * @param meta  A pointer to the generated meta-data object which
     *              implements the <code>SnmpStandardMetaServer</code>
     *              interface.
     *
     * @param req   The sub-request that must be handled by this node.
     *
     * @param depth The depth reached in the OID tree.
     *
     * @exception SnmpStatusException An error occurred while accessing
     *  the MIB node.
     */
    public void check(SnmpStandardMetaServer meta, SnmpMibSubRequest req,
                      int depth)
        throws SnmpStatusException {

        final Object data = req.getUserData();

        for (Enumeration e= req.getElements(); e.hasMoreElements();) {
            final SnmpVarBind var = (SnmpVarBind) e.nextElement();
            try {
                // This method will generate a SnmpStatusException
                // if `depth' is out of bounds.
                //
                final long id = var.oid.getOidArc(depth);
                meta.check(var.value,id,data);
            } catch(SnmpStatusException x) {
                req.registerCheckException(var,x);
            }
        }
    }
}
