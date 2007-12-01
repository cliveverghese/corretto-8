/*
 * Copyright 1997-2005 Sun Microsystems, Inc.  All Rights Reserved.
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

/*
 * Native method support for java.util.zip.Inflater
 */

#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include "jlong.h"
#include "jni.h"
#include "jvm.h"
#include "jni_util.h"
#include "zlib.h"
#include "java_util_zip_Inflater.h"

#define ThrowDataFormatException(env, msg) \
        JNU_ThrowByName(env, "java/util/zip/DataFormatException", msg)

static jfieldID strmID;
static jfieldID needDictID;
static jfieldID finishedID;
static jfieldID bufID, offID, lenID;

JNIEXPORT void JNICALL
Java_java_util_zip_Inflater_initIDs(JNIEnv *env, jclass cls)
{
    strmID = (*env)->GetFieldID(env, cls, "strm", "J");
    needDictID = (*env)->GetFieldID(env, cls, "needDict", "Z");
    finishedID = (*env)->GetFieldID(env, cls, "finished", "Z");
    bufID = (*env)->GetFieldID(env, cls, "buf", "[B");
    offID = (*env)->GetFieldID(env, cls, "off", "I");
    lenID = (*env)->GetFieldID(env, cls, "len", "I");
}

JNIEXPORT jlong JNICALL
Java_java_util_zip_Inflater_init(JNIEnv *env, jclass cls, jboolean nowrap)
{
    z_stream *strm = calloc(1, sizeof(z_stream));

    if (strm == 0) {
        JNU_ThrowOutOfMemoryError(env, 0);
        return jlong_zero;
    } else {
        char *msg;
        switch (inflateInit2(strm, nowrap ? -MAX_WBITS : MAX_WBITS)) {
          case Z_OK:
            return ptr_to_jlong(strm);
          case Z_MEM_ERROR:
            free(strm);
            JNU_ThrowOutOfMemoryError(env, 0);
            return jlong_zero;
          default:
            msg = strm->msg;
            free(strm);
            JNU_ThrowInternalError(env, msg);
            return jlong_zero;
        }
    }
}

JNIEXPORT void JNICALL
Java_java_util_zip_Inflater_setDictionary(JNIEnv *env, jclass cls, jlong strm,
                                          jarray b, jint off, jint len)
{
    Bytef *buf = (*env)->GetPrimitiveArrayCritical(env, b, 0);
    int res;
    if (buf == 0) /* out of memory */
        return;
    res = inflateSetDictionary(jlong_to_ptr(strm), buf + off, len);
    (*env)->ReleasePrimitiveArrayCritical(env, b, buf, 0);
    switch (res) {
    case Z_OK:
        break;
    case Z_STREAM_ERROR:
    case Z_DATA_ERROR:
        JNU_ThrowIllegalArgumentException(env, ((z_stream *)jlong_to_ptr(strm))->msg);
        break;
    default:
        JNU_ThrowInternalError(env, ((z_stream *)jlong_to_ptr(strm))->msg);
        break;
    }
}

JNIEXPORT jint JNICALL
Java_java_util_zip_Inflater_inflateBytes(JNIEnv *env, jobject this,
                                         jarray b, jint off, jint len)
{
    z_stream *strm = jlong_to_ptr((*env)->GetLongField(env, this, strmID));

    if (strm == 0) {
        JNU_ThrowNullPointerException(env, 0);
        return 0;
    } else {
        jarray this_buf = (jarray)(*env)->GetObjectField(env, this, bufID);
        jint this_off = (*env)->GetIntField(env, this, offID);
        jint this_len = (*env)->GetIntField(env, this, lenID);
        jbyte *in_buf;
        jbyte *out_buf;
        int ret;

        in_buf = (jbyte *) malloc(this_len);
        if (in_buf == 0) {
            return 0;
        }
        (*env)->GetByteArrayRegion(env, this_buf, this_off, this_len, in_buf);

        out_buf = (jbyte *) malloc(len);
        if (out_buf == 0) {
            free(in_buf);
            return 0;
        }

        strm->next_in  = (Bytef *) in_buf;
        strm->next_out = (Bytef *) out_buf;
        strm->avail_in  = this_len;
        strm->avail_out = len;
        ret = inflate(strm, Z_PARTIAL_FLUSH);

        if (ret == Z_STREAM_END || ret == Z_OK) {
            (*env)->SetByteArrayRegion(env, b, off, len - strm->avail_out, out_buf);
        }
        free(out_buf);
        free(in_buf);

        switch (ret) {
        case Z_STREAM_END:
            (*env)->SetBooleanField(env, this, finishedID, JNI_TRUE);
            /* fall through */
        case Z_OK:
            this_off += this_len - strm->avail_in;
            (*env)->SetIntField(env, this, offID, this_off);
            (*env)->SetIntField(env, this, lenID, strm->avail_in);
            return len - strm->avail_out;
        case Z_NEED_DICT:
            (*env)->SetBooleanField(env, this, needDictID, JNI_TRUE);
            /* Might have consumed some input here! */
            this_off += this_len - strm->avail_in;
            (*env)->SetIntField(env, this, offID, this_off);
            (*env)->SetIntField(env, this, lenID, strm->avail_in);
            return 0;
        case Z_BUF_ERROR:
            return 0;
        case Z_DATA_ERROR:
            ThrowDataFormatException(env, strm->msg);
            return 0;
        case Z_MEM_ERROR:
            JNU_ThrowOutOfMemoryError(env, 0);
            return 0;
        default:
            JNU_ThrowInternalError(env, strm->msg);
            return 0;
        }
    }
}

JNIEXPORT jint JNICALL
Java_java_util_zip_Inflater_getAdler(JNIEnv *env, jclass cls, jlong strm)
{
    return ((z_stream *)jlong_to_ptr(strm))->adler;
}

JNIEXPORT jlong JNICALL
Java_java_util_zip_Inflater_getBytesRead(JNIEnv *env, jclass cls, jlong strm)
{
    return ((z_stream *)jlong_to_ptr(strm))->total_in;
}

JNIEXPORT jlong JNICALL
Java_java_util_zip_Inflater_getBytesWritten(JNIEnv *env, jclass cls, jlong strm)
{
    return ((z_stream *)jlong_to_ptr(strm))->total_out;
}

JNIEXPORT void JNICALL
Java_java_util_zip_Inflater_reset(JNIEnv *env, jclass cls, jlong strm)
{
    if (inflateReset(jlong_to_ptr(strm)) != Z_OK) {
        JNU_ThrowInternalError(env, 0);
    }
}

JNIEXPORT void JNICALL
Java_java_util_zip_Inflater_end(JNIEnv *env, jclass cls, jlong strm)
{
    if (inflateEnd(jlong_to_ptr(strm)) == Z_STREAM_ERROR) {
        JNU_ThrowInternalError(env, 0);
    } else {
        free(jlong_to_ptr(strm));
    }
}
