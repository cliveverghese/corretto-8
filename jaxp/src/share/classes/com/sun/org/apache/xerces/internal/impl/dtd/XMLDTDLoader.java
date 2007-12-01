/*
 * reserved comment block
 * DO NOT REMOVE OR ALTER!
 */
/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xerces" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package com.sun.org.apache.xerces.internal.impl.dtd;

import com.sun.org.apache.xerces.internal.impl.Constants;
import com.sun.org.apache.xerces.internal.impl.XMLDTDScannerImpl;
import com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import com.sun.org.apache.xerces.internal.impl.msg.XMLMessageFormatter;

import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.util.DefaultErrorHandler;

import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarLoader;
import com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler;
import com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;

import java.util.Locale;
import java.io.IOException;
import java.io.EOFException;

/**
 * The DTD loader. The loader knows how to build grammars from XMLInputSources.
 * It extends the DTD processor in order to do this; it's
 * a separate class because DTD processors don't need to know how
 * to talk to the outside world in their role as instance-document
 * helpers.
 * <p>
 * This component requires the following features and properties.  It
 * know ho to set them if no one else does:from the
 * <ul>
 *  <li>http://xml.org/sax/features/namespaces</li>
 *  <li>http://apache.org/xml/properties/internal/symbol-table</li>
 *  <li>http://apache.org/xml/properties/internal/error-reporter</li>
 *  <li>http://apache.org/xml/properties/internal/grammar-pool</li>
 *  <li>http://apache.org/xml/properties/internal/datatype-validator-factory</li>
 * </ul>
 *
 * @xerces.internal
 *
 * @author Neil Graham, IBM
 *
 */
