/*
 * Copyright 1999-2007 Sun Microsystems, Inc.  All Rights Reserved.
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

// ciInstanceKlass
//
// This class represents a klassOop in the HotSpot virtual machine
// whose Klass part is an instanceKlass.  It may or may not
// be loaded.
class ciInstanceKlass : public ciKlass {
  CI_PACKAGE_ACCESS
  friend class ciEnv;
  friend class ciMethod;
  friend class ciField;
  friend class ciBytecodeStream;

private:
  bool                   _is_shared;

  jobject                _loader;
  jobject                _protection_domain;

  bool                   _is_initialized;
  bool                   _is_linked;
  bool                   _has_finalizer;
  bool                   _has_subklass;
  ciFlags                _flags;
  jint                   _nonstatic_field_size;

  // Lazy fields get filled in only upon request.
  ciInstanceKlass*       _super;
  ciInstance*            _java_mirror;

  ciConstantPoolCache*   _field_cache;  // cached map index->field
  GrowableArray<ciField*>* _nonstatic_fields;

  enum { implementors_limit = instanceKlass::implementors_limit };
  ciInstanceKlass*       _implementors[implementors_limit];
  jint                   _nof_implementors;

protected:
  ciInstanceKlass(KlassHandle h_k);
  ciInstanceKlass(ciSymbol* name, jobject loader, jobject protection_domain);

  instanceKlass* get_instanceKlass() const {
    return (instanceKlass*)get_Klass();
  }

  oop loader();
  jobject loader_handle();

  oop protection_domain();
  jobject protection_domain_handle();

  const char* type_string() { return "ciInstanceKlass"; }

  void print_impl(outputStream* st);

  ciConstantPoolCache* field_cache();

  bool is_shared() { return _is_shared; }

  bool compute_shared_is_initialized();
  bool compute_shared_is_linked();
  bool compute_shared_has_subklass();
  int  compute_shared_nof_implementors();
  int  compute_nonstatic_fields();
  GrowableArray<ciField*>* compute_nonstatic_fields_impl(GrowableArray<ciField*>* super_fields);

public:
  // Has this klass been initialized?
  bool                   is_initialized() {
    if (_is_shared && !_is_initialized) {
      return is_loaded() && compute_shared_is_initialized();
    }
    return _is_initialized;
  }
  // Has this klass been linked?
  bool                   is_linked() {
    if (_is_shared && !_is_linked) {
      return is_loaded() && compute_shared_is_linked();
    }
    return _is_linked;
  }

  // General klass information.
  ciFlags                flags()          {
    assert(is_loaded(), "must be loaded");
    return _flags;
  }
  bool                   has_finalizer()  {
    assert(is_loaded(), "must be loaded");
    return _has_finalizer; }
  bool                   has_subklass()   {
    assert(is_loaded(), "must be loaded");
    if (_is_shared && !_has_subklass) {
      if (flags().is_final()) {
        return false;
      } else {
        return compute_shared_has_subklass();
      }
    }
    return _has_subklass;
  }
  jint                   size_helper()  {
    return (Klass::layout_helper_size_in_bytes(layout_helper())
            >> LogHeapWordSize);
  }
  jint                   nonstatic_field_size()  {
    assert(is_loaded(), "must be loaded");
    return _nonstatic_field_size; }
  ciInstanceKlass*       super();
  jint                   nof_implementors()  {
    assert(is_loaded(), "must be loaded");
    if (_is_shared)  return compute_shared_nof_implementors();
    return _nof_implementors;
  }

  ciInstanceKlass* get_canonical_holder(int offset);
  ciField* get_field_by_offset(int field_offset, bool is_static);
  // total number of nonstatic fields (including inherited):
  int nof_nonstatic_fields() {
    if (_nonstatic_fields == NULL)
      return compute_nonstatic_fields();
    else
      return _nonstatic_fields->length();
  }
  // nth nonstatic field (presented by ascending address)
  ciField* nonstatic_field_at(int i) {
    assert(_nonstatic_fields != NULL, "");
    return _nonstatic_fields->at(i);
  }

  ciInstanceKlass* unique_concrete_subklass();
  bool has_finalizable_subclass();

  bool contains_field_offset(int offset) {
      return (offset/wordSize) >= instanceOopDesc::header_size()
             && (offset/wordSize)-instanceOopDesc::header_size() < nonstatic_field_size();
  }

  // Get the instance of java.lang.Class corresponding to
  // this klass.  This instance is used for locking of
  // synchronized static methods of this klass.
  ciInstance*            java_mirror();

  // Java access flags
  bool is_public      () { return flags().is_public(); }
  bool is_final       () { return flags().is_final(); }
  bool is_super       () { return flags().is_super(); }
  bool is_interface   () { return flags().is_interface(); }
  bool is_abstract    () { return flags().is_abstract(); }

  ciMethod* find_method(ciSymbol* name, ciSymbol* signature);
  // Note:  To find a method from name and type strings, use ciSymbol::make,
  // but consider adding to vmSymbols.hpp instead.

  bool is_leaf_type();
  ciInstanceKlass* implementor(int n);

  // Is the defining class loader of this class the default loader?
  bool uses_default_loader();

  bool is_java_lang_Object();

  // What kind of ciObject is this?
  bool is_instance_klass() { return true; }
  bool is_java_klass()     { return true; }
};
