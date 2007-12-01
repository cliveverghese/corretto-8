/*
 * Copyright 2000-2001 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.imageio.spi;

import java.io.File;
import java.util.Locale;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.FileImageInputStream;

public class FileImageInputStreamSpi extends ImageInputStreamSpi {

    private static final String vendorName = "Sun Microsystems, Inc.";

    private static final String version = "1.0";

    private static final Class inputClass = File.class;

    public FileImageInputStreamSpi() {
        super(vendorName, version, inputClass);
    }

    public String getDescription(Locale locale) {
        return "Service provider that instantiates a FileImageInputStream from a File";
    }

    public ImageInputStream createInputStreamInstance(Object input,
                                                      boolean useCache,
                                                      File cacheDir) {
        if (input instanceof File) {
            try {
                return new FileImageInputStream((File)input);
            } catch (Exception e) {
                return null;
            }
        } else {
            throw new IllegalArgumentException();
        }
    }
}
