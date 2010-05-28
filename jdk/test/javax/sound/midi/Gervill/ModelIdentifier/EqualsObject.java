/*
 * Copyright (c) 2007, Oracle and/or its affiliates. All rights reserved.
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

/* @test
   @summary Test ModelIdentifier equals method */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.sound.sampled.*;

import com.sun.media.sound.*;

public class EqualsObject {

    public static void main(String[] args) throws Exception {
        ModelIdentifier id = new ModelIdentifier("test","a",1);
        ModelIdentifier id2 = new ModelIdentifier("test","a",1);
        ModelIdentifier id3 = new ModelIdentifier("test","a",2);
        ModelIdentifier id4 = new ModelIdentifier("test","b",1);
        ModelIdentifier id5 = new ModelIdentifier("hello","a",1);
        if(!id.equals(id2))
            throw new RuntimeException("Compare failed!");
        if(id.equals(id3))
            throw new RuntimeException("Compare failed!");
        if(id.equals(id4))
            throw new RuntimeException("Compare failed!");
        if(id.equals(id5))
            throw new RuntimeException("Compare failed!");
    }
}
