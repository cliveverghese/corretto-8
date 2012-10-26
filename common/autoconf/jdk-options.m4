#
# Copyright (c) 2011, 2012, Oracle and/or its affiliates. All rights reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# This code is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License version 2 only, as
# published by the Free Software Foundation.  Oracle designates this
# particular file as subject to the "Classpath" exception as provided
# by Oracle in the LICENSE file that accompanied this code.
#
# This code is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
# version 2 for more details (a copy is included in the LICENSE file that
# accompanied this code).
#
# You should have received a copy of the GNU General Public License version
# 2 along with this work; if not, write to the Free Software Foundation,
# Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
#
# Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
# or visit www.oracle.com if you need additional information or have any
# questions.
#

AC_DEFUN_ONCE([JDKOPT_SETUP_JDK_VARIANT],
[
###############################################################################
#
# Check which variant of the JDK that we want to build.
# Currently we have:
#    normal:   standard edition   
# but the custom make system may add other variants
#
# Effectively the JDK variant gives a name to a specific set of
# modules to compile into the JDK. In the future, these modules
# might even be Jigsaw modules.
#
AC_MSG_CHECKING([which variant of the JDK to build])
AC_ARG_WITH([jdk-variant], [AS_HELP_STRING([--with-jdk-variant],
	[JDK variant to build (normal) @<:@normal@:>@])])

if test "x$with_jdk_variant" = xnormal || test "x$with_jdk_variant" = x; then
    JDK_VARIANT="normal"
else
    AC_MSG_ERROR([The available JDK variants are: normal])
fi

AC_SUBST(JDK_VARIANT)

AC_MSG_RESULT([$JDK_VARIANT])
])

AC_DEFUN_ONCE([JDKOPT_SETUP_JVM_VARIANTS],
[

###############################################################################
#
# Check which variants of the JVM that we want to build.
# Currently we have:
#    server: normal interpreter and a tiered C1/C2 compiler
#    client: normal interpreter and C1 (no C2 compiler) (only 32-bit platforms)
#    kernel: kernel footprint JVM that passes the TCK without major performance problems,
#             ie normal interpreter and C1, only the serial GC, kernel jvmti etc
#    zero: no machine code interpreter, no compiler
#    zeroshark: zero interpreter and shark/llvm compiler backend
AC_MSG_CHECKING([which variants of the JVM to build])
AC_ARG_WITH([jvm-variants], [AS_HELP_STRING([--with-jvm-variants],
	[JVM variants (separated by commas) to build (server, client, kernel, zero, zeroshark) @<:@server@:>@])])

if test "x$with_jvm_variants" = x; then
     with_jvm_variants="server"
fi

JVM_VARIANTS=",$with_jvm_variants,"
TEST_VARIANTS=`$ECHO "$JVM_VARIANTS" | $SED -e 's/server,//' -e 's/client,//' -e 's/kernel,//' -e 's/zero,//' -e 's/zeroshark,//'`

if test "x$TEST_VARIANTS" != "x,"; then
   AC_MSG_ERROR([The available JVM variants are: server, client, kernel, zero, zeroshark])
fi   
AC_MSG_RESULT([$with_jvm_variants])

JVM_VARIANT_SERVER=`$ECHO "$JVM_VARIANTS" | $SED -e '/,server,/!s/.*/false/g' -e '/,server,/s/.*/true/g'`
JVM_VARIANT_CLIENT=`$ECHO "$JVM_VARIANTS" | $SED -e '/,client,/!s/.*/false/g' -e '/,client,/s/.*/true/g'` 
JVM_VARIANT_KERNEL=`$ECHO "$JVM_VARIANTS" | $SED -e '/,kernel,/!s/.*/false/g' -e '/,kernel,/s/.*/true/g'`
JVM_VARIANT_ZERO=`$ECHO "$JVM_VARIANTS" | $SED -e '/,zero,/!s/.*/false/g' -e '/,zero,/s/.*/true/g'`
JVM_VARIANT_ZEROSHARK=`$ECHO "$JVM_VARIANTS" | $SED -e '/,zeroshark,/!s/.*/false/g' -e '/,zeroshark,/s/.*/true/g'`

if test "x$JVM_VARIANT_CLIENT" = xtrue; then
    if test "x$OPENJDK_TARGET_CPU_BITS" = x64; then
        AC_MSG_ERROR([You cannot build a client JVM for a 64-bit machine.])
    fi
fi
if test "x$JVM_VARIANT_KERNEL" = xtrue; then
    if test "x$OPENJDK_TARGET_CPU_BITS" = x64; then
        AC_MSG_ERROR([You cannot build a kernel JVM for a 64-bit machine.])
    fi
fi

# Replace the commas with AND for use in the build directory name.
ANDED_JVM_VARIANTS=`$ECHO "$JVM_VARIANTS" | $SED -e 's/^,//' -e 's/,$//' -e 's/,/AND/'`
COUNT_VARIANTS=`$ECHO "$JVM_VARIANTS" | $SED -e 's/server,/1/' -e 's/client,/1/' -e 's/kernel,/1/' -e 's/zero,/1/' -e 's/zeroshark,/1/'`
if test "x$COUNT_VARIANTS" != "x,1"; then
    BUILDING_MULTIPLE_JVM_VARIANTS=yes
else
    BUILDING_MULTIPLE_JVM_VARIANTS=no
fi

AC_SUBST(JVM_VARIANTS)
AC_SUBST(JVM_VARIANT_SERVER)
AC_SUBST(JVM_VARIANT_CLIENT)
AC_SUBST(JVM_VARIANT_KERNEL)
AC_SUBST(JVM_VARIANT_ZERO)
AC_SUBST(JVM_VARIANT_ZEROSHARK)

if test "x$OPENJDK_TARGET_OS" = "xmacosx"; then
   MACOSX_UNIVERSAL="true"
fi

AC_SUBST(MACOSX_UNIVERSAL)

])

