/*
 * Copyright (c) 1997, 2010, Oracle and/or its affiliates. All rights reserved.
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
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 *
 */

#include "precompiled.hpp"
#include "interpreter/bytecodeHistogram.hpp"
#include "interpreter/bytecodeTracer.hpp"
#include "interpreter/bytecodes.hpp"
#include "interpreter/interpreter.hpp"
#include "interpreter/interpreterRuntime.hpp"
#include "memory/resourceArea.hpp"
#include "oops/methodDataOop.hpp"
#include "oops/methodOop.hpp"
#include "runtime/mutexLocker.hpp"
#include "runtime/timer.hpp"


#ifndef PRODUCT

// Standard closure for BytecodeTracer: prints the current bytecode
// and its attributes using bytecode-specific information.

class BytecodePrinter: public BytecodeClosure {
 private:
  // %%% This field is not GC-ed, and so can contain garbage
  // between critical sections.  Use only pointer-comparison
  // operations on the pointer, except within a critical section.
  // (Also, ensure that occasional false positives are benign.)
  methodOop _current_method;
  bool      _is_wide;
  Bytecodes::Code _code;
  address   _next_pc;                // current decoding position

  void      align()                  { _next_pc = (address)round_to((intptr_t)_next_pc, sizeof(jint)); }
  int       get_byte()               { return *(jbyte*) _next_pc++; }  // signed
  short     get_short()              { short i=Bytes::get_Java_u2(_next_pc); _next_pc+=2; return i; }
  int       get_int()                { int i=Bytes::get_Java_u4(_next_pc); _next_pc+=4; return i; }

  int       get_index_u1()           { return *(address)_next_pc++; }
  int       get_index_u2()           { int i=Bytes::get_Java_u2(_next_pc); _next_pc+=2; return i; }
  int       get_index_u1_cpcache()   { return get_index_u1() + constantPoolOopDesc::CPCACHE_INDEX_TAG; }
  int       get_index_u2_cpcache()   { int i=Bytes::get_native_u2(_next_pc); _next_pc+=2; return i + constantPoolOopDesc::CPCACHE_INDEX_TAG; }
  int       get_index_u4()           { int i=Bytes::get_native_u4(_next_pc); _next_pc+=4; return i; }
  int       get_index_special()      { return (is_wide()) ? get_index_u2() : get_index_u1(); }
  methodOop method()                 { return _current_method; }
  bool      is_wide()                { return _is_wide; }
  Bytecodes::Code raw_code()         { return Bytecodes::Code(_code); }


  bool      check_index(int i, int& cp_index, outputStream* st = tty);
  void      print_constant(int i, outputStream* st = tty);
  void      print_field_or_method(int i, outputStream* st = tty);
  void      print_field_or_method(int orig_i, int i, outputStream* st = tty);
  void      print_attributes(int bci, outputStream* st = tty);
  void      bytecode_epilog(int bci, outputStream* st = tty);

 public:
  BytecodePrinter() {
    _is_wide = false;
    _code = Bytecodes::_illegal;
  }

  // This method is called while executing the raw bytecodes, so none of
  // the adjustments that BytecodeStream performs applies.
  void trace(methodHandle method, address bcp, uintptr_t tos, uintptr_t tos2, outputStream* st) {
    ResourceMark rm;
    if (_current_method != method()) {
      // Note 1: This code will not work as expected with true MT/MP.
      //         Need an explicit lock or a different solution.
      // It is possible for this block to be skipped, if a garbage
      // _current_method pointer happens to have the same bits as
      // the incoming method.  We could lose a line of trace output.
      // This is acceptable in a debug-only feature.
      st->cr();
      st->print("[%d] ", (int) Thread::current()->osthread()->thread_id());
      method->print_name(st);
      st->cr();
      _current_method = method();
    }
    Bytecodes::Code code;
    if (is_wide()) {
      // bcp wasn't advanced if previous bytecode was _wide.
      code = Bytecodes::code_at(bcp+1);
    } else {
      code = Bytecodes::code_at(bcp);
    }
    _code = code;
     int bci = bcp - method->code_base();
    st->print("[%d] ", (int) Thread::current()->osthread()->thread_id());
    if (Verbose) {
      st->print("%8d  %4d  " INTPTR_FORMAT " " INTPTR_FORMAT " %s",
           BytecodeCounter::counter_value(), bci, tos, tos2, Bytecodes::name(code));
    } else {
      st->print("%8d  %4d  %s",
           BytecodeCounter::counter_value(), bci, Bytecodes::name(code));
    }
    _next_pc = is_wide() ? bcp+2 : bcp+1;
    print_attributes(bci);
    // Set is_wide for the next one, since the caller of this doesn't skip
    // the next bytecode.
    _is_wide = (code == Bytecodes::_wide);
    _code = Bytecodes::_illegal;
  }

