/*
 * Copyright 2000 Sun Microsystems, Inc.  All Rights Reserved.
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
 *
 */

package sun.jvm.hotspot.types.basic;

import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.types.*;

/** A specialization of Field which represents a field containing a
    Java int value (in either a C/C++ data structure or a Java
    object) and which adds typechecked getValue() routines returning
    ints. */

public class BasicJIntField extends BasicField implements JIntField {
  public BasicJIntField(BasicTypeDataBase db, Type containingType, String name, Type type,
                          boolean isStatic, long offset, Address staticFieldAddress) {
    super(db, containingType, name, type, isStatic, offset, staticFieldAddress);

    if (!type.equals(db.getJIntType())) {
      throw new WrongTypeException("Type of a BasicJIntField must be equal to TypeDataBase.getJIntType()");
    }
  }

  /** The field must be nonstatic and the type of the field must be a
      Java int, or a WrongTypeException will be thrown. */
  public int getValue(Address addr) throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return getJInt(addr);
  }

  /** The field must be static and the type of the field must be a
      Java int, or a WrongTypeException will be thrown. */
  public int getValue() throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return getJInt();
  }
}
