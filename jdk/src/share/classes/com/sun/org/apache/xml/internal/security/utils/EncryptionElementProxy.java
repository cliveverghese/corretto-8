/*
 * reserved comment block
 * DO NOT REMOVE OR ALTER!
 */
/*
 * Copyright  1999-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.sun.org.apache.xml.internal.security.utils;



import com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * This is the base object for all objects which map directly to an Element from
 * the xenc spec.
 *
 * @author $Author: raul $
 */
public abstract class EncryptionElementProxy extends ElementProxy {

   /**
    * Constructor EncryptionElementProxy
    *
    * @param doc
    */
   public EncryptionElementProxy(Document doc) {
      super(doc);
   }

   /**
    * Constructor EncryptionElementProxy
    *
    * @param element
    * @param BaseURI
    * @throws XMLSecurityException
    */
   public EncryptionElementProxy(Element element, String BaseURI)
           throws XMLSecurityException {
      super(element, BaseURI);
   }

   /** @inheritDoc */
   public final String getBaseNamespace() {
      return EncryptionConstants.EncryptionSpecNS;
   }
}