  // Used for methodOop::print_codes().  The input bcp comes from
  // BytecodeStream, which will skip wide bytecodes.
  void trace(methodHandle method, address bcp, outputStream* st) {
    _current_method = method();
    ResourceMark rm;
    Bytecodes::Code code = Bytecodes::code_at(bcp);
    // Set is_wide
    _is_wide = (code == Bytecodes::_wide);
    if (is_wide()) {
      code = Bytecodes::code_at(bcp+1);
    }
    _code = code;
    int bci = bcp - method->code_base();
    // Print bytecode index and name
    if (is_wide()) {
      st->print("%d %s_w", bci, Bytecodes::name(code));
    } else {
      st->print("%d %s", bci, Bytecodes::name(code));
    }
    _next_pc = is_wide() ? bcp+2 : bcp+1;
    print_attributes(bci, st);
    bytecode_epilog(bci, st);
  }
};


// Implementation of BytecodeTracer

// %%% This set_closure thing seems overly general, given that
// nobody uses it.  Also, if BytecodePrinter weren't hidden
// then methodOop could use instances of it directly and it
// would be easier to remove races on _current_method and bcp.
// Since this is not product functionality, we can defer cleanup.

BytecodeClosure* BytecodeTracer::_closure = NULL;

static BytecodePrinter std_closure;
BytecodeClosure* BytecodeTracer::std_closure() {
  return &::std_closure;
}


void BytecodeTracer::trace(methodHandle method, address bcp, uintptr_t tos, uintptr_t tos2, outputStream* st) {
  if (TraceBytecodes && BytecodeCounter::counter_value() >= TraceBytecodesAt) {
    ttyLocker ttyl;  // 5065316: keep the following output coherent
    // The ttyLocker also prevents races between two threads
    // trying to use the single instance of BytecodePrinter.
    // Using the ttyLocker prevents the system from coming to
    // a safepoint within this code, which is sensitive to methodOop
    // movement.
    //
    // There used to be a leaf mutex here, but the ttyLocker will
    // work just as well, as long as the printing operations never block.
    //
    // We put the locker on the static trace method, not the
    // virtual one, because the clients of this module go through
    // the static method.
    _closure->trace(method, bcp, tos, tos2, st);
  }
}

void BytecodeTracer::trace(methodHandle method, address bcp, outputStream* st) {
  ttyLocker ttyl;  // 5065316: keep the following output coherent
  _closure->trace(method, bcp, st);
}

void print_symbol(symbolOop sym, outputStream* st) {
  char buf[40];
  int len = sym->utf8_length();
  if (len >= (int)sizeof(buf)) {
    st->print_cr(" %s...[%d]", sym->as_C_string(buf, sizeof(buf)), len);
  } else {
    st->print(" ");
    sym->print_on(st); st->cr();
  }
}

void print_oop(oop value, outputStream* st) {
  if (value == NULL) {
    st->print_cr(" NULL");
  } else if (java_lang_String::is_instance(value)) {
    EXCEPTION_MARK;
    Handle h_value (THREAD, value);
    symbolHandle sym = java_lang_String::as_symbol(h_value, CATCH);
    print_symbol(sym(), st);
  } else if (value->is_symbol()) {
    print_symbol(symbolOop(value), st);
  } else {
    st->print_cr(" " PTR_FORMAT, (intptr_t) value);
  }
}

