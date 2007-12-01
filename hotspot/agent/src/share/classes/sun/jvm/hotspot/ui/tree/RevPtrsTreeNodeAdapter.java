/*
 * Copyright 2002 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.jvm.hotspot.ui.tree;

import java.util.*;
import java.io.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.utilities.*;

/** Who directly points to this node. */

public class RevPtrsTreeNodeAdapter extends FieldTreeNodeAdapter {
  private static FieldIdentifier fid = new NamedFieldIdentifier("_revPtrs");

  private List children;

  public RevPtrsTreeNodeAdapter(Oop oop) {
    this(oop, false);
  }

  public RevPtrsTreeNodeAdapter(Oop oop, boolean treeTableMode) {
    super(fid, treeTableMode);
    children = VM.getVM().getRevPtrs().get(oop);
  }

  public int getChildCount() {
    return children != null ? children.size() : 0;
  }

  public SimpleTreeNode getChild(int index) {
    LivenessPathElement lpe = (LivenessPathElement)children.get(index);
    IndexableFieldIdentifier ifid = new IndexableFieldIdentifier(index);
    Oop oop = lpe.getObj();
    if (oop != null) {
      return new OopTreeNodeAdapter(oop, ifid, getTreeTableMode());
    } else {
      NamedFieldIdentifier nfi = (NamedFieldIdentifier)lpe.getField();
      return new RootTreeNodeAdapter(nfi.getName(), ifid, getTreeTableMode());
    }
  }

  public boolean isLeaf() {
    return false;
  }

  public int getIndexOfChild(SimpleTreeNode child) {
    FieldIdentifier id = ((FieldTreeNodeAdapter) child).getID();
    IndexableFieldIdentifier ifid = (IndexableFieldIdentifier)id;
    return ifid.getIndex();
  }

  public String getName()  { return "<<Reverse pointers>>"; }
  public String getValue() { return ""; }
}
