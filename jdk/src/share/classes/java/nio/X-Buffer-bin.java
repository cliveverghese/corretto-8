/*
 * Copyright 2000-2002 Sun Microsystems, Inc.  All Rights Reserved.
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

#warn This file is preprocessed before being compiled

class XXX {

#begin

    /**
     * Relative <i>get</i> method for reading $a$ $type$ value.
     *
     * <p> Reads the next $nbytes$ bytes at this buffer's current position,
     * composing them into $a$ $type$ value according to the current byte order,
     * and then increments the position by $nbytes$.  </p>
     *
     * @return  The $type$ value at the buffer's current position
     *
     * @throws  BufferUnderflowException
     *          If there are fewer than $nbytes$ bytes
     *          remaining in this buffer
     */
    public abstract $type$ get$Type$();

    /**
     * Relative <i>put</i> method for writing $a$ $type$
     * value&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> Writes $nbytes$ bytes containing the given $type$ value, in the
     * current byte order, into this buffer at the current position, and then
     * increments the position by $nbytes$.  </p>
     *
     * @param  value
     *         The $type$ value to be written
     *
     * @return  This buffer
     *
     * @throws  BufferOverflowException
     *          If there are fewer than $nbytes$ bytes
     *          remaining in this buffer
     *
     * @throws  ReadOnlyBufferException
     *          If this buffer is read-only
     */
    public abstract ByteBuffer put$Type$($type$ value);

    /**
     * Absolute <i>get</i> method for reading $a$ $type$ value.
     *
     * <p> Reads $nbytes$ bytes at the given index, composing them into a
     * $type$ value according to the current byte order.  </p>
     *
     * @param  index
     *         The index from which the bytes will be read
     *
     * @return  The $type$ value at the given index
     *
     * @throws  IndexOutOfBoundsException
     *          If <tt>index</tt> is negative
     *          or not smaller than the buffer's limit,
     *          minus $nbytesButOne$
     */
    public abstract $type$ get$Type$(int index);

    /**
     * Absolute <i>put</i> method for writing $a$ $type$
     * value&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> Writes $nbytes$ bytes containing the given $type$ value, in the
     * current byte order, into this buffer at the given index.  </p>
     *
     * @param  index
     *         The index at which the bytes will be written
     *
     * @param  value
     *         The $type$ value to be written
     *
     * @return  This buffer
     *
     * @throws  IndexOutOfBoundsException
     *          If <tt>index</tt> is negative
     *          or not smaller than the buffer's limit,
     *          minus $nbytesButOne$
     *
     * @throws  ReadOnlyBufferException
     *          If this buffer is read-only
     */
    public abstract ByteBuffer put$Type$(int index, $type$ value);

    /**
     * Creates a view of this byte buffer as $a$ $type$ buffer.
     *
     * <p> The content of the new buffer will start at this buffer's current
     * position.  Changes to this buffer's content will be visible in the new
     * buffer, and vice versa; the two buffers' position, limit, and mark
     * values will be independent.
     *
     * <p> The new buffer's position will be zero, its capacity and its limit
     * will be the number of bytes remaining in this buffer divided by
     * $nbytes$, and its mark will be undefined.  The new buffer will be direct
     * if, and only if, this buffer is direct, and it will be read-only if, and
     * only if, this buffer is read-only.  </p>
     *
     * @return  A new $type$ buffer
     */
    public abstract $Type$Buffer as$Type$Buffer();

#end

}
