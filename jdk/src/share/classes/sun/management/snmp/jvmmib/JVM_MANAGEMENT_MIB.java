/*
 * Copyright 2003-2006 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.management.snmp.jvmmib;

//
// Generated by mibgen version 5.0 (06/02/03) when compiling JVM-MANAGEMENT-MIB in standard metadata mode.
//

// java imports
//
import java.io.Serializable;
import java.util.Hashtable;

// jmx imports
//
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.InstanceAlreadyExistsException;

// jdmk imports
//
import com.sun.jmx.snmp.agent.SnmpMib;
import com.sun.jmx.snmp.agent.SnmpMibNode;
import com.sun.jmx.snmp.agent.SnmpMibTable;
import com.sun.jmx.snmp.agent.SnmpStandardObjectServer;

/**
 * The class is used for representing "JVM-MANAGEMENT-MIB".
 * You can edit the file if you want to modify the behaviour of the MIB.
 */
public abstract class JVM_MANAGEMENT_MIB extends SnmpMib implements Serializable {

    /**
     * Default constructor. Initialize the Mib tree.
     */
    public JVM_MANAGEMENT_MIB() {
        mibName = "JVM_MANAGEMENT_MIB";
    }

    /**
     * Initialization of the MIB with no registration in Java DMK.
     */
    public void init() throws IllegalAccessException {
        // Allow only one initialization of the MIB.
        //
        if (isInitialized == true) {
            return ;
        }

        try  {
            populate(null, null);
        } catch(IllegalAccessException x)  {
            throw x;
        } catch(RuntimeException x)  {
            throw x;
        } catch(Exception x)  {
            throw new Error(x.getMessage());
        }

        isInitialized = true;
    }

    /**
     * Initialization of the MIB with AUTOMATIC REGISTRATION in Java DMK.
     */
    public ObjectName preRegister(MBeanServer server, ObjectName name)
            throws Exception {
        // Allow only one initialization of the MIB.
        //
        if (isInitialized == true) {
            throw new InstanceAlreadyExistsException();
        }

        // Initialize MBeanServer information.
        //
        this.server = server;

        populate(server, name);

        isInitialized = true;
        return name;
    }

    /**
     * Initialization of the MIB with no registration in Java DMK.
     */
    public void populate(MBeanServer server, ObjectName name)
        throws Exception {
        // Allow only one initialization of the MIB.
        //
        if (isInitialized == true) {
            return ;
        }

        if (objectserver == null)
            objectserver = new SnmpStandardObjectServer();

        // Initialization of the "JvmOS" group.
        // To disable support of this group, redefine the
        // "createJvmOSMetaNode()" factory method, and make it return "null"
        //
        initJvmOS(server);

        // Initialization of the "JvmCompilation" group.
        // To disable support of this group, redefine the
        // "createJvmCompilationMetaNode()" factory method, and make it return "null"
        //
        initJvmCompilation(server);

        // Initialization of the "JvmRuntime" group.
        // To disable support of this group, redefine the
        // "createJvmRuntimeMetaNode()" factory method, and make it return "null"
        //
        initJvmRuntime(server);

        // Initialization of the "JvmThreading" group.
        // To disable support of this group, redefine the
        // "createJvmThreadingMetaNode()" factory method, and make it return "null"
        //
        initJvmThreading(server);

        // Initialization of the "JvmMemory" group.
        // To disable support of this group, redefine the
        // "createJvmMemoryMetaNode()" factory method, and make it return "null"
        //
        initJvmMemory(server);

        // Initialization of the "JvmClassLoading" group.
        // To disable support of this group, redefine the
        // "createJvmClassLoadingMetaNode()" factory method, and make it return "null"
        //
        initJvmClassLoading(server);

        isInitialized = true;
    }


    // ------------------------------------------------------------
    //
    // Initialization of the "JvmOS" group.
    //
    // ------------------------------------------------------------


