/*
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
 *
 */

/*
 *
 * (C) Copyright IBM Corp. 1998-2004 - All Rights Reserved
 *
 */

#ifndef __STATETABLEPROCESSOR_H
#define __STATETABLEPROCESSOR_H

/**
 * \file
 * \internal
 */

#include "LETypes.h"
#include "MorphTables.h"
#include "MorphStateTables.h"
#include "SubtableProcessor.h"

U_NAMESPACE_BEGIN

class LEGlyphStorage;

class StateTableProcessor : public SubtableProcessor
{
public:
    void process(LEGlyphStorage &glyphStorage);

    virtual void beginStateTable() = 0;

    virtual ByteOffset processStateEntry(LEGlyphStorage &glyphStorage, le_int32 &currGlyph, EntryTableIndex index) = 0;

    virtual void endStateTable() = 0;

protected:
    StateTableProcessor(const MorphSubtableHeader *morphSubtableHeader);
    virtual ~StateTableProcessor();

    StateTableProcessor();

    le_int16 stateSize;
    ByteOffset classTableOffset;
    ByteOffset stateArrayOffset;
    ByteOffset entryTableOffset;

    const ClassTable *classTable;
    TTGlyphID firstGlyph;
    TTGlyphID lastGlyph;

    const MorphStateTableHeader *stateTableHeader;

private:
    StateTableProcessor(const StateTableProcessor &other); // forbid copying of this class
    StateTableProcessor &operator=(const StateTableProcessor &other); // forbid copying of this class
};

U_NAMESPACE_END
#endif
