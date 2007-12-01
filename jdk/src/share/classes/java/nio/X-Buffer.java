/*
 * Copyright 2000-2007 Sun Microsystems, Inc.  All Rights Reserved.
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

package java.nio;

#if[char]
import java.io.IOException;
#end[char]

/**
 * $A$ $fulltype$ buffer.
 *
 * <p> This class defines {#if[byte]?six:four} categories of operations upon
 * $fulltype$ buffers:
 *
 * <ul>
 *
 *   <li><p> Absolute and relative {@link #get() </code><i>get</i><code>} and
 *   {@link #put($type$) </code><i>put</i><code>} methods that read and write
 *   single $fulltype$s; </p></li>
 *
 *   <li><p> Relative {@link #get($type$[]) </code><i>bulk get</i><code>}
 *   methods that transfer contiguous sequences of $fulltype$s from this buffer
 *   into an array; {#if[!byte]?and}</p></li>
 *
 *   <li><p> Relative {@link #put($type$[]) </code><i>bulk put</i><code>}
 *   methods that transfer contiguous sequences of $fulltype$s from $a$
 *   $fulltype$ array{#if[char]?,&#32;a&#32;string,} or some other $fulltype$
 *   buffer into this buffer;{#if[!byte]?&#32;and} </p></li>
 *
#if[byte]
 *
 *   <li><p> Absolute and relative {@link #getChar() </code><i>get</i><code>}
 *   and {@link #putChar(char) </code><i>put</i><code>} methods that read and
 *   write values of other primitive types, translating them to and from
 *   sequences of bytes in a particular byte order; </p></li>
 *
 *   <li><p> Methods for creating <i><a href="#views">view buffers</a></i>,
 *   which allow a byte buffer to be viewed as a buffer containing values of
 *   some other primitive type; and </p></li>
 *
#end[byte]
 *
 *   <li><p> Methods for {@link #compact </code>compacting<code>}, {@link
 *   #duplicate </code>duplicating<code>}, and {@link #slice
 *   </code>slicing<code>} $a$ $fulltype$ buffer.  </p></li>
 *
 * </ul>
 *
 * <p> $Fulltype$ buffers can be created either by {@link #allocate
 * </code><i>allocation</i><code>}, which allocates space for the buffer's
 *
#if[byte]
 *
 * content, or by {@link #wrap($type$[]) </code><i>wrapping</i><code>} an
 * existing $fulltype$ array {#if[char]?or&#32;string} into a buffer.
 *
#else[byte]
 *
 * content, by {@link #wrap($type$[]) </code><i>wrapping</i><code>} an existing
 * $fulltype$ array {#if[char]?or&#32;string} into a buffer, or by creating a
 * <a href="ByteBuffer.html#views"><i>view</i></a> of an existing byte buffer.
 *
#end[byte]
 *
#if[byte]
 *
 * <a name="direct">
 * <h4> Direct <i>vs.</i> non-direct buffers </h4>
 *
 * <p> A byte buffer is either <i>direct</i> or <i>non-direct</i>.  Given a
 * direct byte buffer, the Java virtual machine will make a best effort to
 * perform native I/O operations directly upon it.  That is, it will attempt to
 * avoid copying the buffer's content to (or from) an intermediate buffer
 * before (or after) each invocation of one of the underlying operating
 * system's native I/O operations.
 *
 * <p> A direct byte buffer may be created by invoking the {@link
 * #allocateDirect(int) allocateDirect} factory method of this class.  The
 * buffers returned by this method typically have somewhat higher allocation
 * and deallocation costs than non-direct buffers.  The contents of direct
 * buffers may reside outside of the normal garbage-collected heap, and so
 * their impact upon the memory footprint of an application might not be
 * obvious.  It is therefore recommended that direct buffers be allocated
 * primarily for large, long-lived buffers that are subject to the underlying
 * system's native I/O operations.  In general it is best to allocate direct
 * buffers only when they yield a measureable gain in program performance.
 *
 * <p> A direct byte buffer may also be created by {@link
 * java.nio.channels.FileChannel#map </code>mapping<code>} a region of a file
 * directly into memory.  An implementation of the Java platform may optionally
 * support the creation of direct byte buffers from native code via JNI.  If an
 * instance of one of these kinds of buffers refers to an inaccessible region
 * of memory then an attempt to access that region will not change the buffer's
 * content and will cause an unspecified exception to be thrown either at the
 * time of the access or at some later time.
 *
 * <p> Whether a byte buffer is direct or non-direct may be determined by
 * invoking its {@link #isDirect isDirect} method.  This method is provided so
 * that explicit buffer management can be done in performance-critical code.
 *
 *
 * <a name="bin">
 * <h4> Access to binary data </h4>
 *
 * <p> This class defines methods for reading and writing values of all other
 * primitive types, except <tt>boolean</tt>.  Primitive values are translated
 * to (or from) sequences of bytes according to the buffer's current byte
 * order, which may be retrieved and modified via the {@link #order order}
 * methods.  Specific byte orders are represented by instances of the {@link
 * ByteOrder} class.  The initial order of a byte buffer is always {@link
 * ByteOrder#BIG_ENDIAN BIG_ENDIAN}.
 *
 * <p> For access to heterogeneous binary data, that is, sequences of values of
 * different types, this class defines a family of absolute and relative
 * <i>get</i> and <i>put</i> methods for each type.  For 32-bit floating-point
 * values, for example, this class defines:
 *
 * <blockquote><pre>
 * float  {@link #getFloat()}
 * float  {@link #getFloat(int) getFloat(int index)}
 *  void  {@link #putFloat(float) putFloat(float f)}
 *  void  {@link #putFloat(int,float) putFloat(int index, float f)}</pre></blockquote>
 *
 * <p> Corresponding methods are defined for the types <tt>char</tt>,
 * <tt>short</tt>, <tt>int</tt>, <tt>long</tt>, and <tt>double</tt>.  The index
 * parameters of the absolute <i>get</i> and <i>put</i> methods are in terms of
 * bytes rather than of the type being read or written.
 *
 * <a name="views">
 *
 * <p> For access to homogeneous binary data, that is, sequences of values of
 * the same type, this class defines methods that can create <i>views</i> of a
 * given byte buffer.  A <i>view buffer</i> is simply another buffer whose
 * content is backed by the byte buffer.  Changes to the byte buffer's content
 * will be visible in the view buffer, and vice versa; the two buffers'
 * position, limit, and mark values are independent.  The {@link
 * #asFloatBuffer() asFloatBuffer} method, for example, creates an instance of
 * the {@link FloatBuffer} class that is backed by the byte buffer upon which
 * the method is invoked.  Corresponding view-creation methods are defined for
 * the types <tt>char</tt>, <tt>short</tt>, <tt>int</tt>, <tt>long</tt>, and
 * <tt>double</tt>.
 *
 * <p> View buffers have three important advantages over the families of
 * type-specific <i>get</i> and <i>put</i> methods described above:
 *
 * <ul>
 *
 *   <li><p> A view buffer is indexed not in terms of bytes but rather in terms
 *   of the type-specific size of its values;  </p></li>
 *
 *   <li><p> A view buffer provides relative bulk <i>get</i> and <i>put</i>
 *   methods that can transfer contiguous sequences of values between a buffer
 *   and an array or some other buffer of the same type; and  </p></li>
 *
 *   <li><p> A view buffer is potentially much more efficient because it will
 *   be direct if, and only if, its backing byte buffer is direct.  </p></li>
 *
 * </ul>
 *
 * <p> The byte order of a view buffer is fixed to be that of its byte buffer
 * at the time that the view is created.  </p>
 *
#end[byte]
*
#if[!byte]
 *
 * <p> Like a byte buffer, $a$ $fulltype$ buffer is either <a
 * href="ByteBuffer.html#direct"><i>direct</i> or <i>non-direct</i></a>.  A
 * $fulltype$ buffer created via the <tt>wrap</tt> methods of this class will
 * be non-direct.  $A$ $fulltype$ buffer created as a view of a byte buffer will
 * be direct if, and only if, the byte buffer itself is direct.  Whether or not
 * $a$ $fulltype$ buffer is direct may be determined by invoking the {@link
 * #isDirect isDirect} method.  </p>
 *
#end[!byte]
*
#if[char]
 *
 * <p> This class implements the {@link CharSequence} interface so that
 * character buffers may be used wherever character sequences are accepted, for
 * example in the regular-expression package <tt>{@link java.util.regex}</tt>.
 * </p>
 *
#end[char]
 *
#if[byte]
 * <h4> Invocation chaining </h4>
#end[byte]
 *
 * <p> Methods in this class that do not otherwise have a value to return are
 * specified to return the buffer upon which they are invoked.  This allows
 * method invocations to be chained.
 *
#if[byte]
 *
 * The sequence of statements
 *
 * <blockquote><pre>
 * bb.putInt(0xCAFEBABE);
 * bb.putShort(3);
 * bb.putShort(45);</pre></blockquote>
 *
 * can, for example, be replaced by the single statement
 *
 * <blockquote><pre>
 * bb.putInt(0xCAFEBABE).putShort(3).putShort(45);</pre></blockquote>
 *
#end[byte]
#if[char]
 *
 * The sequence of statements
 *
 * <blockquote><pre>
 * cb.put("text/");
 * cb.put(subtype);
 * cb.put("; charset=");
 * cb.put(enc);</pre></blockquote>
 *
 * can, for example, be replaced by the single statement
 *
 * <blockquote><pre>
 * cb.put("text/").put(subtype).put("; charset=").put(enc);</pre></blockquote>
 *
#end[char]
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 */