AC_DEFUN_ONCE([JDKOPT_SETUP_DEBUG_LEVEL],
[
###############################################################################
#
# Set the debug level
#    release: no debug information, all optimizations, no asserts.
#    fastdebug: debug information (-g), all optimizations, all asserts
#    slowdebug: debug information (-g), no optimizations, all asserts
#
DEBUG_LEVEL="release"              
AC_MSG_CHECKING([which debug level to use])
AC_ARG_ENABLE([debug], [AS_HELP_STRING([--enable-debug],
	[set the debug level to fastdebug (shorthand for --with-debug-level=fastdebug) @<:@disabled@:>@])],
	[
        ENABLE_DEBUG="${enableval}"
        DEBUG_LEVEL="fastdebug"
    ], [ENABLE_DEBUG="no"])

AC_ARG_WITH([debug-level], [AS_HELP_STRING([--with-debug-level],
	[set the debug level (release, fastdebug, slowdebug) @<:@release@:>@])],
	[
        DEBUG_LEVEL="${withval}"
        if test "x$ENABLE_DEBUG" = xyes; then
			AC_MSG_ERROR([You cannot use both --enable-debug and --with-debug-level at the same time.])
        fi
    ])
AC_MSG_RESULT([$DEBUG_LEVEL])

if test "x$DEBUG_LEVEL" != xrelease && \
   test "x$DEBUG_LEVEL" != xfastdebug && \
   test "x$DEBUG_LEVEL" != xslowdebug; then
   AC_MSG_ERROR([Allowed debug levels are: release, fastdebug and slowdebug])
fi


###############################################################################
#
# Setup legacy vars/targets and new vars to deal with different debug levels.
#

case $DEBUG_LEVEL in
      release )
          VARIANT="OPT"
          FASTDEBUG="false"
          DEBUG_CLASSFILES="false"            
          BUILD_VARIANT_RELEASE=""             
          HOTSPOT_DEBUG_LEVEL="product"
          HOTSPOT_EXPORT="product"
           ;;
      fastdebug )
          VARIANT="DBG"
          FASTDEBUG="true"
          DEBUG_CLASSFILES="true"            
          BUILD_VARIANT_RELEASE="-fastdebug"
          HOTSPOT_DEBUG_LEVEL="fastdebug"   
          HOTSPOT_EXPORT="fastdebug"
           ;;
      slowdebug )
          VARIANT="DBG"
          FASTDEBUG="false"
          DEBUG_CLASSFILES="true"            
          BUILD_VARIANT_RELEASE="-debug"             
          HOTSPOT_DEBUG_LEVEL="jvmg"
          HOTSPOT_EXPORT="debug"
           ;;
esac