    /**
     * Initialization of the "JvmOS" group.
     *
     * To disable support of this group, redefine the
     * "createJvmOSMetaNode()" factory method, and make it return "null"
     *
     * @param server    MBeanServer for this group (may be null)
     *
     **/
    protected void initJvmOS(MBeanServer server)
        throws Exception {
        final String oid = getGroupOid("JvmOS", "1.3.6.1.4.1.42.2.145.3.163.1.1.6");
        ObjectName objname = null;
        if (server != null) {
            objname = getGroupObjectName("JvmOS", oid, mibName + ":name=sun.management.snmp.jvmmib.JvmOS");
        }
        final JvmOSMeta meta = createJvmOSMetaNode("JvmOS", oid, objname, server);
        if (meta != null) {
            meta.registerTableNodes( this, server );

            // Note that when using standard metadata,
            // the returned object must implement the "JvmOSMBean"
            // interface.
            //
            final JvmOSMBean group = (JvmOSMBean) createJvmOSMBean("JvmOS", oid, objname, server);
            meta.setInstance( group );
            registerGroupNode("JvmOS", oid, objname, meta, group, server);
        }
    }


    /**
     * Factory method for "JvmOS" group metadata class.
     *
     * You can redefine this method if you need to replace the default
     * generated metadata class with your own customized class.
     *
     * @param groupName Name of the group ("JvmOS")
     * @param groupOid  OID of this group
     * @param groupObjname ObjectName for this group (may be null)
     * @param server    MBeanServer for this group (may be null)
     *
     * @return An instance of the metadata class generated for the
     *         "JvmOS" group (JvmOSMeta)
     *
     **/
    protected JvmOSMeta createJvmOSMetaNode(String groupName,
                String groupOid, ObjectName groupObjname, MBeanServer server)  {
        return new JvmOSMeta(this, objectserver);
    }


    /**
     * Factory method for "JvmOS" group MBean.
     *
     * You can redefine this method if you need to replace the default
     * generated MBean class with your own customized class.
     *
     * @param groupName Name of the group ("JvmOS")
     * @param groupOid  OID of this group
     * @param groupObjname ObjectName for this group (may be null)
     * @param server    MBeanServer for this group (may be null)
     *
     * @return An instance of the MBean class generated for the
     *         "JvmOS" group (JvmOS)
     *
     * Note that when using standard metadata,
     * the returned object must implement the "JvmOSMBean"
     * interface.
     **/
    protected abstract Object createJvmOSMBean(String groupName,
                String groupOid,  ObjectName groupObjname, MBeanServer server);


    // ------------------------------------------------------------
    //
    // Initialization of the "JvmCompilation" group.
    //
    // ------------------------------------------------------------


    /**
     * Initialization of the "JvmCompilation" group.
     *
     * To disable support of this group, redefine the
     * "createJvmCompilationMetaNode()" factory method, and make it return "null"
     *
     * @param server    MBeanServer for this group (may be null)
     *
     **/
    protected void initJvmCompilation(MBeanServer server)
        throws Exception {
        final String oid = getGroupOid("JvmCompilation", "1.3.6.1.4.1.42.2.145.3.163.1.1.5");
        ObjectName objname = null;
        if (server != null) {
            objname = getGroupObjectName("JvmCompilation", oid, mibName + ":name=sun.management.snmp.jvmmib.JvmCompilation");
        }
        final JvmCompilationMeta meta = createJvmCompilationMetaNode("JvmCompilation", oid, objname, server);
        if (meta != null) {
            meta.registerTableNodes( this, server );

            // Note that when using standard metadata,
            // the returned object must implement the "JvmCompilationMBean"
            // interface.
            //
            final JvmCompilationMBean group = (JvmCompilationMBean) createJvmCompilationMBean("JvmCompilation", oid, objname, server);
            meta.setInstance( group );
            registerGroupNode("JvmCompilation", oid, objname, meta, group, server);
        }
    }


    /**
     * Factory method for "JvmCompilation" group metadata class.
     *
     * You can redefine this method if you need to replace the default
     * generated metadata class with your own customized class.
     *
     * @param groupName Name of the group ("JvmCompilation")
     * @param groupOid  OID of this group
     * @param groupObjname ObjectName for this group (may be null)
     * @param server    MBeanServer for this group (may be null)
     *
     * @return An instance of the metadata class generated for the
     *         "JvmCompilation" group (JvmCompilationMeta)
     *
     **/
    protected JvmCompilationMeta createJvmCompilationMetaNode(String groupName,
                String groupOid, ObjectName groupObjname, MBeanServer server)  {
        return new JvmCompilationMeta(this, objectserver);
    }