bool BytecodePrinter::check_index(int i, int& cp_index, outputStream* st) {
  constantPoolOop constants = method()->constants();
  int ilimit = constants->length(), climit = 0;
  Bytecodes::Code code = raw_code();

  constantPoolCacheOop cache = NULL;
  if (Bytecodes::uses_cp_cache(code)) {
    cache = constants->cache();
    if (cache != NULL) {
      //climit = cache->length();  // %%% private!
      size_t size = cache->size() * HeapWordSize;
      size -= sizeof(constantPoolCacheOopDesc);
      size /= sizeof(ConstantPoolCacheEntry);
      climit = (int) size;
    }
  }

  if (cache != NULL && constantPoolCacheOopDesc::is_secondary_index(i)) {
    i = constantPoolCacheOopDesc::decode_secondary_index(i);
    st->print(" secondary cache[%d] of", i);
    if (i >= 0 && i < climit) {
      if (!cache->entry_at(i)->is_secondary_entry()) {
        st->print_cr(" not secondary entry?", i);
        return false;
      }
      i = cache->entry_at(i)->main_entry_index();
      goto check_cache_index;
    } else {
      st->print_cr(" not in cache[*]?", i);
      return false;
    }
  }

  if (cache != NULL) {
    goto check_cache_index;
  }

 check_cp_index:
  if (i >= 0 && i < ilimit) {
    if (WizardMode)  st->print(" cp[%d]", i);
    cp_index = i;
    return true;
  }

  st->print_cr(" CP[%d] not in CP", i);
  return false;

 check_cache_index:
#ifdef ASSERT
  {
    const int CPCACHE_INDEX_TAG = constantPoolOopDesc::CPCACHE_INDEX_TAG;
    if (i >= CPCACHE_INDEX_TAG && i < climit + CPCACHE_INDEX_TAG) {
      i -= CPCACHE_INDEX_TAG;
    } else {
      st->print_cr(" CP[%d] missing bias?", i);
      return false;
    }
  }
#endif //ASSERT
  if (i >= 0 && i < climit) {
    if (cache->entry_at(i)->is_secondary_entry()) {
      st->print_cr(" secondary entry?");
      return false;
    }
    i = cache->entry_at(i)->constant_pool_index();
    goto check_cp_index;
  }
  st->print_cr(" not in CP[*]?", i);
  return false;
}

void BytecodePrinter::print_constant(int i, outputStream* st) {
  int orig_i = i;
  if (!check_index(orig_i, i, st))  return;

  constantPoolOop constants = method()->constants();
  constantTag tag = constants->tag_at(i);

  if (tag.is_int()) {
    st->print_cr(" " INT32_FORMAT, constants->int_at(i));
  } else if (tag.is_long()) {
    st->print_cr(" " INT64_FORMAT, constants->long_at(i));
  } else if (tag.is_float()) {
    st->print_cr(" %f", constants->float_at(i));
  } else if (tag.is_double()) {
    st->print_cr(" %f", constants->double_at(i));
  } else if (tag.is_string()) {
    oop string = constants->pseudo_string_at(i);
    print_oop(string, st);
  } else if (tag.is_unresolved_string()) {
    const char* string = constants->string_at_noresolve(i);
    st->print_cr(" %s", string);
  } else if (tag.is_klass()) {
    st->print_cr(" %s", constants->resolved_klass_at(i)->klass_part()->external_name());
  } else if (tag.is_unresolved_klass()) {
    st->print_cr(" <unresolved klass at %d>", i);
  } else if (tag.is_object()) {
    st->print(" <Object>");
    print_oop(constants->object_at(i), st);
  } else if (tag.is_method_type()) {
    int i2 = constants->method_type_index_at(i);
    st->print(" <MethodType> %d", i2);
    print_oop(constants->symbol_at(i2), st);
  } else if (tag.is_method_handle()) {
    int kind = constants->method_handle_ref_kind_at(i);
    int i2 = constants->method_handle_index_at(i);
    st->print(" <MethodHandle of kind %d>", kind, i2);
    print_field_or_method(-i, i2, st);
  } else {
    st->print_cr(" bad tag=%d at %d", tag.value(), i);
  }
}

void BytecodePrinter::print_field_or_method(int i, outputStream* st) {
  int orig_i = i;
  if (!check_index(orig_i, i, st))  return;
  print_field_or_method(orig_i, i, st);
}