#####
# Generate the legacy makefile targets for hotspot.
# The hotspot api for selecting the build artifacts, really, needs to be improved.
#
HOTSPOT_TARGET=""

if test "x$JVM_VARIANT_SERVER" = xtrue; then
    HOTSPOT_TARGET="$HOTSPOT_TARGET${HOTSPOT_DEBUG_LEVEL} "
fi

if test "x$JVM_VARIANT_CLIENT" = xtrue; then
    HOTSPOT_TARGET="$HOTSPOT_TARGET${HOTSPOT_DEBUG_LEVEL}1 "
fi

if test "x$JVM_VARIANT_KERNEL" = xtrue; then
    HOTSPOT_TARGET="$HOTSPOT_TARGET${HOTSPOT_DEBUG_LEVEL}kernel "
fi

if test "x$JVM_VARIANT_ZERO" = xtrue; then
    HOTSPOT_TARGET="$HOTSPOT_TARGET${HOTSPOT_DEBUG_LEVEL}zero "
fi

if test "x$JVM_VARIANT_ZEROSHARK" = xtrue; then
    HOTSPOT_TARGET="$HOTSPOT_TARGET${HOTSPOT_DEBUG_LEVEL}shark "
fi

HOTSPOT_TARGET="$HOTSPOT_TARGET docs export_$HOTSPOT_EXPORT"

# On Macosx universal binaries are produced, but they only contain
# 64 bit intel. This invalidates control of which jvms are built
# from configure, but only server is valid anyway. Fix this
# when hotspot makefiles are rewritten.
if test "x$MACOSX_UNIVERSAL" = xtrue; then
    HOTSPOT_TARGET=universal_product
fi

#####

AC_SUBST(DEBUG_LEVEL)
AC_SUBST(VARIANT)
AC_SUBST(FASTDEBUG)
AC_SUBST(DEBUG_CLASSFILES)
AC_SUBST(BUILD_VARIANT_RELEASE)
])

AC_DEFUN_ONCE([JDKOPT_SETUP_JDK_OPTIONS],
[

###############################################################################
#
# Should we build only OpenJDK even if closed sources are present?
#
AC_ARG_ENABLE([openjdk-only], [AS_HELP_STRING([--enable-openjdk-only],
    [supress building closed source even if present @<:@disabled@:>@])],,[enable_openjdk_only="no"])

AC_MSG_CHECKING([for presence of closed sources])
if test -d "$SRC_ROOT/jdk/src/closed"; then
    CLOSED_SOURCE_PRESENT=yes
else
    CLOSED_SOURCE_PRESENT=no
fi
AC_MSG_RESULT([$CLOSED_SOURCE_PRESENT])

AC_MSG_CHECKING([if closed source is supressed (openjdk-only)])
SUPRESS_CLOSED_SOURCE="$enable_openjdk_only"
AC_MSG_RESULT([$SUPRESS_CLOSED_SOURCE])

if test "x$CLOSED_SOURCE_PRESENT" = xno; then
  OPENJDK=true
  if test "x$SUPRESS_CLOSED_SOURCE" = "xyes"; then
    AC_MSG_WARN([No closed source present, --enable-openjdk-only makes no sense])
  fi
else
  if test "x$SUPRESS_CLOSED_SOURCE" = "xyes"; then
    OPENJDK=true
  else
    OPENJDK=false
  fi
fi

if test "x$OPENJDK" = "xtrue"; then
    SET_OPENJDK="OPENJDK=true"
fi

AC_SUBST(SET_OPENJDK)

###############################################################################
#
# Should we build a JDK/JVM with headful support (ie a graphical ui)?
# We always build headless support.
#
AC_MSG_CHECKING([headful support])
AC_ARG_ENABLE([headful], [AS_HELP_STRING([--disable-headful],
	[disable building headful support (graphical UI support) @<:@enabled@:>@])],
    [SUPPORT_HEADFUL=${enable_headful}], [SUPPORT_HEADFUL=yes])

SUPPORT_HEADLESS=yes
BUILD_HEADLESS="BUILD_HEADLESS:=true"

if test "x$SUPPORT_HEADFUL" = xyes; then
    # We are building both headful and headless.
    headful_msg="inlude support for both headful and headless"
fi

if test "x$SUPPORT_HEADFUL" = xno; then
    # Thus we are building headless only.
    BUILD_HEADLESS="BUILD_HEADLESS:=true"
    headful_msg="headless only"
fi

AC_MSG_RESULT([$headful_msg])

AC_SUBST(SUPPORT_HEADLESS)
AC_SUBST(SUPPORT_HEADFUL)
AC_SUBST(BUILD_HEADLESS)

# Control wether Hotspot runs Queens test after build.
AC_ARG_ENABLE([hotspot-test-in-build], [AS_HELP_STRING([--enable-hotspot-test-in-build],
	[run the Queens test after Hotspot build @<:@disabled@:>@])],,
    [enable_hotspot_test_in_build=no])
if test "x$enable_hotspot_test_in_build" = "xyes"; then
    TEST_IN_BUILD=true
else
    TEST_IN_BUILD=false
fi
AC_SUBST(TEST_IN_BUILD)

###############################################################################
#
# Choose cacerts source file
#
AC_ARG_WITH(cacerts-file, [AS_HELP_STRING([--with-cacerts-file],
    [specify alternative cacerts file])])
if test "x$with_cacerts_file" != x; then
    CACERTS_FILE=$with_cacerts_file
else
    if test "x$OPENJDK" = "xtrue"; then
        CACERTS_FILE=${SRC_ROOT}/jdk/src/share/lib/security/cacerts
    else
        CACERTS_FILE=${SRC_ROOT}/jdk/src/closed/share/lib/security/cacerts.internal
    fi
fi
AC_SUBST(CACERTS_FILE)

###############################################################################
#
# Compress jars
#
COMPRESS_JARS=false

AC_SUBST(COMPRESS_JARS)
])

