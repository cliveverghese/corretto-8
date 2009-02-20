#
# Copyright 2008-2009 Sun Microsystems, Inc.  All Rights Reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# This code is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License version 2 only, as
# published by the Free Software Foundation.
#
# This code is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
# version 2 for more details (a copy is included in the LICENSE file that
# accompanied this code).
#
# You should have received a copy of the GNU General Public License version
# 2 along with this work; if not, write to the Free Software Foundation,
# Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
#
# Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
# CA 95054 USA or visit www.sun.com if you need additional information or
# have any questions.
#

# @test
# @bug 4607272
# @summary Unit test for AsynchronousChannelGrou#execute
# @build AsExecutor PrivilegedThreadFactory Attack
# @run shell run_any_task.sh

# if TESTJAVA isn't set then we assume an interactive run.

if [ -z "$TESTJAVA" ]; then
    TESTSRC=.
    TESTCLASSES=.
    JAVA=java
    JAR=jar
else
    JAVA="${TESTJAVA}/bin/java"
    JAR="${TESTJAVA}/bin/jar"
fi

echo "Creating JAR file ..."
$JAR -cf "${TESTCLASSES}/Privileged.jar" \
    -C "${TESTCLASSES}" PrivilegedThreadFactory.class \
    -C "${TESTCLASSES}" PrivilegedThreadFactory\$1.class \
    -C "${TESTCLASSES}" Attack.class

echo "Running test ..."
$JAVA -XX:-UseVMInterruptibleIO \
    -Xbootclasspath/a:"${TESTCLASSES}/Privileged.jar" \
    -classpath "${TESTCLASSES}" \
    AsExecutor