void BytecodePrinter::print_field_or_method(int orig_i, int i, outputStream* st) {
  constantPoolOop constants = method()->constants();
  constantTag tag = constants->tag_at(i);

  bool has_klass = true;

  switch (tag.value()) {
  case JVM_CONSTANT_InterfaceMethodref:
  case JVM_CONSTANT_Methodref:
  case JVM_CONSTANT_Fieldref:
    break;
  case JVM_CONSTANT_NameAndType:
  case JVM_CONSTANT_InvokeDynamic:
  case JVM_CONSTANT_InvokeDynamicTrans:
    has_klass = false;
    break;
  default:
    st->print_cr(" bad tag=%d at %d", tag.value(), i);
    return;
  }

  symbolOop name = constants->uncached_name_ref_at(i);
  symbolOop signature = constants->uncached_signature_ref_at(i);
  const char* sep = (tag.is_field() ? "/" : "");
  if (has_klass) {
    symbolOop klass = constants->klass_name_at(constants->uncached_klass_ref_index_at(i));
    st->print_cr(" %d <%s.%s%s%s> ", i, klass->as_C_string(), name->as_C_string(), sep, signature->as_C_string());
  } else {
    if (tag.is_invoke_dynamic()) {
      int bsm = constants->invoke_dynamic_bootstrap_method_ref_index_at(i);
      st->print(" bsm=%d", bsm);
    }
    st->print_cr(" %d <%s%s%s>", i, name->as_C_string(), sep, signature->as_C_string());
  }
}