AC_DEFUN_ONCE([JDKOPT_SETUP_JDK_VERSION_NUMBERS],
[
# Source the version numbers
. $AUTOCONF_DIR/version.numbers
if test "x$OPENJDK" = "xfalse"; then
    . $AUTOCONF_DIR/closed.version.numbers
fi
# Now set the JDK version, milestone, build number etc.
AC_SUBST(JDK_MAJOR_VERSION)
AC_SUBST(JDK_MINOR_VERSION)
AC_SUBST(JDK_MICRO_VERSION)
AC_SUBST(JDK_UPDATE_VERSION)
AC_SUBST(JDK_BUILD_NUMBER)
AC_SUBST(MILESTONE)
AC_SUBST(LAUNCHER_NAME)
AC_SUBST(PRODUCT_NAME)
AC_SUBST(PRODUCT_SUFFIX)
AC_SUBST(JDK_RC_PLATFORM_NAME)
AC_SUBST(COMPANY_NAME)
AC_SUBST(MACOSX_BUNDLE_NAME_BASE)
AC_SUBST(MACOSX_BUNDLE_ID_BASE)

COPYRIGHT_YEAR=`date +'%Y'`
AC_SUBST(COPYRIGHT_YEAR)

RUNTIME_NAME="$PRODUCT_NAME $PRODUCT_SUFFIX"
AC_SUBST(RUNTIME_NAME)

if test "x$JDK_UPDATE_VERSION" != x; then
    JDK_VERSION="${JDK_MAJOR_VERSION}.${JDK_MINOR_VERSION}.${JDK_MICRO_VERSION}_${JDK_UPDATE_VERSION}"
else
    JDK_VERSION="${JDK_MAJOR_VERSION}.${JDK_MINOR_VERSION}.${JDK_MICRO_VERSION}"
fi
AC_SUBST(JDK_VERSION)

if test "x$MILESTONE" != x; then
    RELEASE="${JDK_VERSION}-${MILESTONE}${BUILD_VARIANT_RELEASE}"
else
    RELEASE="${JDK_VERSION}${BUILD_VARIANT_RELEASE}"
fi
AC_SUBST(RELEASE)

if test "x$JDK_BUILD_NUMBER" != x; then
    FULL_VERSION="${RELEASE}-${JDK_BUILD_NUMBER}"
else
    JDK_BUILD_NUMBER=b00
    BUILD_DATE=`date '+%Y_%m_%d_%H_%M'`
    # Avoid [:alnum:] since it depends on the locale.
    CLEAN_USERNAME=`echo "$USER" | $TR -d -c 'abcdefghijklmnopqrstuvqxyz0123456789'`
    USER_RELEASE_SUFFIX=`echo "${CLEAN_USERNAME}_${BUILD_DATE}" | $TR 'ABCDEFGHIJKLMNOPQRSTUVWXYZ' 'abcdefghijklmnopqrstuvwxyz'`
    FULL_VERSION="${RELEASE}-${USER_RELEASE_SUFFIX}-${JDK_BUILD_NUMBER}"
fi
AC_SUBST(FULL_VERSION)
COOKED_BUILD_NUMBER=`$ECHO $JDK_BUILD_NUMBER | $SED -e 's/^b//' -e 's/^0//'`
AC_SUBST(COOKED_BUILD_NUMBER)
])