    /**
     * Factory method for "JvmCompilation" group MBean.
     *
     * You can redefine this method if you need to replace the default
     * generated MBean class with your own customized class.
     *
     * @param groupName Name of the group ("JvmCompilation")
     * @param groupOid  OID of this group
     * @param groupObjname ObjectName for this group (may be null)
     * @param server    MBeanServer for this group (may be null)
     *
     * @return An instance of the MBean class generated for the
     *         "JvmCompilation" group (JvmCompilation)
     *
     * Note that when using standard metadata,
     * the returned object must implement the "JvmCompilationMBean"
     * interface.
     **/
    protected abstract Object createJvmCompilationMBean(String groupName,
                String groupOid,  ObjectName groupObjname, MBeanServer server);


    // ------------------------------------------------------------
    //
    // Initialization of the "JvmRuntime" group.
    //
    // ------------------------------------------------------------


    /**
     * Initialization of the "JvmRuntime" group.
     *
     * To disable support of this group, redefine the
     * "createJvmRuntimeMetaNode()" factory method, and make it return "null"
     *
     * @param server    MBeanServer for this group (may be null)
     *
     **/
    protected void initJvmRuntime(MBeanServer server)
        throws Exception {
        final String oid = getGroupOid("JvmRuntime", "1.3.6.1.4.1.42.2.145.3.163.1.1.4");
        ObjectName objname = null;
        if (server != null) {
            objname = getGroupObjectName("JvmRuntime", oid, mibName + ":name=sun.management.snmp.jvmmib.JvmRuntime");
        }
        final JvmRuntimeMeta meta = createJvmRuntimeMetaNode("JvmRuntime", oid, objname, server);
        if (meta != null) {
            meta.registerTableNodes( this, server );

            // Note that when using standard metadata,
            // the returned object must implement the "JvmRuntimeMBean"
            // interface.
            //
            final JvmRuntimeMBean group = (JvmRuntimeMBean) createJvmRuntimeMBean("JvmRuntime", oid, objname, server);
            meta.setInstance( group );
            registerGroupNode("JvmRuntime", oid, objname, meta, group, server);
        }
    }


    /**
     * Factory method for "JvmRuntime" group metadata class.
     *
     * You can redefine this method if you need to replace the default
     * generated metadata class with your own customized class.
     *
     * @param groupName Name of the group ("JvmRuntime")
     * @param groupOid  OID of this group
     * @param groupObjname ObjectName for this group (may be null)
     * @param server    MBeanServer for this group (may be null)
     *
     * @return An instance of the metadata class generated for the
     *         "JvmRuntime" group (JvmRuntimeMeta)
     *
     **/
    protected JvmRuntimeMeta createJvmRuntimeMetaNode(String groupName,
                String groupOid, ObjectName groupObjname, MBeanServer server)  {
        return new JvmRuntimeMeta(this, objectserver);
    }


    /**
     * Factory method for "JvmRuntime" group MBean.
     *
     * You can redefine this method if you need to replace the default
     * generated MBean class with your own customized class.
     *
     * @param groupName Name of the group ("JvmRuntime")
     * @param groupOid  OID of this group
     * @param groupObjname ObjectName for this group (may be null)
     * @param server    MBeanServer for this group (may be null)
     *
     * @return An instance of the MBean class generated for the
     *         "JvmRuntime" group (JvmRuntime)
     *
     * Note that when using standard metadata,
     * the returned object must implement the "JvmRuntimeMBean"
     * interface.
     **/
    protected abstract Object createJvmRuntimeMBean(String groupName,
                String groupOid,  ObjectName groupObjname, MBeanServer server);


    // ------------------------------------------------------------
    //
    // Initialization of the "JvmThreading" group.
    //
    // ------------------------------------------------------------


    /**
     * Initialization of the "JvmThreading" group.
     *
     * To disable support of this group, redefine the
     * "createJvmThreadingMetaNode()" factory method, and make it return "null"
     *
     * @param server    MBeanServer for this group (may be null)
     *
     **/
    protected void initJvmThreading(MBeanServer server)
        throws Exception {
        final String oid = getGroupOid("JvmThreading", "1.3.6.1.4.1.42.2.145.3.163.1.1.3");
        ObjectName objname = null;
        if (server != null) {
            objname = getGroupObjectName("JvmThreading", oid, mibName + ":name=sun.management.snmp.jvmmib.JvmThreading");
        }
        final JvmThreadingMeta meta = createJvmThreadingMetaNode("JvmThreading", oid, objname, server);
        if (meta != null) {
            meta.registerTableNodes( this, server );

            // Note that when using standard metadata,
            // the returned object must implement the "JvmThreadingMBean"
            // interface.
            //
            final JvmThreadingMBean group = (JvmThreadingMBean) createJvmThreadingMBean("JvmThreading", oid, objname, server);
            meta.setInstance( group );
            registerGroupNode("JvmThreading", oid, objname, meta, group, server);
        }
    }


