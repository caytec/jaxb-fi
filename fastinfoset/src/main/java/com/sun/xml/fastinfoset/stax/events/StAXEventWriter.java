/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004, 2022 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sun.xml.fastinfoset.stax.events;

import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.*;
import com.sun.xml.fastinfoset.CommonResourceBundle;

public class StAXEventWriter implements XMLEventWriter {
    
    private final XMLStreamWriter _streamWriter ;
    /**
     *
     */
    public StAXEventWriter(XMLStreamWriter streamWriter){
        _streamWriter = streamWriter;
    }

    /**
    * Writes any cached events to the underlying output mechanism
     */
    @Override
    public void flush() throws XMLStreamException {
        _streamWriter.flush();
    }
    /**
    * Frees any resources associated with this stream
     */
    @Override
    public void close() throws javax.xml.stream.XMLStreamException {
        _streamWriter.close();
    }
    
    /**
     *
     */
    @Override
    public void add(XMLEventReader eventReader) throws XMLStreamException {
        if(eventReader == null) throw new XMLStreamException(CommonResourceBundle.getInstance().getString("message.nullEventReader"));
        while(eventReader.hasNext()){
            add(eventReader.nextEvent());
        }
    }
    
    /**
    * Add an event to the output stream
    * Adding a START_ELEMENT will open a new namespace scope that 
    * will be closed when the corresponding END_ELEMENT is written.
    *
     */
    @Override
    public void add(XMLEvent event) throws XMLStreamException {
        int type = event.getEventType();
        switch(type){
            case XMLEvent.DTD:{
                DTD dtd = (DTD)event ;
                _streamWriter.writeDTD(dtd.getDocumentTypeDeclaration());
                break;
            }
            case XMLEvent.START_DOCUMENT :{
                StartDocument startDocument = (StartDocument)event ;
                _streamWriter.writeStartDocument(startDocument.getCharacterEncodingScheme(), startDocument.getVersion());
                break;
            }
            case XMLEvent.START_ELEMENT :{
                StartElement startElement = event.asStartElement() ;
                QName qname = startElement.getName();
                _streamWriter.writeStartElement(qname.getPrefix(), qname.getLocalPart(), qname.getNamespaceURI());
                
                Iterator<Namespace> iterator = startElement.getNamespaces();
                while(iterator.hasNext()){
                    Namespace namespace = iterator.next();
                    _streamWriter.writeNamespace(namespace.getPrefix(), namespace.getNamespaceURI());
                }

                Iterator<Attribute> attributes = startElement.getAttributes();
                while(attributes.hasNext()){
                    Attribute attribute = attributes.next();
                    QName name = attribute.getName();
                    _streamWriter.writeAttribute(name.getPrefix(), name.getNamespaceURI(), 
                                                name.getLocalPart(),attribute.getValue());
                }
                break;
            }
            case XMLEvent.NAMESPACE:{
                Namespace namespace = (Namespace)event;
                _streamWriter.writeNamespace(namespace.getPrefix(), namespace.getNamespaceURI());
                break ;
            }
            case XMLEvent.COMMENT: {
                Comment comment = (Comment)event ;
                _streamWriter.writeComment(comment.getText());
                break;
            }
            case XMLEvent.PROCESSING_INSTRUCTION:{
                ProcessingInstruction processingInstruction = (ProcessingInstruction)event ;
                _streamWriter.writeProcessingInstruction(processingInstruction.getTarget(), processingInstruction.getData());
                break;
            }
            case XMLEvent.CHARACTERS:{
                Characters characters = event.asCharacters();
                //check if the CHARACTERS are CDATA
                if(characters.isCData()){
                    _streamWriter.writeCData(characters.getData());
                }
                else{
                    _streamWriter.writeCharacters(characters.getData());
                }
                break;
            }
            case XMLEvent.ENTITY_REFERENCE:{
                EntityReference entityReference = (EntityReference)event ;
                _streamWriter.writeEntityRef(entityReference.getName());
                break;
            }
            case XMLEvent.ATTRIBUTE:{
                Attribute attribute = (Attribute)event;
                QName qname = attribute.getName();
                _streamWriter.writeAttribute(qname.getPrefix(), qname.getNamespaceURI(), qname.getLocalPart(),attribute.getValue());
                break;
            }
            case XMLEvent.CDATA:{
                //there is no separate CDATA datatype but CDATA event can be reported
                //by using vendor specific CDATA property.
                Characters characters = (Characters)event;
                if(characters.isCData()){
                    _streamWriter.writeCData(characters.getData());
                }
                break;
            }
            
            case XMLEvent.END_ELEMENT:{
                _streamWriter.writeEndElement();
                break;
            }
            case XMLEvent.END_DOCUMENT:{
                _streamWriter.writeEndDocument();
                break;
            }
            default:
                throw new XMLStreamException(CommonResourceBundle.getInstance().getString("message.eventTypeNotSupported", new Object[]{Util.getEventTypeString(type)}));
            //throw new XMLStreamException("Unknown Event type = " + type);
        }
        
    }
    
    /**
    * Gets the prefix the uri is bound to
    * @param uri the uri to look up
     */
    @Override
    public String getPrefix(String uri) throws XMLStreamException {
        return _streamWriter.getPrefix(uri);
    }
    
    
    /**
    * Returns the current namespace context.
    * @return the current namespace context
    */
    @Override
    public NamespaceContext getNamespaceContext() {
        return _streamWriter.getNamespaceContext();
    }
    
    
    /**
    * Binds a URI to the default namespace
    * This URI is bound
    * in the scope of the current START_ELEMENT / END_ELEMENT pair.
    * If this method is called before a START_ELEMENT has been written
    * the uri is bound in the root scope.
    * @param uri the uri to bind to the default namespace
     */
    @Override
    public void setDefaultNamespace(String uri) throws XMLStreamException {
        _streamWriter.setDefaultNamespace(uri);
    }
    
    /**
    * Sets the current namespace context for prefix and uri bindings.
    * This context becomes the root namespace context for writing and
    * will replace the current root namespace context.  Subsequent calls
    * to setPrefix and setDefaultNamespace will bind namespaces using
    * the context passed to the method as the root context for resolving
    * namespaces.
    * @param namespaceContext the namespace context to use for this writer
     */
    @Override
    public void setNamespaceContext(NamespaceContext namespaceContext) throws XMLStreamException {
        _streamWriter.setNamespaceContext(namespaceContext);
    }
    /**
    * Sets the prefix the uri is bound to.  This prefix is bound
    * in the scope of the current START_ELEMENT / END_ELEMENT pair.
    * If this method is called before a START_ELEMENT has been written
    * the prefix is bound in the root scope.
    * @param prefix the prefix to bind to the uri
    * @param uri the uri to bind to the prefix
     */
    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        _streamWriter.setPrefix(prefix, uri);
    }
        
}
