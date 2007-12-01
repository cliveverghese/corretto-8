/*
 * Copyright 2004-2007 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.security.ssl;

import java.lang.ref.*;
import java.util.*;
import static java.util.Locale.ENGLISH;
import java.util.concurrent.atomic.AtomicLong;
import java.net.Socket;

import java.security.*;
import java.security.KeyStore.*;
import java.security.cert.*;
import java.security.cert.Certificate;

import javax.net.ssl.*;

/**
 * The new X509 key manager implementation. The main differences to the
 * old SunX509 key manager are:
 *  . it is based around the KeyStore.Builder API. This allows it to use
 *    other forms of KeyStore protection or password input (e.g. a
 *    CallbackHandler) or to have keys within one KeyStore protected by
 *    different keys.
 *  . it can use multiple KeyStores at the same time.
 *  . it is explicitly designed to accomodate KeyStores that change over
 *    the lifetime of the process.
 *  . it makes an effort to choose the key that matches best, i.e. one that
 *    is not expired and has the appropriate certificate extensions.
 *
 * Note that this code is not explicitly performance optimzied yet.
 *
 * @author  Andreas Sterbenz
 */
final class X509KeyManagerImpl extends X509ExtendedKeyManager
        implements X509KeyManager {

    private static final Debug debug = Debug.getInstance("ssl");

    private final static boolean useDebug =
                            (debug != null) && Debug.isOn("keymanager");

    // for unit testing only, set via privileged reflection
    private static Date verificationDate;

    // list of the builders
    private final List<Builder> builders;

    // counter to generate unique ids for the aliases
    private final AtomicLong uidCounter;

    // cached entries
    private final Map<String,Reference<PrivateKeyEntry>> entryCacheMap;

    X509KeyManagerImpl(Builder builder) {
        this(Collections.singletonList(builder));
    }

    X509KeyManagerImpl(List<Builder> builders) {
        this.builders = builders;
        uidCounter = new AtomicLong();
        entryCacheMap = Collections.synchronizedMap
                        (new SizedMap<String,Reference<PrivateKeyEntry>>());
    }

    // LinkedHashMap with a max size of 10
    // see LinkedHashMap JavaDocs
    private static class SizedMap<K,V> extends LinkedHashMap<K,V> {
        @Override protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
            return size() > 10;
        }
    }

    //
    // public methods
    //

    public X509Certificate[] getCertificateChain(String alias) {
        PrivateKeyEntry entry = getEntry(alias);
        return entry == null ? null :
                (X509Certificate[])entry.getCertificateChain();
    }

    public PrivateKey getPrivateKey(String alias) {
        PrivateKeyEntry entry = getEntry(alias);
        return entry == null ? null : entry.getPrivateKey();
    }

    public String chooseClientAlias(String[] keyTypes, Principal[] issuers,
            Socket socket) {
        return chooseAlias(getKeyTypes(keyTypes), issuers, CheckType.CLIENT);
    }

    public String chooseEngineClientAlias(String[] keyTypes,
            Principal[] issuers, SSLEngine engine) {
        return chooseAlias(getKeyTypes(keyTypes), issuers, CheckType.CLIENT);
    }

    public String chooseServerAlias(String keyType,
            Principal[] issuers, Socket socket) {
        return chooseAlias(getKeyTypes(keyType), issuers, CheckType.SERVER);
    }

    public String chooseEngineServerAlias(String keyType,
            Principal[] issuers, SSLEngine engine) {
        return chooseAlias(getKeyTypes(keyType), issuers, CheckType.SERVER);
    }

    public String[] getClientAliases(String keyType, Principal[] issuers) {
        return getAliases(keyType, issuers, CheckType.CLIENT);
    }

    public String[] getServerAliases(String keyType, Principal[] issuers) {
        return getAliases(keyType, issuers, CheckType.SERVER);
    }

    //
    // implementation private methods
    //

    // we construct the alias we return to JSSE as seen in the code below
    // a unique id is included to allow us to reliably cache entries
    // between the calls to getCertificateChain() and getPrivateKey()
    // even if tokens are inserted or removed
    private String makeAlias(EntryStatus entry) {
        return uidCounter.incrementAndGet() + "." + entry.builderIndex + "."
                + entry.alias;
    }

    private PrivateKeyEntry getEntry(String alias) {
        // if the alias is null, return immediately
        if (alias == null) {
            return null;
        }

        // try to get the entry from cache
        Reference<PrivateKeyEntry> ref = entryCacheMap.get(alias);
        PrivateKeyEntry entry = (ref != null) ? ref.get() : null;
        if (entry != null) {
            return entry;
        }

        // parse the alias
        int firstDot = alias.indexOf('.');
        int secondDot = alias.indexOf('.', firstDot + 1);
        if ((firstDot == -1) || (secondDot == firstDot)) {
            // invalid alias
            return null;
        }
        try {
            int builderIndex = Integer.parseInt
                                (alias.substring(firstDot + 1, secondDot));
            String keyStoreAlias = alias.substring(secondDot + 1);
            Builder builder = builders.get(builderIndex);
            KeyStore ks = builder.getKeyStore();
            Entry newEntry = ks.getEntry
                    (keyStoreAlias, builder.getProtectionParameter(alias));
            if (newEntry instanceof PrivateKeyEntry == false) {
                // unexpected type of entry
                return null;
            }
            entry = (PrivateKeyEntry)newEntry;
            entryCacheMap.put(alias, new SoftReference(entry));
            return entry;
        } catch (Exception e) {
            // ignore
            return null;
        }
    }

    // Class to help verify that the public key algorithm (and optionally
    // the signature algorithm) of a certificate matches what we need.
    private static class KeyType {

        final String keyAlgorithm;
        final String sigKeyAlgorithm;

        KeyType(String algorithm) {
            int k = algorithm.indexOf("_");
            if (k == -1) {
                keyAlgorithm = algorithm;
                sigKeyAlgorithm = null;
            } else {
                keyAlgorithm = algorithm.substring(0, k);
                sigKeyAlgorithm = algorithm.substring(k + 1);
            }
        }

        boolean matches(Certificate[] chain) {
            if (!chain[0].getPublicKey().getAlgorithm().equals(keyAlgorithm)) {
                return false;
            }
            if (sigKeyAlgorithm == null) {
                return true;
            }
            if (chain.length > 1) {
                // if possible, check the public key in the issuer cert
                return sigKeyAlgorithm.equals(chain[1].getPublicKey().getAlgorithm());
            } else {
                // Check the signature algorithm of the certificate itself.
                // Look for the "withRSA" in "SHA1withRSA", etc.
                X509Certificate issuer = (X509Certificate)chain[0];
                String sigAlgName = issuer.getSigAlgName().toUpperCase(ENGLISH);
                String pattern = "WITH" + sigKeyAlgorithm.toUpperCase(ENGLISH);
                return sigAlgName.contains(pattern);
            }
        }
    }

    private static List<KeyType> getKeyTypes(String ... keyTypes) {
        if ((keyTypes == null) || (keyTypes.length == 0) || (keyTypes[0] == null)) {
            return null;
        }
        List<KeyType> list = new ArrayList<KeyType>(keyTypes.length);
        for (String keyType : keyTypes) {
            list.add(new KeyType(keyType));
        }
        return list;
    }

    /*
     * Return the best alias that fits the given parameters.
     * The algorithm we use is:
     *   . scan through all the aliases in all builders in order
     *   . as soon as we find a perfect match, return
     *     (i.e. a match with a cert that has appropriate key usage
     *      and is not expired).
     *   . if we do not find a perfect match, keep looping and remember
     *     the imperfect matches
     *   . at the end, sort the imperfect matches. we prefer expired certs
     *     with appropriate key usage to certs with the wrong key usage.
     *     return the first one of them.
     */
    private String chooseAlias(List<KeyType> keyTypeList,
            Principal[] issuers, CheckType checkType) {
        if (keyTypeList == null || keyTypeList.size() == 0) {
            return null;
        }

        Set<Principal> issuerSet = getIssuerSet(issuers);
        List<EntryStatus> allResults = null;
        for (int i = 0, n = builders.size(); i < n; i++) {
            try {
                List<EntryStatus> results =
                    getAliases(i, keyTypeList, issuerSet, false, checkType);
                if (results != null) {
                    // the results will either be a single perfect match
                    // or 1 or more imperfect matches
                    // if it's a perfect match, return immediately
                    EntryStatus status = results.get(0);
                    if (status.checkResult == CheckResult.OK) {
                        if (useDebug) {
                            debug.println("KeyMgr: choosing key: " + status);
                        }
                        return makeAlias(status);
                    }
                    if (allResults == null) {
                        allResults = new ArrayList<EntryStatus>();
                    }
                    allResults.addAll(results);
                }
            } catch (Exception e) {
                // ignore
            }
        }
        if (allResults == null) {
            if (useDebug) {
                debug.println("KeyMgr: no matching key found");
            }
            return null;
        }
        Collections.sort(allResults);
        if (useDebug) {
            debug.println("KeyMgr: no good matching key found, "
                        + "returning best match out of:");
            debug.println(allResults.toString());
        }
        return makeAlias(allResults.get(0));
    }

    /*
     * Return all aliases that (approximately) fit the parameters.
     * These are perfect matches plus imperfect matches (expired certificates
     * and certificates with the wrong extensions).
     * The perfect matches will be first in the array.
     */
    public String[] getAliases(String keyType, Principal[] issuers,
            CheckType checkType) {
        if (keyType == null) {
            return null;
        }

        Set<Principal> issuerSet = getIssuerSet(issuers);
        List<KeyType> keyTypeList = getKeyTypes(keyType);
        List<EntryStatus> allResults = null;
        for (int i = 0, n = builders.size(); i < n; i++) {
            try {
                List<EntryStatus> results =
                        getAliases(i, keyTypeList, issuerSet, true, checkType);
                if (results != null) {
                    if (allResults == null) {
                        allResults = new ArrayList<EntryStatus>();
                    }
                    allResults.addAll(results);
                }
            } catch (Exception e) {
                // ignore
            }
        }
        if (allResults == null || allResults.size() == 0) {
            if (useDebug) {
                debug.println("KeyMgr: no matching alias found");
            }
            return null;
        }
        Collections.sort(allResults);
        if (useDebug) {
            debug.println("KeyMgr: getting aliases: " + allResults);
        }
        return toAliases(allResults);
    }

    // turn candidate entries into unique aliases we can return to JSSE
    private String[] toAliases(List<EntryStatus> results) {
        String[] s = new String[results.size()];
        int i = 0;
        for (EntryStatus result : results) {
            s[i++] = makeAlias(result);
        }
        return s;
    }

    // make a Set out of the array
    private Set<Principal> getIssuerSet(Principal[] issuers) {
        if ((issuers != null) && (issuers.length != 0)) {
            return new HashSet<Principal>(Arrays.asList(issuers));
        } else {
            return null;
        }
    }

    // a candidate match
    // identifies the entry by builder and alias
    // and includes the result of the certificate check
    private static class EntryStatus implements Comparable<EntryStatus> {

        final int builderIndex;
        final int keyIndex;
        final String alias;
        final CheckResult checkResult;

        EntryStatus(int builderIndex, int keyIndex, String alias,
                Certificate[] chain, CheckResult checkResult) {
            this.builderIndex = builderIndex;
            this.keyIndex = keyIndex;
            this.alias = alias;
            this.checkResult = checkResult;
        }

        public int compareTo(EntryStatus other) {
            int result = this.checkResult.compareTo(other.checkResult);
            return (result == 0) ? (this.keyIndex - other.keyIndex) : result;
        }

        public String toString() {
            String s = alias + " (verified: " + checkResult + ")";
            if (builderIndex == 0) {
                return s;
            } else {
                return "Builder #" + builderIndex + ", alias: " + s;
            }
        }
    }

    // enum for the type of certificate check we want to perform
    // (client or server)
    // also includes the check code itself
    private static enum CheckType {

        // enum constant for "no check" (currently not used)
        NONE(Collections.<String>emptySet()),

        // enum constant for "tls client" check
        // valid EKU for TLS client: any, tls_client
        CLIENT(new HashSet<String>(Arrays.asList(new String[] {
            "2.5.29.37.0", "1.3.6.1.5.5.7.3.2" }))),

        // enum constant for "tls server" check
        // valid EKU for TLS server: any, tls_server, ns_sgc, ms_sgc
        SERVER(new HashSet<String>(Arrays.asList(new String[] {
            "2.5.29.37.0", "1.3.6.1.5.5.7.3.1", "2.16.840.1.113730.4.1",
            "1.3.6.1.4.1.311.10.3.3" })));

        // set of valid EKU values for this type
        final Set<String> validEku;

        CheckType(Set<String> validEku) {
            this.validEku = validEku;
        }

        private static boolean getBit(boolean[] keyUsage, int bit) {
            return (bit < keyUsage.length) && keyUsage[bit];
        }

        // check if this certificate is appropriate for this type of use
        // first check extensions, if they match, check expiration
        // note: we may want to move this code into the sun.security.validator
        // package
        CheckResult check(X509Certificate cert, Date date) {
            if (this == NONE) {
                return CheckResult.OK;
            }

            // check extensions
            try {
                // check extended key usage
                List<String> certEku = cert.getExtendedKeyUsage();
                if ((certEku != null) && Collections.disjoint(validEku, certEku)) {
                    // if extension present and it does not contain any of
                    // the valid EKU OIDs, return extension_mismatch
                    return CheckResult.EXTENSION_MISMATCH;
                }

                // check key usage
                boolean[] ku = cert.getKeyUsage();
                if (ku != null) {
                    String algorithm = cert.getPublicKey().getAlgorithm();
                    boolean kuSignature = getBit(ku, 0);
                    if (algorithm.equals("RSA")) {
                        // require either signature bit
                        // or if server also allow key encipherment bit
                        if (kuSignature == false) {
                            if ((this == CLIENT) || (getBit(ku, 2) == false)) {
                                return CheckResult.EXTENSION_MISMATCH;
                            }
                        }
                    } else if (algorithm.equals("DSA")) {
                        // require signature bit
                        if (kuSignature == false) {
                            return CheckResult.EXTENSION_MISMATCH;
                        }
                    } else if (algorithm.equals("DH")) {
                        // require keyagreement bit
                        if (getBit(ku, 4) == false) {
                            return CheckResult.EXTENSION_MISMATCH;
                        }
                    } else if (algorithm.equals("EC")) {
                        // require signature bit
                        if (kuSignature == false) {
                            return CheckResult.EXTENSION_MISMATCH;
                        }
                        // For servers, also require key agreement.
                        // This is not totally accurate as the keyAgreement bit
                        // is only necessary for static ECDH key exchange and
                        // not ephemeral ECDH. We leave it in for now until
                        // there are signs that this check causes problems
                        // for real world EC certificates.
                        if ((this == SERVER) && (getBit(ku, 4) == false)) {
                            return CheckResult.EXTENSION_MISMATCH;
                        }
                    }
                }
            } catch (CertificateException e) {
                // extensions unparseable, return failure
                return CheckResult.EXTENSION_MISMATCH;
            }

            try {
                cert.checkValidity(date);
                return CheckResult.OK;
            } catch (CertificateException e) {
                return CheckResult.EXPIRED;
            }
        }
    }

    // enum for the result of the extension check
    // NOTE: the order of the constants is important as they are used
    // for sorting, i.e. OK is best, followed by EXPIRED and EXTENSION_MISMATCH
    private static enum CheckResult {
        OK,                     // ok or not checked
        EXPIRED,                // extensions valid but cert expired
        EXTENSION_MISMATCH,     // extensions invalid (expiration not checked)
    }

    /*
     * Return a List of all candidate matches in the specified builder
     * that fit the parameters.
     * We exclude entries in the KeyStore if they are not:
     *  . private key entries
     *  . the certificates are not X509 certificates
     *  . the algorithm of the key in the EE cert doesn't match one of keyTypes
     *  . none of the certs is issued by a Principal in issuerSet
     * Using those entries would not be possible or they would almost
     * certainly be rejected by the peer.
     *
     * In addition to those checks, we also check the extensions in the EE
     * cert and its expiration. Even if there is a mismatch, we include
     * such certificates because they technically work and might be accepted
     * by the peer. This leads to more graceful failure and better error
     * messages if the cert expires from one day to the next.
     *
     * The return values are:
     *   . null, if there are no matching entries at all
     *   . if 'findAll' is 'false' and there is a perfect match, a List
     *     with a single element (early return)
     *   . if 'findAll' is 'false' and there is NO perfect match, a List
     *     with all the imperfect matches (expired, wrong extensions)
     *   . if 'findAll' is 'true', a List with all perfect and imperfect
     *     matches
     */
    private List<EntryStatus> getAliases(int builderIndex,
            List<KeyType> keyTypes, Set<Principal> issuerSet,
            boolean findAll, CheckType checkType) throws Exception {
        Builder builder = builders.get(builderIndex);
        KeyStore ks = builder.getKeyStore();
        List<EntryStatus> results = null;
        Date date = verificationDate;
        boolean preferred = false;
        for (Enumeration<String> e = ks.aliases(); e.hasMoreElements(); ) {
            String alias = e.nextElement();
            // check if it is a key entry (private key or secret key)
            if (ks.isKeyEntry(alias) == false) {
                continue;
            }

            Certificate[] chain = ks.getCertificateChain(alias);
            if ((chain == null) || (chain.length == 0)) {
                // must be secret key entry, ignore
                continue;
            }
            // check keytype
            int keyIndex = -1;
            int j = 0;
            for (KeyType keyType : keyTypes) {
                if (keyType.matches(chain)) {
                    keyIndex = j;
                    break;
                }
                j++;
            }
            if (keyIndex == -1) {
                if (useDebug) {
                    debug.println("Ignoring alias " + alias
                                + ": key algorithm does not match");
                }
                continue;
            }
            // check issuers
            if (issuerSet != null) {
                boolean found = false;
                for (Certificate cert : chain) {
                    if (cert instanceof X509Certificate == false) {
                        // not an X509Certificate, ignore this entry
                        break;
                    }
                    X509Certificate xcert = (X509Certificate)cert;
                    if (issuerSet.contains(xcert.getIssuerX500Principal())) {
                        found = true;
                        break;
                    }
                }
                if (found == false) {
                    if (useDebug) {
                        debug.println("Ignoring alias " + alias
                                    + ": issuers do not match");
                    }
                    continue;
                }
            }
            if (date == null) {
                date = new Date();
            }
            CheckResult checkResult =
                    checkType.check((X509Certificate)chain[0], date);
            EntryStatus status =
                    new EntryStatus(builderIndex, keyIndex,
                                        alias, chain, checkResult);
            if (!preferred && checkResult == CheckResult.OK && keyIndex == 0) {
                preferred = true;
            }
            if (preferred && (findAll == false)) {
                // if we have a good match and do not need all matches,
                // return immediately
                return Collections.singletonList(status);
            } else {
                if (results == null) {
                    results = new ArrayList<EntryStatus>();
                }
                results.add(status);
            }
        }
        return results;
    }

}