    /**
     * Factory method for "JvmThreading" group metadata class.
     *
     * You can redefine this method if you need to replace the default
     * generated metadata class with your own customized class.
     *
     * @param groupName Name of the group ("JvmThreading")
     * @param groupOid  OID of this group
     * @param groupObjname ObjectName for this group (may be null)
     * @param server    MBeanServer for this group (may be null)
     *
     * @return An instance of the metadata class generated for the
     *         "JvmThreading" group (JvmThreadingMeta)
     *
     **/
    protected JvmThreadingMeta createJvmThreadingMetaNode(String groupName,
                String groupOid, ObjectName groupObjname, MBeanServer server)  {
        return new JvmThreadingMeta(this, objectserver);
    }


    /**
     * Factory method for "JvmThreading" group MBean.
     *
     * You can redefine this method if you need to replace the default
     * generated MBean class with your own customized class.
     *
     * @param groupName Name of the group ("JvmThreading")
     * @param groupOid  OID of this group
     * @param groupObjname ObjectName for this group (may be null)
     * @param server    MBeanServer for this group (may be null)
     *
     * @return An instance of the MBean class generated for the
     *         "JvmThreading" group (JvmThreading)
     *
     * Note that when using standard metadata,
     * the returned object must implement the "JvmThreadingMBean"
     * interface.
     **/
    protected abstract Object createJvmThreadingMBean(String groupName,
                String groupOid,  ObjectName groupObjname, MBeanServer server);


    // ------------------------------------------------------------
    //
    // Initialization of the "JvmMemory" group.
    //
    // ------------------------------------------------------------


    /**
     * Initialization of the "JvmMemory" group.
     *
     * To disable support of this group, redefine the
     * "createJvmMemoryMetaNode()" factory method, and make it return "null"
     *
     * @param server    MBeanServer for this group (may be null)
     *
     **/
    protected void initJvmMemory(MBeanServer server)
        throws Exception {
        final String oid = getGroupOid("JvmMemory", "1.3.6.1.4.1.42.2.145.3.163.1.1.2");
        ObjectName objname = null;
        if (server != null) {
            objname = getGroupObjectName("JvmMemory", oid, mibName + ":name=sun.management.snmp.jvmmib.JvmMemory");
        }
        final JvmMemoryMeta meta = createJvmMemoryMetaNode("JvmMemory", oid, objname, server);
        if (meta != null) {
            meta.registerTableNodes( this, server );

            // Note that when using standard metadata,
            // the returned object must implement the "JvmMemoryMBean"
            // interface.
            //
            final JvmMemoryMBean group = (JvmMemoryMBean) createJvmMemoryMBean("JvmMemory", oid, objname, server);
            meta.setInstance( group );
            registerGroupNode("JvmMemory", oid, objname, meta, group, server);
        }
    }


    /**
     * Factory method for "JvmMemory" group metadata class.
     *
     * You can redefine this method if you need to replace the default
     * generated metadata class with your own customized class.
     *
     * @param groupName Name of the group ("JvmMemory")
     * @param groupOid  OID of this group
     * @param groupObjname ObjectName for this group (may be null)
     * @param server    MBeanServer for this group (may be null)
     *
     * @return An instance of the metadata class generated for the
     *         "JvmMemory" group (JvmMemoryMeta)
     *
     **/
    protected JvmMemoryMeta createJvmMemoryMetaNode(String groupName,
                String groupOid, ObjectName groupObjname, MBeanServer server)  {
        return new JvmMemoryMeta(this, objectserver);
    }


    /**
     * Factory method for "JvmMemory" group MBean.
     *
     * You can redefine this method if you need to replace the default
     * generated MBean class with your own customized class.
     *
     * @param groupName Name of the group ("JvmMemory")
     * @param groupOid  OID of this group
     * @param groupObjname ObjectName for this group (may be null)
     * @param server    MBeanServer for this group (may be null)
     *
     * @return An instance of the MBean class generated for the
     *         "JvmMemory" group (JvmMemory)
     *
     * Note that when using standard metadata,
     * the returned object must implement the "JvmMemoryMBean"
     * interface.
     **/
    protected abstract Object createJvmMemoryMBean(String groupName,
                String groupOid,  ObjectName groupObjname, MBeanServer server);