public abstract class $Type$Buffer
    extends Buffer
    implements Comparable<$Type$Buffer>{#if[char]?, Appendable, CharSequence, Readable}
{

    // These fields are declared here rather than in Heap-X-Buffer in order to
    // reduce the number of virtual method invocations needed to access these
    // values, which is especially costly when coding small buffers.
    //
    final $type$[] hb;                  // Non-null only for heap buffers
    final int offset;
    boolean isReadOnly;                 // Valid only for heap buffers

    // Creates a new buffer with the given mark, position, limit, capacity,
    // backing array, and array offset
    //
    $Type$Buffer(int mark, int pos, int lim, int cap,   // package-private
                 $type$[] hb, int offset)
    {
        super(mark, pos, lim, cap);
        this.hb = hb;
        this.offset = offset;
    }

    // Creates a new buffer with the given mark, position, limit, and capacity
    //
    $Type$Buffer(int mark, int pos, int lim, int cap) { // package-private
        this(mark, pos, lim, cap, null, 0);
    }

#if[byte]

    /**
     * Allocates a new direct $fulltype$ buffer.
     *
     * <p> The new buffer's position will be zero, its limit will be its
     * capacity, its mark will be undefined, and each of its elements will be
     * initialized to zero.  Whether or not it has a
     * {@link #hasArray </code>backing array<code>} is unspecified.
     *
     * @param  capacity
     *         The new buffer's capacity, in $fulltype$s
     *
     * @return  The new $fulltype$ buffer
     *
     * @throws  IllegalArgumentException
     *          If the <tt>capacity</tt> is a negative integer
     */
    public static $Type$Buffer allocateDirect(int capacity) {
        return new Direct$Type$Buffer(capacity);
    }

#end[byte]

    /**
     * Allocates a new $fulltype$ buffer.
     *
     * <p> The new buffer's position will be zero, its limit will be its
     * capacity, its mark will be undefined, and each of its elements will be
     * initialized to zero.  It will have a {@link #array
     * </code>backing array<code>}, and its {@link #arrayOffset </code>array
     * offset<code>} will be zero.
     *
     * @param  capacity
     *         The new buffer's capacity, in $fulltype$s
     *
     * @return  The new $fulltype$ buffer
     *
     * @throws  IllegalArgumentException
     *          If the <tt>capacity</tt> is a negative integer
     */
    public static $Type$Buffer allocate(int capacity) {
        if (capacity < 0)
            throw new IllegalArgumentException();
        return new Heap$Type$Buffer(capacity, capacity);
    }

    /**
     * Wraps $a$ $fulltype$ array into a buffer.
     *
     * <p> The new buffer will be backed by the given $fulltype$ array;
     * that is, modifications to the buffer will cause the array to be modified
     * and vice versa.  The new buffer's capacity will be
     * <tt>array.length</tt>, its position will be <tt>offset</tt>, its limit
     * will be <tt>offset + length</tt>, and its mark will be undefined.  Its
     * {@link #array </code>backing array<code>} will be the given array, and
     * its {@link #arrayOffset </code>array offset<code>} will be zero.  </p>
     *
     * @param  array
     *         The array that will back the new buffer
     *
     * @param  offset
     *         The offset of the subarray to be used; must be non-negative and
     *         no larger than <tt>array.length</tt>.  The new buffer's position
     *         will be set to this value.
     *
     * @param  length
     *         The length of the subarray to be used;
     *         must be non-negative and no larger than
     *         <tt>array.length - offset</tt>.
     *         The new buffer's limit will be set to <tt>offset + length</tt>.
     *
     * @return  The new $fulltype$ buffer
     *
     * @throws  IndexOutOfBoundsException
     *          If the preconditions on the <tt>offset</tt> and <tt>length</tt>
     *          parameters do not hold
     */
    public static $Type$Buffer wrap($type$[] array,
                                    int offset, int length)
    {
        try {
            return new Heap$Type$Buffer(array, offset, length);
        } catch (IllegalArgumentException x) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Wraps $a$ $fulltype$ array into a buffer.
     *
     * <p> The new buffer will be backed by the given $fulltype$ array;
     * that is, modifications to the buffer will cause the array to be modified
     * and vice versa.  The new buffer's capacity and limit will be
     * <tt>array.length</tt>, its position will be zero, and its mark will be
     * undefined.  Its {@link #array </code>backing array<code>} will be the
     * given array, and its {@link #arrayOffset </code>array offset<code>} will
     * be zero.  </p>
     *
     * @param  array
     *         The array that will back this buffer
     *
     * @return  The new $fulltype$ buffer
     */
    public static $Type$Buffer wrap($type$[] array) {
        return wrap(array, 0, array.length);
    }

#if[char]

    /**
     * Attempts to read characters into the specified character buffer.
     * The buffer is used as a repository of characters as-is: the only
     * changes made are the results of a put operation. No flipping or
     * rewinding of the buffer is performed.
     *
     * @param target the buffer to read characters into
     * @return The number of characters added to the buffer, or
     *         -1 if this source of characters is at its end
     * @throws IOException if an I/O error occurs
     * @throws NullPointerException if target is null
     * @throws ReadOnlyBufferException if target is a read only buffer
     * @since 1.5
     */
    public int read(CharBuffer target) throws IOException {
        // Determine the number of bytes n that can be transferred
        int targetRemaining = target.remaining();
        int remaining = remaining();
        if (remaining == 0)
            return -1;
        int n = Math.min(remaining, targetRemaining);
        int limit = limit();
        // Set source limit to prevent target overflow
        if (targetRemaining < remaining)
            limit(position() + n);
        try {
            if (n > 0)
                target.put(this);
        } finally {
            limit(limit); // restore real limit
        }
        return n;
    }

    /**
     * Wraps a character sequence into a buffer.
     *
     * <p> The content of the new, read-only buffer will be the content of the
     * given character sequence.  The buffer's capacity will be
     * <tt>csq.length()</tt>, its position will be <tt>start</tt>, its limit
     * will be <tt>end</tt>, and its mark will be undefined.  </p>
     *
     * @param  csq
     *         The character sequence from which the new character buffer is to
     *         be created
     *
     * @param  start
     *         The index of the first character to be used;
     *         must be non-negative and no larger than <tt>csq.length()</tt>.
     *         The new buffer's position will be set to this value.
     *
     * @param  end
     *         The index of the character following the last character to be
     *         used; must be no smaller than <tt>start</tt> and no larger
     *         than <tt>csq.length()</tt>.
     *         The new buffer's limit will be set to this value.
     *
     * @return  The new character buffer
     *
     * @throws  IndexOutOfBoundsException
     *          If the preconditions on the <tt>start</tt> and <tt>end</tt>
     *          parameters do not hold
     */
    public static CharBuffer wrap(CharSequence csq, int start, int end) {
        try {
            return new StringCharBuffer(csq, start, end);
        } catch (IllegalArgumentException x) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Wraps a character sequence into a buffer.
     *
     * <p> The content of the new, read-only buffer will be the content of the
     * given character sequence.  The new buffer's capacity and limit will be
     * <tt>csq.length()</tt>, its position will be zero, and its mark will be
     * undefined.  </p>
     *
     * @param  csq
     *         The character sequence from which the new character buffer is to
     *         be created
     *
     * @return  The new character buffer
     */
    public static CharBuffer wrap(CharSequence csq) {
        return wrap(csq, 0, csq.length());
    }

#end[char]

    /**
     * Creates a new $fulltype$ buffer whose content is a shared subsequence of
     * this buffer's content.
     *
     * <p> The content of the new buffer will start at this buffer's current
     * position.  Changes to this buffer's content will be visible in the new
     * buffer, and vice versa; the two buffers' position, limit, and mark
     * values will be independent.
     *
     * <p> The new buffer's position will be zero, its capacity and its limit
     * will be the number of $fulltype$s remaining in this buffer, and its mark
     * will be undefined.  The new buffer will be direct if, and only if, this
     * buffer is direct, and it will be read-only if, and only if, this buffer
     * is read-only.  </p>
     *
     * @return  The new $fulltype$ buffer
     */
    public abstract $Type$Buffer slice();

    /**
     * Creates a new $fulltype$ buffer that shares this buffer's content.
     *
     * <p> The content of the new buffer will be that of this buffer.  Changes
     * to this buffer's content will be visible in the new buffer, and vice
     * versa; the two buffers' position, limit, and mark values will be
     * independent.
     *
     * <p> The new buffer's capacity, limit, position, and mark values will be
     * identical to those of this buffer.  The new buffer will be direct if,
     * and only if, this buffer is direct, and it will be read-only if, and
     * only if, this buffer is read-only.  </p>
     *
     * @return  The new $fulltype$ buffer
     */
    public abstract $Type$Buffer duplicate();

    /**
     * Creates a new, read-only $fulltype$ buffer that shares this buffer's
     * content.
     *
     * <p> The content of the new buffer will be that of this buffer.  Changes
     * to this buffer's content will be visible in the new buffer; the new
     * buffer itself, however, will be read-only and will not allow the shared
     * content to be modified.  The two buffers' position, limit, and mark
     * values will be independent.
     *
     * <p> The new buffer's capacity, limit, position, and mark values will be
     * identical to those of this buffer.
     *
     * <p> If this buffer is itself read-only then this method behaves in
     * exactly the same way as the {@link #duplicate duplicate} method.  </p>
     *
     * @return  The new, read-only $fulltype$ buffer
     */
    public abstract $Type$Buffer asReadOnlyBuffer();


    // -- Singleton get/put methods --

    /**
     * Relative <i>get</i> method.  Reads the $fulltype$ at this buffer's
     * current position, and then increments the position. </p>
     *
     * @return  The $fulltype$ at the buffer's current position
     *
     * @throws  BufferUnderflowException
     *          If the buffer's current position is not smaller than its limit
     */
    public abstract $type$ get();

    /**
     * Relative <i>put</i> method&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> Writes the given $fulltype$ into this buffer at the current
     * position, and then increments the position. </p>
     *
     * @param  $x$
     *         The $fulltype$ to be written
     *
     * @return  This buffer
     *
     * @throws  BufferOverflowException
     *          If this buffer's current position is not smaller than its limit
     *
     * @throws  ReadOnlyBufferException
     *          If this buffer is read-only
     */
    public abstract $Type$Buffer put($type$ $x$);

    /**
     * Absolute <i>get</i> method.  Reads the $fulltype$ at the given
     * index. </p>
     *
     * @param  index
     *         The index from which the $fulltype$ will be read
     *
     * @return  The $fulltype$ at the given index
     *
     * @throws  IndexOutOfBoundsException
     *          If <tt>index</tt> is negative
     *          or not smaller than the buffer's limit
     */
    public abstract $type$ get(int index);

    /**
     * Absolute <i>put</i> method&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> Writes the given $fulltype$ into this buffer at the given
     * index. </p>
     *
     * @param  index
     *         The index at which the $fulltype$ will be written
     *
     * @param  $x$
     *         The $fulltype$ value to be written
     *
     * @return  This buffer
     *
     * @throws  IndexOutOfBoundsException
     *          If <tt>index</tt> is negative
     *          or not smaller than the buffer's limit
     *
     * @throws  ReadOnlyBufferException
     *          If this buffer is read-only
     */
    public abstract $Type$Buffer put(int index, $type$ $x$);


    // -- Bulk get operations --

    /**
     * Relative bulk <i>get</i> method.
     *
     * <p> This method transfers $fulltype$s from this buffer into the given
     * destination array.  If there are fewer $fulltype$s remaining in the
     * buffer than are required to satisfy the request, that is, if
     * <tt>length</tt>&nbsp;<tt>&gt;</tt>&nbsp;<tt>remaining()</tt>, then no
     * $fulltype$s are transferred and a {@link BufferUnderflowException} is
     * thrown.
     *
     * <p> Otherwise, this method copies <tt>length</tt> $fulltype$s from this
     * buffer into the given array, starting at the current position of this
     * buffer and at the given offset in the array.  The position of this
     * buffer is then incremented by <tt>length</tt>.
     *
     * <p> In other words, an invocation of this method of the form
     * <tt>src.get(dst,&nbsp;off,&nbsp;len)</tt> has exactly the same effect as
     * the loop
     *
     * <pre>
     *     for (int i = off; i < off + len; i++)
     *         dst[i] = src.get(); </pre>
     *
     * except that it first checks that there are sufficient $fulltype$s in
     * this buffer and it is potentially much more efficient. </p>
     *
     * @param  dst
     *         The array into which $fulltype$s are to be written
     *
     * @param  offset
     *         The offset within the array of the first $fulltype$ to be
     *         written; must be non-negative and no larger than
     *         <tt>dst.length</tt>
     *
     * @param  length
     *         The maximum number of $fulltype$s to be written to the given
     *         array; must be non-negative and no larger than
     *         <tt>dst.length - offset</tt>
     *
     * @return  This buffer
     *
     * @throws  BufferUnderflowException
     *          If there are fewer than <tt>length</tt> $fulltype$s
     *          remaining in this buffer
     *
     * @throws  IndexOutOfBoundsException
     *          If the preconditions on the <tt>offset</tt> and <tt>length</tt>
     *          parameters do not hold
     */
    public $Type$Buffer get($type$[] dst, int offset, int length) {
        checkBounds(offset, length, dst.length);
        if (length > remaining())
            throw new BufferUnderflowException();
        int end = offset + length;
        for (int i = offset; i < end; i++)
            dst[i] = get();
        return this;
    }

    /**
     * Relative bulk <i>get</i> method.
     *
     * <p> This method transfers $fulltype$s from this buffer into the given
     * destination array.  An invocation of this method of the form
     * <tt>src.get(a)</tt> behaves in exactly the same way as the invocation
     *
     * <pre>
     *     src.get(a, 0, a.length) </pre>
     *
     * @return  This buffer
     *
     * @throws  BufferUnderflowException
     *          If there are fewer than <tt>length</tt> $fulltype$s
     *          remaining in this buffer
     */
    public $Type$Buffer get($type$[] dst) {
        return get(dst, 0, dst.length);
    }


    // -- Bulk put operations --

    /**
     * Relative bulk <i>put</i> method&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> This method transfers the $fulltype$s remaining in the given source
     * buffer into this buffer.  If there are more $fulltype$s remaining in the
     * source buffer than in this buffer, that is, if
     * <tt>src.remaining()</tt>&nbsp;<tt>&gt;</tt>&nbsp;<tt>remaining()</tt>,
     * then no $fulltype$s are transferred and a {@link
     * BufferOverflowException} is thrown.
     *
     * <p> Otherwise, this method copies
     * <i>n</i>&nbsp;=&nbsp;<tt>src.remaining()</tt> $fulltype$s from the given
     * buffer into this buffer, starting at each buffer's current position.
     * The positions of both buffers are then incremented by <i>n</i>.
     *
     * <p> In other words, an invocation of this method of the form
     * <tt>dst.put(src)</tt> has exactly the same effect as the loop
     *
     * <pre>
     *     while (src.hasRemaining())
     *         dst.put(src.get()); </pre>
     *
     * except that it first checks that there is sufficient space in this
     * buffer and it is potentially much more efficient. </p>
     *
     * @param  src
     *         The source buffer from which $fulltype$s are to be read;
     *         must not be this buffer
     *
     * @return  This buffer
     *
     * @throws  BufferOverflowException
     *          If there is insufficient space in this buffer
     *          for the remaining $fulltype$s in the source buffer
     *
     * @throws  IllegalArgumentException
     *          If the source buffer is this buffer
     *
     * @throws  ReadOnlyBufferException
     *          If this buffer is read-only
     */
    public $Type$Buffer put($Type$Buffer src) {
        if (src == this)
            throw new IllegalArgumentException();
        int n = src.remaining();
        if (n > remaining())
            throw new BufferOverflowException();
        for (int i = 0; i < n; i++)
            put(src.get());
        return this;
    }

    /**
     * Relative bulk <i>put</i> method&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> This method transfers $fulltype$s into this buffer from the given
     * source array.  If there are more $fulltype$s to be copied from the array
     * than remain in this buffer, that is, if
     * <tt>length</tt>&nbsp;<tt>&gt;</tt>&nbsp;<tt>remaining()</tt>, then no
     * $fulltype$s are transferred and a {@link BufferOverflowException} is
     * thrown.
     *
     * <p> Otherwise, this method copies <tt>length</tt> $fulltype$s from the
     * given array into this buffer, starting at the given offset in the array
     * and at the current position of this buffer.  The position of this buffer
     * is then incremented by <tt>length</tt>.
     *
     * <p> In other words, an invocation of this method of the form
     * <tt>dst.put(src,&nbsp;off,&nbsp;len)</tt> has exactly the same effect as
     * the loop
     *
     * <pre>
     *     for (int i = off; i < off + len; i++)
     *         dst.put(a[i]); </pre>
     *
     * except that it first checks that there is sufficient space in this
     * buffer and it is potentially much more efficient. </p>
     *
     * @param  src
     *         The array from which $fulltype$s are to be read
     *
     * @param  offset
     *         The offset within the array of the first $fulltype$ to be read;
     *         must be non-negative and no larger than <tt>array.length</tt>
     *
     * @param  length
     *         The number of $fulltype$s to be read from the given array;
     *         must be non-negative and no larger than
     *         <tt>array.length - offset</tt>
     *
     * @return  This buffer
     *
     * @throws  BufferOverflowException
     *          If there is insufficient space in this buffer
     *
     * @throws  IndexOutOfBoundsException
     *          If the preconditions on the <tt>offset</tt> and <tt>length</tt>
     *          parameters do not hold
     *
     * @throws  ReadOnlyBufferException
     *          If this buffer is read-only
     */
    public $Type$Buffer put($type$[] src, int offset, int length) {
        checkBounds(offset, length, src.length);
        if (length > remaining())
            throw new BufferOverflowException();
        int end = offset + length;
        for (int i = offset; i < end; i++)
            this.put(src[i]);
        return this;
    }

    /**
     * Relative bulk <i>put</i> method&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> This method transfers the entire content of the given source
     * $fulltype$ array into this buffer.  An invocation of this method of the
     * form <tt>dst.put(a)</tt> behaves in exactly the same way as the
     * invocation
     *
     * <pre>
     *     dst.put(a, 0, a.length) </pre>
     *
     * @return  This buffer
     *
     * @throws  BufferOverflowException
     *          If there is insufficient space in this buffer
     *
     * @throws  ReadOnlyBufferException
     *          If this buffer is read-only
     */
    public final $Type$Buffer put($type$[] src) {
        return put(src, 0, src.length);
    }

#if[char]

    /**
     * Relative bulk <i>put</i> method&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> This method transfers $fulltype$s from the given string into this
     * buffer.  If there are more $fulltype$s to be copied from the string than
     * remain in this buffer, that is, if
     * <tt>end&nbsp;-&nbsp;start</tt>&nbsp;<tt>&gt;</tt>&nbsp;<tt>remaining()</tt>,
     * then no $fulltype$s are transferred and a {@link
     * BufferOverflowException} is thrown.
     *
     * <p> Otherwise, this method copies
     * <i>n</i>&nbsp;=&nbsp;<tt>end</tt>&nbsp;-&nbsp;<tt>start</tt> $fulltype$s
     * from the given string into this buffer, starting at the given
     * <tt>start</tt> index and at the current position of this buffer.  The
     * position of this buffer is then incremented by <i>n</i>.
     *
     * <p> In other words, an invocation of this method of the form
     * <tt>dst.put(src,&nbsp;start,&nbsp;end)</tt> has exactly the same effect
     * as the loop
     *
     * <pre>
     *     for (int i = start; i < end; i++)
     *         dst.put(src.charAt(i)); </pre>
     *
     * except that it first checks that there is sufficient space in this
     * buffer and it is potentially much more efficient. </p>
     *
     * @param  src
     *         The string from which $fulltype$s are to be read
     *
     * @param  start
     *         The offset within the string of the first $fulltype$ to be read;
     *         must be non-negative and no larger than
     *         <tt>string.length()</tt>
     *
     * @param  end
     *         The offset within the string of the last $fulltype$ to be read,
     *         plus one; must be non-negative and no larger than
     *         <tt>string.length()</tt>
     *
     * @return  This buffer
     *
     * @throws  BufferOverflowException
     *          If there is insufficient space in this buffer
     *
     * @throws  IndexOutOfBoundsException
     *          If the preconditions on the <tt>start</tt> and <tt>end</tt>
     *          parameters do not hold
     *
     * @throws  ReadOnlyBufferException
     *          If this buffer is read-only
     */
    public $Type$Buffer put(String src, int start, int end) {
        checkBounds(start, end - start, src.length());
        for (int i = start; i < end; i++)
            this.put(src.charAt(i));
        return this;
    }

    /**
     * Relative bulk <i>put</i> method&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> This method transfers the entire content of the given source string
     * into this buffer.  An invocation of this method of the form
     * <tt>dst.put(s)</tt> behaves in exactly the same way as the invocation
     *
     * <pre>
     *     dst.put(s, 0, s.length()) </pre>
     *
     * @return  This buffer
     *
     * @throws  BufferOverflowException
     *          If there is insufficient space in this buffer
     *
     * @throws  ReadOnlyBufferException
     *          If this buffer is read-only
     */
    public final $Type$Buffer put(String src) {
        return put(src, 0, src.length());
    }

#end[char]


    // -- Other stuff --

    /**
     * Tells whether or not this buffer is backed by an accessible $fulltype$
     * array.
     *
     * <p> If this method returns <tt>true</tt> then the {@link #array() array}
     * and {@link #arrayOffset() arrayOffset} methods may safely be invoked.
     * </p>
     *
     * @return  <tt>true</tt> if, and only if, this buffer
     *          is backed by an array and is not read-only
     */
    public final boolean hasArray() {
        return (hb != null) && !isReadOnly;
    }

    /**
     * Returns the $fulltype$ array that backs this
     * buffer&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> Modifications to this buffer's content will cause the returned
     * array's content to be modified, and vice versa.
     *
     * <p> Invoke the {@link #hasArray hasArray} method before invoking this
     * method in order to ensure that this buffer has an accessible backing
     * array.  </p>
     *
     * @return  The array that backs this buffer
     *
     * @throws  ReadOnlyBufferException
     *          If this buffer is backed by an array but is read-only
     *
     * @throws  UnsupportedOperationException
     *          If this buffer is not backed by an accessible array
     */
    public final $type$[] array() {
        if (hb == null)
            throw new UnsupportedOperationException();
        if (isReadOnly)
            throw new ReadOnlyBufferException();
        return hb;
    }

    /**
     * Returns the offset within this buffer's backing array of the first
     * element of the buffer&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> If this buffer is backed by an array then buffer position <i>p</i>
     * corresponds to array index <i>p</i>&nbsp;+&nbsp;<tt>arrayOffset()</tt>.
     *
     * <p> Invoke the {@link #hasArray hasArray} method before invoking this
     * method in order to ensure that this buffer has an accessible backing
     * array.  </p>
     *
     * @return  The offset within this buffer's array
     *          of the first element of the buffer
     *
     * @throws  ReadOnlyBufferException
     *          If this buffer is backed by an array but is read-only
     *
     * @throws  UnsupportedOperationException
     *          If this buffer is not backed by an accessible array
     */
    public final int arrayOffset() {
        if (hb == null)
            throw new UnsupportedOperationException();
        if (isReadOnly)
            throw new ReadOnlyBufferException();
        return offset;
    }

    /**
     * Compacts this buffer&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> The $fulltype$s between the buffer's current position and its limit,
     * if any, are copied to the beginning of the buffer.  That is, the
     * $fulltype$ at index <i>p</i>&nbsp;=&nbsp;<tt>position()</tt> is copied
     * to index zero, the $fulltype$ at index <i>p</i>&nbsp;+&nbsp;1 is copied
     * to index one, and so forth until the $fulltype$ at index
     * <tt>limit()</tt>&nbsp;-&nbsp;1 is copied to index
     * <i>n</i>&nbsp;=&nbsp;<tt>limit()</tt>&nbsp;-&nbsp;<tt>1</tt>&nbsp;-&nbsp;<i>p</i>.
     * The buffer's position is then set to <i>n+1</i> and its limit is set to
     * its capacity.  The mark, if defined, is discarded.
     *
     * <p> The buffer's position is set to the number of $fulltype$s copied,
     * rather than to zero, so that an invocation of this method can be
     * followed immediately by an invocation of another relative <i>put</i>
     * method. </p>
     *
#if[byte]
     *
     * <p> Invoke this method after writing data from a buffer in case the
     * write was incomplete.  The following loop, for example, copies bytes
     * from one channel to another via the buffer <tt>buf</tt>:
     *
     * <blockquote><pre>
     * buf.clear();          // Prepare buffer for use
     * while (in.read(buf) >= 0 || buf.position != 0) {
     *     buf.flip();
     *     out.write(buf);
     *     buf.compact();    // In case of partial write
     * }</pre></blockquote>
     *
#end[byte]
     *
     * @return  This buffer
     *
     * @throws  ReadOnlyBufferException
     *          If this buffer is read-only
     */
    public abstract $Type$Buffer compact();

    /**
     * Tells whether or not this $fulltype$ buffer is direct. </p>
     *
     * @return  <tt>true</tt> if, and only if, this buffer is direct
     */
    public abstract boolean isDirect();

#if[!char]

    /**
     * Returns a string summarizing the state of this buffer.  </p>
     *
     * @return  A summary string
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName());
        sb.append("[pos=");
        sb.append(position());
        sb.append(" lim=");
        sb.append(limit());
        sb.append(" cap=");
        sb.append(capacity());
        sb.append("]");
        return sb.toString();
    }

#end[!char]


    // ## Should really use unchecked accessors here for speed

    /**
     * Returns the current hash code of this buffer.
     *
     * <p> The hash code of a $type$ buffer depends only upon its remaining
     * elements; that is, upon the elements from <tt>position()</tt> up to, and
     * including, the element at <tt>limit()</tt>&nbsp;-&nbsp;<tt>1</tt>.
     *
     * <p> Because buffer hash codes are content-dependent, it is inadvisable
     * to use buffers as keys in hash maps or similar data structures unless it
     * is known that their contents will not change.  </p>
     *
     * @return  The current hash code of this buffer
     */
    public int hashCode() {
        int h = 1;
        int p = position();
        for (int i = limit() - 1; i >= p; i--)
            h = 31 * h + (int)get(i);
        return h;
    }

    /**
     * Tells whether or not this buffer is equal to another object.
     *
     * <p> Two $type$ buffers are equal if, and only if,
     *
     * <p><ol>
     *
     *   <li><p> They have the same element type,  </p></li>
     *
     *   <li><p> They have the same number of remaining elements, and
     *   </p></li>
     *
     *   <li><p> The two sequences of remaining elements, considered
     *   independently of their starting positions, are pointwise equal.
     *   </p></li>
     *
     * </ol>
     *
     * <p> A $type$ buffer is not equal to any other type of object.  </p>
     *
     * @param  ob  The object to which this buffer is to be compared
     *
     * @return  <tt>true</tt> if, and only if, this buffer is equal to the
     *           given object
     */
    public boolean equals(Object ob) {
        if (this == ob)
            return true;
        if (!(ob instanceof $Type$Buffer))
            return false;
        $Type$Buffer that = ($Type$Buffer)ob;
        if (this.remaining() != that.remaining())
            return false;
        int p = this.position();
        for (int i = this.limit() - 1, j = that.limit() - 1; i >= p; i--, j--) {
            $type$ v1 = this.get(i);
            $type$ v2 = that.get(j);
            if (v1 != v2) {
                if ((v1 != v1) && (v2 != v2))   // For float and double
                    continue;
                return false;
            }
        }
        return true;
    }

    /**
     * Compares this buffer to another.
     *
     * <p> Two $type$ buffers are compared by comparing their sequences of
     * remaining elements lexicographically, without regard to the starting
     * position of each sequence within its corresponding buffer.
     *
     * <p> A $type$ buffer is not comparable to any other type of object.
     *
     * @return  A negative integer, zero, or a positive integer as this buffer
     *          is less than, equal to, or greater than the given buffer
     */
    public int compareTo($Type$Buffer that) {
        int n = this.position() + Math.min(this.remaining(), that.remaining());
        for (int i = this.position(), j = that.position(); i < n; i++, j++) {
            $type$ v1 = this.get(i);
            $type$ v2 = that.get(j);
            if (v1 == v2)
                continue;
            if ((v1 != v1) && (v2 != v2))       // For float and double
                continue;
            if (v1 < v2)
                return -1;
            return +1;
        }
        return this.remaining() - that.remaining();
    }



    // -- Other char stuff --

#if[char]

    /**
     * Returns a string containing the characters in this buffer.
     *
     * <p> The first character of the resulting string will be the character at
     * this buffer's position, while the last character will be the character
     * at index <tt>limit()</tt>&nbsp;-&nbsp;1.  Invoking this method does not
     * change the buffer's position. </p>
     *
     * @return  The specified string
     */
    public String toString() {
        return toString(position(), limit());
    }

    abstract String toString(int start, int end);       // package-private


    // --- Methods to support CharSequence ---

    /**
     * Returns the length of this character buffer.
     *
     * <p> When viewed as a character sequence, the length of a character
     * buffer is simply the number of characters between the position
     * (inclusive) and the limit (exclusive); that is, it is equivalent to
     * <tt>remaining()</tt>. </p>
     *
     * @return  The length of this character buffer
     */
    public final int length() {
        return remaining();
    }

    /**
     * Reads the character at the given index relative to the current
     * position. </p>
     *
     * @param  index
     *         The index of the character to be read, relative to the position;
     *         must be non-negative and smaller than <tt>remaining()</tt>
     *
     * @return  The character at index
     *          <tt>position()&nbsp;+&nbsp;index</tt>
     *
     * @throws  IndexOutOfBoundsException
     *          If the preconditions on <tt>index</tt> do not hold
     */
    public final char charAt(int index) {
        return get(position() + checkIndex(index, 1));
    }

    /**
     * Creates a new character buffer that represents the specified subsequence
     * of this buffer, relative to the current position.
     *
     * <p> The new buffer will share this buffer's content; that is, if the
     * content of this buffer is mutable then modifications to one buffer will
     * cause the other to be modified.  The new buffer's capacity will be that
     * of this buffer, its position will be
     * <tt>position()</tt>&nbsp;+&nbsp;<tt>start</tt>, and its limit will be
     * <tt>position()</tt>&nbsp;+&nbsp;<tt>end</tt>.  The new buffer will be
     * direct if, and only if, this buffer is direct, and it will be read-only
     * if, and only if, this buffer is read-only.  </p>
     *
     * @param  start
     *         The index, relative to the current position, of the first
     *         character in the subsequence; must be non-negative and no larger
     *         than <tt>remaining()</tt>
     *
     * @param  end
     *         The index, relative to the current position, of the character
     *         following the last character in the subsequence; must be no
     *         smaller than <tt>start</tt> and no larger than
     *         <tt>remaining()</tt>
     *
     * @return  The new character sequence
     *
     * @throws  IndexOutOfBoundsException
     *          If the preconditions on <tt>start</tt> and <tt>end</tt>
     *          do not hold
     */
    public abstract CharSequence subSequence(int start, int end);


    // --- Methods to support Appendable ---

    /**
     * Appends the specified character sequence  to this
     * buffer&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> An invocation of this method of the form <tt>dst.append(csq)</tt>
     * behaves in exactly the same way as the invocation
     *
     * <pre>
     *     dst.put(csq.toString()) </pre>
     *
     * <p> Depending on the specification of <tt>toString</tt> for the
     * character sequence <tt>csq</tt>, the entire sequence may not be
     * appended.  For instance, invoking the {@link $Type$Buffer#toString()
     * toString} method of a character buffer will return a subsequence whose
     * content depends upon the buffer's position and limit.
     *
     * @param  csq
     *         The character sequence to append.  If <tt>csq</tt> is
     *         <tt>null</tt>, then the four characters <tt>"null"</tt> are
     *         appended to this character buffer.
     *
     * @return  This buffer
     *
     * @throws  BufferOverflowException
     *          If there is insufficient space in this buffer
     *
     * @throws  ReadOnlyBufferException
     *          If this buffer is read-only
     *
     * @since  1.5
     */
    public $Type$Buffer append(CharSequence csq) {
        if (csq == null)
            return put("null");
        else
            return put(csq.toString());
    }

    /**
     * Appends a subsequence of the  specified character sequence  to this
     * buffer&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> An invocation of this method of the form <tt>dst.append(csq, start,
     * end)</tt> when <tt>csq</tt> is not <tt>null</tt>, behaves in exactly the
     * same way as the invocation
     *
     * <pre>
     *     dst.put(csq.subSequence(start, end).toString()) </pre>
     *
     * @param  csq
     *         The character sequence from which a subsequence will be
     *         appended.  If <tt>csq</tt> is <tt>null</tt>, then characters
     *         will be appended as if <tt>csq</tt> contained the four
     *         characters <tt>"null"</tt>.
     *
     * @return  This buffer
     *
     * @throws  BufferOverflowException
     *          If there is insufficient space in this buffer
     *
     * @throws  IndexOutOfBoundsException
     *          If <tt>start</tt> or <tt>end</tt> are negative, <tt>start</tt>
     *          is greater than <tt>end</tt>, or <tt>end</tt> is greater than
     *          <tt>csq.length()</tt>
     *
     * @throws  ReadOnlyBufferException
     *          If this buffer is read-only
     *
     * @since  1.5
     */
    public $Type$Buffer append(CharSequence csq, int start, int end) {
        CharSequence cs = (csq == null ? "null" : csq);
        return put(cs.subSequence(start, end).toString());
    }

    /**
     * Appends the specified $fulltype$  to this
     * buffer&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> An invocation of this method of the form <tt>dst.append($x$)</tt>
     * behaves in exactly the same way as the invocation
     *
     * <pre>
     *     dst.put($x$) </pre>
     *
     * @param  $x$
     *         The 16-bit $fulltype$ to append
     *
     * @return  This buffer
     *
     * @throws  BufferOverflowException
     *          If there is insufficient space in this buffer
     *
     * @throws  ReadOnlyBufferException
     *          If this buffer is read-only
     *
     * @since  1.5
     */
    public $Type$Buffer append($type$ $x$) {
        return put($x$);
    }

#end[char]


    // -- Other byte stuff: Access to binary data --

#if[!byte]

    /**
     * Retrieves this buffer's byte order.
     *
     * <p> The byte order of $a$ $fulltype$ buffer created by allocation or by
     * wrapping an existing <tt>$type$</tt> array is the {@link
     * ByteOrder#nativeOrder </code>native order<code>} of the underlying
     * hardware.  The byte order of $a$ $fulltype$ buffer created as a <a
     * href="ByteBuffer.html#views">view</a> of a byte buffer is that of the
     * byte buffer at the moment that the view is created.  </p>
     *
     * @return  This buffer's byte order
     */
    public abstract ByteOrder order();

#end[!byte]

#if[byte]

    boolean bigEndian                                   // package-private
        = true;
    boolean nativeByteOrder                             // package-private
        = (Bits.byteOrder() == ByteOrder.BIG_ENDIAN);

    /**
     * Retrieves this buffer's byte order.
     *
     * <p> The byte order is used when reading or writing multibyte values, and
     * when creating buffers that are views of this byte buffer.  The order of
     * a newly-created byte buffer is always {@link ByteOrder#BIG_ENDIAN
     * BIG_ENDIAN}.  </p>
     *
     * @return  This buffer's byte order
     */
    public final ByteOrder order() {
        return bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
    }

    /**
     * Modifies this buffer's byte order.  </p>
     *
     * @param  bo
     *         The new byte order,
     *         either {@link ByteOrder#BIG_ENDIAN BIG_ENDIAN}
     *         or {@link ByteOrder#LITTLE_ENDIAN LITTLE_ENDIAN}
     *
     * @return  This buffer
     */
    public final $Type$Buffer order(ByteOrder bo) {
        bigEndian = (bo == ByteOrder.BIG_ENDIAN);
        nativeByteOrder =
            (bigEndian == (Bits.byteOrder() == ByteOrder.BIG_ENDIAN));
        return this;
    }

    // Unchecked accessors, for use by ByteBufferAs-X-Buffer classes
    //
    abstract byte _get(int i);                          // package-private
    abstract void _put(int i, byte b);                  // package-private

    // #BIN
    //
    // Binary-data access methods  for short, char, int, long, float,
    // and double will be inserted here

#end[byte]

}