void BytecodePrinter::print_attributes(int bci, outputStream* st) {
  // Show attributes of pre-rewritten codes
  Bytecodes::Code code = Bytecodes::java_code(raw_code());
  // If the code doesn't have any fields there's nothing to print.
  // note this is ==1 because the tableswitch and lookupswitch are
  // zero size (for some reason) and we want to print stuff out for them.
  if (Bytecodes::length_for(code) == 1) {
    st->cr();
    return;
  }

  switch(code) {
    // Java specific bytecodes only matter.
    case Bytecodes::_bipush:
      st->print_cr(" " INT32_FORMAT, get_byte());
      break;
    case Bytecodes::_sipush:
      st->print_cr(" " INT32_FORMAT, get_short());
      break;
    case Bytecodes::_ldc:
      if (Bytecodes::uses_cp_cache(raw_code())) {
        print_constant(get_index_u1_cpcache(), st);
      } else {
        print_constant(get_index_u1(), st);
      }
      break;

    case Bytecodes::_ldc_w:
    case Bytecodes::_ldc2_w:
      if (Bytecodes::uses_cp_cache(raw_code())) {
        print_constant(get_index_u2_cpcache(), st);
      } else {
        print_constant(get_index_u2(), st);
      }
      break;

    case Bytecodes::_iload:
    case Bytecodes::_lload:
    case Bytecodes::_fload:
    case Bytecodes::_dload:
    case Bytecodes::_aload:
    case Bytecodes::_istore:
    case Bytecodes::_lstore:
    case Bytecodes::_fstore:
    case Bytecodes::_dstore:
    case Bytecodes::_astore:
      st->print_cr(" #%d", get_index_special());
      break;

    case Bytecodes::_iinc:
      { int index = get_index_special();
        jint offset = is_wide() ? get_short(): get_byte();
        st->print_cr(" #%d " INT32_FORMAT, index, offset);
      }
      break;

    case Bytecodes::_newarray: {
        BasicType atype = (BasicType)get_index_u1();
        const char* str = type2name(atype);
        if (str == NULL || atype == T_OBJECT || atype == T_ARRAY) {
          assert(false, "Unidentified basic type");
        }
        st->print_cr(" %s", str);
      }
      break;
    case Bytecodes::_anewarray: {
        int klass_index = get_index_u2();
        constantPoolOop constants = method()->constants();
        symbolOop name = constants->klass_name_at(klass_index);
        st->print_cr(" %s ", name->as_C_string());
      }
      break;
    case Bytecodes::_multianewarray: {
        int klass_index = get_index_u2();
        int nof_dims = get_index_u1();
        constantPoolOop constants = method()->constants();
        symbolOop name = constants->klass_name_at(klass_index);
        st->print_cr(" %s %d", name->as_C_string(), nof_dims);
      }
      break;

    case Bytecodes::_ifeq:
    case Bytecodes::_ifnull:
    case Bytecodes::_iflt:
    case Bytecodes::_ifle:
    case Bytecodes::_ifne:
    case Bytecodes::_ifnonnull:
    case Bytecodes::_ifgt:
    case Bytecodes::_ifge:
    case Bytecodes::_if_icmpeq:
    case Bytecodes::_if_icmpne:
    case Bytecodes::_if_icmplt:
    case Bytecodes::_if_icmpgt:
    case Bytecodes::_if_icmple:
    case Bytecodes::_if_icmpge:
    case Bytecodes::_if_acmpeq:
    case Bytecodes::_if_acmpne:
    case Bytecodes::_goto:
    case Bytecodes::_jsr:
      st->print_cr(" %d", bci + get_short());
      break;

    case Bytecodes::_goto_w:
    case Bytecodes::_jsr_w:
      st->print_cr(" %d", bci + get_int());
      break;

    case Bytecodes::_ret: st->print_cr(" %d", get_index_special()); break;

    case Bytecodes::_tableswitch:
      { align();
        int  default_dest = bci + get_int();
        int  lo           = get_int();
        int  hi           = get_int();
        int  len          = hi - lo + 1;
        jint* dest        = NEW_RESOURCE_ARRAY(jint, len);
        for (int i = 0; i < len; i++) {
          dest[i] = bci + get_int();
        }
        st->print(" %d " INT32_FORMAT " " INT32_FORMAT " ",
                      default_dest, lo, hi);
        int first = true;
        for (int ll = lo; ll <= hi; ll++, first = false)  {
          int idx = ll - lo;
          const char *format = first ? " %d:" INT32_FORMAT " (delta: %d)" :
                                       ", %d:" INT32_FORMAT " (delta: %d)";
          st->print(format, ll, dest[idx], dest[idx]-bci);
        }
        st->cr();
      }
      break;
    case Bytecodes::_lookupswitch:
      { align();
        int  default_dest = bci + get_int();
        int  len          = get_int();
        jint* key         = NEW_RESOURCE_ARRAY(jint, len);
        jint* dest        = NEW_RESOURCE_ARRAY(jint, len);
        for (int i = 0; i < len; i++) {
          key [i] = get_int();
          dest[i] = bci + get_int();
        };
        st->print(" %d %d ", default_dest, len);
        bool first = true;
        for (int ll = 0; ll < len; ll++, first = false)  {
          const char *format = first ? " " INT32_FORMAT ":" INT32_FORMAT :
                                       ", " INT32_FORMAT ":" INT32_FORMAT ;
          st->print(format, key[ll], dest[ll]);
        }
        st->cr();
      }
      break;

    case Bytecodes::_putstatic:
    case Bytecodes::_getstatic:
    case Bytecodes::_putfield:
    case Bytecodes::_getfield:
      print_field_or_method(get_index_u2_cpcache(), st);
      break;

    case Bytecodes::_invokevirtual:
    case Bytecodes::_invokespecial:
    case Bytecodes::_invokestatic:
      print_field_or_method(get_index_u2_cpcache(), st);
      break;

    case Bytecodes::_invokeinterface:
      { int i = get_index_u2_cpcache();
        int n = get_index_u1();
        get_byte();            // ignore zero byte
        print_field_or_method(i, st);
      }
      break;

    case Bytecodes::_invokedynamic:
      print_field_or_method(get_index_u4(), st);
      break;

    case Bytecodes::_new:
    case Bytecodes::_checkcast:
    case Bytecodes::_instanceof:
      { int i = get_index_u2();
        constantPoolOop constants = method()->constants();
        symbolOop name = constants->klass_name_at(i);
        st->print_cr(" %d <%s>", i, name->as_C_string());
      }
      break;

    case Bytecodes::_wide:
      // length is zero not one, but printed with no more info.
      break;

    default:
      ShouldNotReachHere();
      break;
  }
}


void BytecodePrinter::bytecode_epilog(int bci, outputStream* st) {
  methodDataOop mdo = method()->method_data();
  if (mdo != NULL) {
    ProfileData* data = mdo->bci_to_data(bci);
    if (data != NULL) {
      st->print("  %d", mdo->dp_to_di(data->dp()));
      st->fill_to(6);
      data->print_data_on(st);
    }
  }
}
#endif // PRODUCT