    // ------------------------------------------------------------
    //
    // Initialization of the "JvmClassLoading" group.
    //
    // ------------------------------------------------------------


    /**
     * Initialization of the "JvmClassLoading" group.
     *
     * To disable support of this group, redefine the
     * "createJvmClassLoadingMetaNode()" factory method, and make it return "null"
     *
     * @param server    MBeanServer for this group (may be null)
     *
     **/
    protected void initJvmClassLoading(MBeanServer server)
        throws Exception {
        final String oid = getGroupOid("JvmClassLoading", "1.3.6.1.4.1.42.2.145.3.163.1.1.1");
        ObjectName objname = null;
        if (server != null) {
            objname = getGroupObjectName("JvmClassLoading", oid, mibName + ":name=sun.management.snmp.jvmmib.JvmClassLoading");
        }
        final JvmClassLoadingMeta meta = createJvmClassLoadingMetaNode("JvmClassLoading", oid, objname, server);
        if (meta != null) {
            meta.registerTableNodes( this, server );

            // Note that when using standard metadata,
            // the returned object must implement the "JvmClassLoadingMBean"
            // interface.
            //
            final JvmClassLoadingMBean group = (JvmClassLoadingMBean) createJvmClassLoadingMBean("JvmClassLoading", oid, objname, server);
            meta.setInstance( group );
            registerGroupNode("JvmClassLoading", oid, objname, meta, group, server);
        }
    }


    /**
     * Factory method for "JvmClassLoading" group metadata class.
     *
     * You can redefine this method if you need to replace the default
     * generated metadata class with your own customized class.
     *
     * @param groupName Name of the group ("JvmClassLoading")
     * @param groupOid  OID of this group
     * @param groupObjname ObjectName for this group (may be null)
     * @param server    MBeanServer for this group (may be null)
     *
     * @return An instance of the metadata class generated for the
     *         "JvmClassLoading" group (JvmClassLoadingMeta)
     *
     **/
    protected JvmClassLoadingMeta createJvmClassLoadingMetaNode(String groupName,
                String groupOid, ObjectName groupObjname, MBeanServer server)  {
        return new JvmClassLoadingMeta(this, objectserver);
    }


    /**
     * Factory method for "JvmClassLoading" group MBean.
     *
     * You can redefine this method if you need to replace the default
     * generated MBean class with your own customized class.
     *
     * @param groupName Name of the group ("JvmClassLoading")
     * @param groupOid  OID of this group
     * @param groupObjname ObjectName for this group (may be null)
     * @param server    MBeanServer for this group (may be null)
     *
     * @return An instance of the MBean class generated for the
     *         "JvmClassLoading" group (JvmClassLoading)
     *
     * Note that when using standard metadata,
     * the returned object must implement the "JvmClassLoadingMBean"
     * interface.
     **/
    protected abstract Object createJvmClassLoadingMBean(String groupName,
                String groupOid,  ObjectName groupObjname, MBeanServer server);


    // ------------------------------------------------------------
    //
    // Implements the "registerTableMeta" method defined in "SnmpMib".
    // See the "SnmpMib" Javadoc API for more details.
    //
    // ------------------------------------------------------------

    public void registerTableMeta( String name, SnmpMibTable meta) {
        if (metadatas == null) return;
        if (name == null) return;
        metadatas.put(name,meta);
    }


    // ------------------------------------------------------------
    //
    // Implements the "getRegisteredTableMeta" method defined in "SnmpMib".
    // See the "SnmpMib" Javadoc API for more details.
    //
    // ------------------------------------------------------------

    public SnmpMibTable getRegisteredTableMeta( String name ) {
        if (metadatas == null) return null;
        if (name == null) return null;
        return metadatas.get(name);
    }

    public SnmpStandardObjectServer getStandardObjectServer() {
        if (objectserver == null)
            objectserver = new SnmpStandardObjectServer();
        return objectserver;
    }

    private boolean isInitialized = false;

    protected SnmpStandardObjectServer objectserver;

    protected final Hashtable<String, SnmpMibTable> metadatas =
            new Hashtable<String, SnmpMibTable>();
}