AC_DEFUN_ONCE([JDKOPT_SETUP_BUILD_TWEAKS],
[
HOTSPOT_MAKE_ARGS="$HOTSPOT_TARGET"
AC_SUBST(HOTSPOT_MAKE_ARGS)

# The name of the Service Agent jar.
SALIB_NAME="${LIBRARY_PREFIX}saproc${SHARED_LIBRARY_SUFFIX}"
if test "x$OPENJDK_TARGET_OS" = "xwindows"; then
    SALIB_NAME="${LIBRARY_PREFIX}sawindbg${SHARED_LIBRARY_SUFFIX}"
fi
AC_SUBST(SALIB_NAME)

])

AC_DEFUN_ONCE([JDKOPT_SETUP_DEBUG_SYMBOLS],
[
#
# ENABLE_DEBUG_SYMBOLS
# This must be done after the toolchain is setup, since we're looking at objcopy.
#
ENABLE_DEBUG_SYMBOLS=default

# default on macosx is no...
if test "x$OPENJDK_TARGET_OS" = xmacosx; then
   ENABLE_DEBUG_SYMBOLS=no
fi

AC_ARG_ENABLE([debug-symbols],
              [AS_HELP_STRING([--disable-debug-symbols],[disable generation of debug symbols @<:@enabled@:>@])],
              [ENABLE_DEBUG_SYMBOLS=${enable_debug_symbols}],
)

AC_MSG_CHECKING([if we should generate debug symbols])

if test "x$ENABLE_DEBUG_SYMBOLS" = "xyes" && test "x$OBJCOPY" = x; then
   # explicit enabling of enable-debug-symbols and can't find objcopy
   #   this is an error
   AC_MSG_ERROR([Unable to find objcopy, cannot enable debug-symbols])
fi

if test "x$ENABLE_DEBUG_SYMBOLS" = "xdefault"; then
  # Default is on if objcopy is found, otherwise off
  if test "x$OBJCOPY" != x || test "x$OPENJDK_TARGET_OS" = xwindows; then
     ENABLE_DEBUG_SYMBOLS=yes
  else
     ENABLE_DEBUG_SYMBOLS=no
  fi
fi

AC_MSG_RESULT([$ENABLE_DEBUG_SYMBOLS])

#
# ZIP_DEBUGINFO_FILES
#
ZIP_DEBUGINFO_FILES=yes

AC_ARG_ENABLE([zip-debug-info],
              [AS_HELP_STRING([--disable-zip-debug-info],[disable zipping of debug-info files @<:@enabled@:>@])],
              [ZIP_DEBUGINFO_FILES=${enable_zip_debug_info}],
)

AC_MSG_CHECKING([if we should zip debug-info files])
AC_MSG_RESULT([$ZIP_DEBUGINFO_FILES])

# Hotspot wants ZIP_DEBUGINFO_FILES to be 1 for yes
#   use that...
if test "x$ZIP_DEBUGINFO_FILES" = "xyes"; then
   ZIP_DEBUGINFO_FILES=1
else
   ZIP_DEBUGINFO_FILES=0
fi

AC_SUBST(ENABLE_DEBUG_SYMBOLS)
AC_SUBST(ZIP_DEBUGINFO_FILES)
AC_SUBST(CFLAGS_DEBUG_SYMBOLS)
AC_SUBST(CXXFLAGS_DEBUG_SYMBOLS)
])

# Support for customization of the build process. Some build files
# will include counterparts from this location, if they exist. This allows
# for a degree of customization of the build targets and the rules/recipes
# to create them
AC_ARG_WITH([custom-make-dir], [AS_HELP_STRING([--with-custom-make-dir],
    [use this directory for custom build/make files])], [CUSTOM_MAKE_DIR=$with_custom_make_dir])
AC_SUBST(CUSTOM_MAKE_DIR)