public class XMLDTDLoader
        extends XMLDTDProcessor
        implements XMLGrammarLoader {

    //
    // Constants
    //

    // feature identifiers

    /** Feature identifier: standard uri conformant feature. */
    protected static final String STANDARD_URI_CONFORMANT_FEATURE =
        Constants.XERCES_FEATURE_PREFIX + Constants.STANDARD_URI_CONFORMANT_FEATURE;

    // recognized features:
    private static final String[] RECOGNIZED_FEATURES = {
        VALIDATION,
        WARN_ON_DUPLICATE_ATTDEF,
        NOTIFY_CHAR_REFS,
        STANDARD_URI_CONFORMANT_FEATURE
    };

    // property identifiers

    /** Property identifier: error handler. */
    protected static final String ERROR_HANDLER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_HANDLER_PROPERTY;

    /** Property identifier: entity resolver. */
    public static final String ENTITY_RESOLVER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_RESOLVER_PROPERTY;

    /** Recognized properties. */
    private static final String[] LOADER_RECOGNIZED_PROPERTIES = {
        SYMBOL_TABLE,
        ERROR_REPORTER,
        ERROR_HANDLER,
        ENTITY_RESOLVER,
        GRAMMAR_POOL,
        DTD_VALIDATOR,
    };

    // enforcing strict uri?
    private boolean fStrictURI = false;

    /** Entity resolver . */
    protected XMLEntityResolver fEntityResolver;

    // the scanner we use to actually read the DTD
    protected XMLDTDScannerImpl fDTDScanner;

    // the entity manager the scanner needs.
    protected XMLEntityManager fEntityManager;

    // what's our Locale?
    protected Locale fLocale;

    //
    // Constructors
    //

    /** Deny default construction; we need a SymtolTable! */
    public XMLDTDLoader() {
        this(new SymbolTable());
    } // <init>()

    public XMLDTDLoader(SymbolTable symbolTable) {
        this(symbolTable, null);
    } // init(SymbolTable)

    public XMLDTDLoader(SymbolTable symbolTable,
                XMLGrammarPool grammarPool) {
        this(symbolTable, grammarPool, null, new XMLEntityManager());
    } // init(SymbolTable, XMLGrammarPool)

    XMLDTDLoader(SymbolTable symbolTable,
                XMLGrammarPool grammarPool, XMLErrorReporter errorReporter,
                XMLEntityResolver entityResolver) {
        fSymbolTable = symbolTable;
        fGrammarPool = grammarPool;
        if(errorReporter == null) {
            errorReporter = new XMLErrorReporter();
            errorReporter.setProperty(ERROR_HANDLER, new DefaultErrorHandler());
        }
        fErrorReporter = errorReporter;
        // Add XML message formatter if there isn't one.
        if (fErrorReporter.getMessageFormatter(XMLMessageFormatter.XML_DOMAIN) == null) {
            XMLMessageFormatter xmft = new XMLMessageFormatter();
            fErrorReporter.putMessageFormatter(XMLMessageFormatter.XML_DOMAIN, xmft);
            fErrorReporter.putMessageFormatter(XMLMessageFormatter.XMLNS_DOMAIN, xmft);
        }
        fEntityResolver = entityResolver;
        if(fEntityResolver instanceof XMLEntityManager) {
            fEntityManager = (XMLEntityManager)fEntityResolver;
        } else {
            fEntityManager = new XMLEntityManager();
        }
        fEntityManager.setProperty(Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY, errorReporter);
        fDTDScanner = new XMLDTDScannerImpl(fSymbolTable, fErrorReporter, fEntityManager);
        fDTDScanner.setDTDHandler(this);
        fDTDScanner.setDTDContentModelHandler(this);
        reset();
    } // init(SymbolTable, XMLGrammarPool, XMLErrorReporter, XMLEntityResolver)

    // XMLGrammarLoader methods

    /**
     * Sets the state of a feature. This method is called by the component
     * manager any time after reset when a feature changes state.
     * <p>
     * <strong>Note:</strong> Components should silently ignore features
     * that do not affect the operation of the component.
     *
     * @param featureId The feature identifier.
     * @param state     The state of the feature.
     *
     * @throws SAXNotRecognizedException The component should not throw
     *                                   this exception.
     * @throws SAXNotSupportedException The component should not throw
     *                                  this exception.
     */
    public void setFeature(String featureId, boolean state)
            throws XMLConfigurationException {
        if(featureId.equals(VALIDATION)) {
            fValidation = state;
        } else if(featureId.equals(WARN_ON_DUPLICATE_ATTDEF)) {
            fWarnDuplicateAttdef = state;
        } else if(featureId.equals(NOTIFY_CHAR_REFS)) {
            fDTDScanner.setFeature(featureId, state);
        } else if(featureId.equals(STANDARD_URI_CONFORMANT_FEATURE)) {
            fStrictURI = state;
        }  else {
            throw new XMLConfigurationException(XMLConfigurationException.NOT_RECOGNIZED, featureId);
        }
    } // setFeature(String,boolean)

    /**
     * Returns a list of property identifiers that are recognized by
     * this component. This method may return null if no properties
     * are recognized by this component.
     */
    public String[] getRecognizedProperties() {
        return (String[])(LOADER_RECOGNIZED_PROPERTIES.clone());
    } // getRecognizedProperties():String[]

    /**
     * Returns the state of a property.
     *
     * @param propertyId The property identifier.
     *
     * @throws XMLConfigurationException Thrown on configuration error.
     */
    public Object getProperty(String propertyId)
            throws XMLConfigurationException {
        if(propertyId.equals( SYMBOL_TABLE)) {
            return fSymbolTable;
        } else if(propertyId.equals( ERROR_REPORTER)) {
            return fErrorReporter;
        } else if(propertyId.equals( ERROR_HANDLER)) {
            return fErrorReporter.getErrorHandler();
        } else if(propertyId.equals( ENTITY_RESOLVER)) {
            return fEntityResolver;
        } else if(propertyId.equals( GRAMMAR_POOL)) {
            return fGrammarPool;
        } else if(propertyId.equals( DTD_VALIDATOR)) {
            return fValidator;
        }
        throw new XMLConfigurationException(XMLConfigurationException.NOT_RECOGNIZED, propertyId);
    } // getProperty(String):  Object

    /**
     * Sets the value of a property. This method is called by the component
     * manager any time after reset when a property changes value.
     * <p>
     * <strong>Note:</strong> Components should silently ignore properties
     * that do not affect the operation of the component.
     *
     * @param propertyId The property identifier.
     * @param value      The value of the property.
     *
     * @throws SAXNotRecognizedException The component should not throw
     *                                   this exception.
     * @throws SAXNotSupportedException The component should not throw
     *                                  this exception.
     */
    public void setProperty(String propertyId, Object value)
            throws XMLConfigurationException {
        if(propertyId.equals( SYMBOL_TABLE)) {
            fSymbolTable = (SymbolTable)value;
            fDTDScanner.setProperty(propertyId, value);
            fEntityManager.setProperty(propertyId, value);
        } else if(propertyId.equals( ERROR_REPORTER)) {
            fErrorReporter = (XMLErrorReporter)value;
            // Add XML message formatter if there isn't one.
            if (fErrorReporter.getMessageFormatter(XMLMessageFormatter.XML_DOMAIN) == null) {
                XMLMessageFormatter xmft = new XMLMessageFormatter();
                fErrorReporter.putMessageFormatter(XMLMessageFormatter.XML_DOMAIN, xmft);
                fErrorReporter.putMessageFormatter(XMLMessageFormatter.XMLNS_DOMAIN, xmft);
            }
            fDTDScanner.setProperty(propertyId, value);
            fEntityManager.setProperty(propertyId, value);
        } else if(propertyId.equals( ERROR_HANDLER)) {
            fErrorReporter.setProperty(propertyId, value);
        } else if(propertyId.equals( ENTITY_RESOLVER)) {
            fEntityResolver = (XMLEntityResolver)value;
        } else if(propertyId.equals( GRAMMAR_POOL)) {
            fGrammarPool = (XMLGrammarPool)value;
        } else {
            throw new XMLConfigurationException(XMLConfigurationException.NOT_RECOGNIZED, propertyId);
        }
    } // setProperty(String,Object)

    /**
     * Returns the state of a feature.
     *
     * @param featureId The feature identifier.
     *
     * @throws XMLConfigurationException Thrown on configuration error.
     */
    public boolean getFeature(String featureId)
            throws XMLConfigurationException {
        if(featureId.equals( VALIDATION)) {
            return fValidation;
        } else if(featureId.equals( WARN_ON_DUPLICATE_ATTDEF)) {
            return fWarnDuplicateAttdef;
        } else if(featureId.equals( NOTIFY_CHAR_REFS)) {
            return fDTDScanner.getFeature(featureId);
        }
        throw new XMLConfigurationException(XMLConfigurationException.NOT_RECOGNIZED, featureId);
    } //getFeature(String):  boolean

    /**
     * Set the locale to use for messages.
     *
     * @param locale The locale object to use for localization of messages.
     *
     * @exception XNIException Thrown if the parser does not support the
     *                         specified locale.
     */
    public void setLocale(Locale locale) {
        fLocale = locale;
    } // setLocale(Locale)

    /** Return the Locale the XMLGrammarLoader is using. */
    public Locale getLocale() {
        return fLocale;
    } // getLocale():  Locale


    /**
     * Sets the error handler.
     *
     * @param errorHandler The error handler.
     */
    public void setErrorHandler(XMLErrorHandler errorHandler) {
        fErrorReporter.setProperty(ERROR_HANDLER, errorHandler);
    } // setErrorHandler(XMLErrorHandler)

    /** Returns the registered error handler.  */
    public XMLErrorHandler getErrorHandler() {
        return fErrorReporter.getErrorHandler();
    } // getErrorHandler():  XMLErrorHandler

    /**
     * Sets the entity resolver.
     *
     * @param entityResolver The new entity resolver.
     */
    public void setEntityResolver(XMLEntityResolver entityResolver) {
        fEntityResolver = entityResolver;
    } // setEntityResolver(XMLEntityResolver)

    /** Returns the registered entity resolver.  */
    public XMLEntityResolver getEntityResolver() {
        return fEntityResolver;
    } // getEntityResolver():  XMLEntityResolver

    /**
     * Returns a Grammar object by parsing the contents of the
     * entity pointed to by source.
     *
     * @param source        the location of the entity which forms
     *                          the starting point of the grammar to be constructed.
     * @throws IOException      When a problem is encountered reading the entity
     *          XNIException    When a condition arises (such as a FatalError) that requires parsing
     *                              of the entity be terminated.
     */
    public Grammar loadGrammar(XMLInputSource source)
            throws IOException, XNIException {
        reset();
        // First chance checking strict URI
        String eid = XMLEntityManager.expandSystemId(source.getSystemId(), source.getBaseSystemId(), fStrictURI);
        fDTDGrammar = new DTDGrammar(fSymbolTable, new XMLDTDDescription(source.getPublicId(), source.getSystemId(), source.getBaseSystemId(), eid, null));
        fGrammarBucket = new DTDGrammarBucket();
        fGrammarBucket.setStandalone(false);
        fGrammarBucket.setActiveGrammar(fDTDGrammar);
        // no reason to use grammar bucket's "put" method--we
        // know which grammar it is, and we don't know the root name anyway...

        // actually start the parsing!
        try {
            fDTDScanner.setInputSource(source);
            fDTDScanner.scanDTDExternalSubset(true);
        } catch (EOFException e) {
            // expected behaviour...
        }
        finally {
            // Close all streams opened by the parser.
            fEntityManager.closeReaders();
        }
        if(fDTDGrammar != null && fGrammarPool != null) {
            fGrammarPool.cacheGrammars(XMLDTDDescription.XML_DTD, new Grammar[] {fDTDGrammar});
        }
        return fDTDGrammar;
    } // loadGrammar(XMLInputSource):  Grammar

    // reset all the components that we rely upon
    protected void reset() {
        super.reset();
        fDTDScanner.reset();
        fEntityManager.reset();
        fErrorReporter.setDocumentLocator(fEntityManager.getEntityScanner());
    }

} // class XMLDTDLoader
