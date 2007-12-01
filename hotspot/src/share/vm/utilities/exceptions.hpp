/*
 * Copyright 1998-2007 Sun Microsystems, Inc.  All Rights Reserved.
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

// This file provides the basic support for exception handling in the VM.
// Note: We do not use C++ exceptions to avoid compiler dependencies and
// unpredictable performance.
//
// Scheme: Exceptions are stored with the thread. There is never more
// than one pending exception per thread. All functions that can throw
// an exception carry a THREAD argument (usually the last argument and
// declared with the TRAPS macro). Throwing an exception means setting
// a pending exception in the thread. Upon return from a function that
// can throw an exception, we must check if an exception is pending.
// The CHECK macros do this in a convenient way. Carrying around the
// thread provides also convenient access to it (e.g. for Handle
// creation, w/o the need for recomputation).



// Forward declarations to be independent of the include structure.
// This allows us to have exceptions.hpp included in top.hpp.

class Thread;
class Handle;
class symbolHandle;
class symbolOopDesc;
class JavaCallArguments;

// The ThreadShadow class is a helper class to access the _pending_exception
// field of the Thread class w/o having access to the Thread's interface (for
// include hierachy reasons).

class ThreadShadow: public CHeapObj {
 protected:
  oop  _pending_exception;                       // Thread has gc actions.
  const char* _exception_file;                   // file information for exception (debugging only)
  int         _exception_line;                   // line information for exception (debugging only)
  friend void check_ThreadShadow();              // checks _pending_exception offset

  // The following virtual exists only to force creation of a vtable.
  // We need ThreadShadow to have a vtable, even in product builds,
  // so that its layout will start at an offset of zero relative to Thread.
  // Some C++ compilers are so "clever" that they put the ThreadShadow
  // base class at offset 4 in Thread (after Thread's vtable), if they
  // notice that Thread has a vtable but ThreadShadow does not.
  virtual void unused_initial_virtual() { }

 public:
  oop  pending_exception() const                 { return _pending_exception; }
  bool has_pending_exception() const             { return _pending_exception != NULL; }
  const char* exception_file() const             { return _exception_file; }
  int  exception_line() const                    { return _exception_line; }

  // Code generation support
  static ByteSize pending_exception_offset()     { return byte_offset_of(ThreadShadow, _pending_exception); }

  // use THROW whenever possible!
  void set_pending_exception(oop exception, const char* file, int line);

  // use CLEAR_PENDING_EXCEPTION whenever possible!
  void clear_pending_exception();

  ThreadShadow() : _pending_exception(NULL),
                   _exception_file(NULL), _exception_line(0) {}
};


// Exceptions is a helper class that encapsulates all operations
// that require access to the thread interface and which are
// relatively rare. The Exceptions operations should only be
// used directly if the macros below are insufficient.

class Exceptions {
  static bool special_exception(Thread *thread, const char* file, int line, Handle exception);
  static bool special_exception(Thread* thread, const char* file, int line, symbolHandle name, const char* message);
 public:
  // this enum is defined to indicate whether it is safe to
  // ignore the encoding scheme of the original message string.
  typedef enum {
    safe_to_utf8 = 0,
    unsafe_to_utf8 = 1
  } ExceptionMsgToUtf8Mode;
  // Throw exceptions: w/o message, w/ message & with formatted message.
  static void _throw_oop(Thread* thread, const char* file, int line, oop exception);
  static void _throw(Thread* thread, const char* file, int line, Handle exception);
  static void _throw_msg(Thread* thread, const char* file, int line,
                         symbolHandle name, const char* message, Handle loader,
                         Handle protection_domain);
  static void _throw_msg(Thread* thread, const char* file, int line,
                         symbolOop name, const char* message);
  static void _throw_msg(Thread* thread, const char* file, int line,
                         symbolHandle name, const char* message);
  static void _throw_args(Thread* thread, const char* file, int line,
                          symbolHandle name, symbolHandle signature,
                          JavaCallArguments* args);
  static void _throw_msg_cause(Thread* thread, const char* file,
                         int line, symbolHandle h_name, const char* message,
                         Handle h_cause, Handle h_loader, Handle h_protection_domain);
  static void _throw_msg_cause(Thread* thread, const char* file, int line,
                            symbolHandle name, const char* message, Handle cause);

  // There is no THROW... macro for this method. Caller should remember
  // to do a return after calling it.
  static void fthrow(Thread* thread, const char* file, int line, symbolHandle name,
                     const char* format, ...);

  // Create and initialize a new exception
  static Handle new_exception(Thread* thread, symbolHandle name,
                              symbolHandle signature, JavaCallArguments* args,
                              Handle cause, Handle loader,
                              Handle protection_domain);

  static Handle new_exception(Thread* thread, symbolHandle name,
                              const char* message, Handle cause, Handle loader,
                              Handle protection_domain,
                              ExceptionMsgToUtf8Mode to_utf8_safe = safe_to_utf8);

 static Handle new_exception(Thread* thread, symbolOop name,
                             const char* message,
                             ExceptionMsgToUtf8Mode to_utf8_safe = safe_to_utf8);

  static void throw_stack_overflow_exception(Thread* thread, const char* file, int line);

  // for AbortVMOnException flag
  NOT_PRODUCT(static void debug_check_abort(Handle exception);)
  NOT_PRODUCT(static void debug_check_abort(const char *value_string);)
};


// The THREAD & TRAPS macros facilitate the declaration of functions that throw exceptions.
// Convention: Use the TRAPS macro as the last argument of such a function; e.g.:
//
// int this_function_may_trap(int x, float y, TRAPS)

#define THREAD __the_thread__
#define TRAPS  Thread* THREAD


// The CHECK... macros should be used to pass along a THREAD reference and to check for pending
// exceptions. In special situations it is necessary to handle pending exceptions explicitly,
// in these cases the PENDING_EXCEPTION helper macros should be used.
//
// Macro naming conventions: Macros that end with _ require a result value to be returned. They
// are for functions with non-void result type. The result value is usually ignored because of
// the exception and is only needed for syntactic correctness. The _0 ending is a shortcut for
// _(0) since this is a frequent case. Example:
//
// int result = this_function_may_trap(x_arg, y_arg, CHECK_0);
//
// CAUTION: make sure that the function call using a CHECK macro is not the only statement of a
// conditional branch w/o enclosing {} braces, since the CHECK macros expand into several state-
// ments!

#define PENDING_EXCEPTION                        (((ThreadShadow*)THREAD)->pending_exception())
#define HAS_PENDING_EXCEPTION                    (((ThreadShadow*)THREAD)->has_pending_exception())
#define CLEAR_PENDING_EXCEPTION                  (((ThreadShadow*)THREAD)->clear_pending_exception())

#define CHECK                                    THREAD); if (HAS_PENDING_EXCEPTION) return       ; (0
#define CHECK_(result)                           THREAD); if (HAS_PENDING_EXCEPTION) return result; (0
#define CHECK_0                                  CHECK_(0)
#define CHECK_NH                                 CHECK_(Handle())
#define CHECK_NULL                               CHECK_(NULL)
#define CHECK_false                              CHECK_(false)

// The THROW... macros should be used to throw an exception. They require a THREAD variable to be
// visible within the scope containing the THROW. Usually this is achieved by declaring the function
// with a TRAPS argument.

#define THREAD_AND_LOCATION                      THREAD, __FILE__, __LINE__

#define THROW_OOP(e)                                \
  { Exceptions::_throw_oop(THREAD_AND_LOCATION, e);                             return;  }

#define THROW_HANDLE(e)                                \
  { Exceptions::_throw(THREAD_AND_LOCATION, e);                             return;  }

#define THROW(name)                                 \
  { Exceptions::_throw_msg(THREAD_AND_LOCATION, name, NULL); return;  }

#define THROW_MSG(name, message)                    \
  { Exceptions::_throw_msg(THREAD_AND_LOCATION, name, message); return;  }

#define THROW_MSG_LOADER(name, message, loader, protection_domain) \
  { Exceptions::_throw_msg(THREAD_AND_LOCATION, name, message, loader, protection_domain); return;  }

#define THROW_ARG(name, signature, args) \
  { Exceptions::_throw_args(THREAD_AND_LOCATION, name, signature, args);   return; }

#define THROW_OOP_(e, result)                       \
  { Exceptions::_throw_oop(THREAD_AND_LOCATION, e);                           return result; }

#define THROW_HANDLE_(e, result)                       \
  { Exceptions::_throw(THREAD_AND_LOCATION, e);                           return result; }

#define THROW_(name, result)                        \
  { Exceptions::_throw_msg(THREAD_AND_LOCATION, name, NULL); return result; }

#define THROW_MSG_(name, message, result)           \
  { Exceptions::_throw_msg(THREAD_AND_LOCATION, name, message); return result; }

#define THROW_MSG_LOADER_(name, message, loader, protection_domain, result) \
  { Exceptions::_throw_msg(THREAD_AND_LOCATION, name, message, loader, protection_domain); return result; }

#define THROW_ARG_(name, signature, args, result) \
  { Exceptions::_throw_args(THREAD_AND_LOCATION, name, signature, args); return result; }

#define THROW_MSG_CAUSE_(name, message, cause, result)   \
  { Exceptions::_throw_msg_cause(THREAD_AND_LOCATION, name, message, cause); return result; }


#define THROW_OOP_0(e)                      THROW_OOP_(e, 0)
#define THROW_HANDLE_0(e)                   THROW_HANDLE_(e, 0)
#define THROW_0(name)                       THROW_(name, 0)
#define THROW_MSG_0(name, message)          THROW_MSG_(name, message, 0)
#define THROW_WRAPPED_0(name, oop_to_wrap)  THROW_WRAPPED_(name, oop_to_wrap, 0)
#define THROW_ARG_0(name, signature, arg)   THROW_ARG_(name, signature, arg, 0)
#define THROW_MSG_CAUSE_0(name, message, cause) THROW_MSG_CAUSE_(name, message, cause, 0)

// The CATCH macro checks that no exception has been thrown by a function; it is used at
// call sites about which is statically known that the callee cannot throw an exception
// even though it is declared with TRAPS.

#define CATCH                              \
  THREAD); if (HAS_PENDING_EXCEPTION) {    \
    oop ex = PENDING_EXCEPTION;            \
    CLEAR_PENDING_EXCEPTION;               \
    ex->print();                           \
    ShouldNotReachHere();                  \
  } (0


// ExceptionMark is a stack-allocated helper class for local exception handling.
// It is used with the EXCEPTION_MARK macro.

class ExceptionMark {
 private:
  Thread* _thread;

 public:
  ExceptionMark(Thread*& thread);
  ~ExceptionMark();
};



// Use an EXCEPTION_MARK for 'local' exceptions. EXCEPTION_MARK makes sure that no
// pending exception exists upon entering its scope and tests that no pending exception
// exists when leaving the scope.

// See also preserveException.hpp for PRESERVE_EXCEPTION_MARK macro,
// which preserves pre-existing exceptions and does not allow new
// exceptions.

#define EXCEPTION_MARK                           Thread* THREAD; ExceptionMark __em(THREAD);
