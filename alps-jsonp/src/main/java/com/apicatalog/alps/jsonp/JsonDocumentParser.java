/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.apicatalog.alps.jsonp;

import java.io.InputStream;
import java.io.Reader;
import java.net.URI;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParsingException;
import javax.json.stream.JsonParser.Event;

import com.apicatalog.alps.DocumentParser;
import com.apicatalog.alps.dom.Document;
import com.apicatalog.alps.error.DocumentError;
import com.apicatalog.alps.error.DocumentException;
import com.apicatalog.alps.error.InvalidDocumentException;
import com.apicatalog.alps.error.MalformedDocumentException;

public final class JsonDocumentParser implements DocumentParser {

    @Override
    public boolean canParse(final String mediaType) {
        return mediaType != null
                && ("application/json".equalsIgnoreCase(mediaType)
                    || mediaType.toLowerCase().endsWith("+json")
                    );
    }

    @Override
    public Document parse(final URI baseUri, final String mediaType, final InputStream stream) throws DocumentException {

        if (stream == null) {
            throw new IllegalArgumentException();
        }
        
        if (!canParse(mediaType)) {
            throw new DocumentException("The parser does not support '" + mediaType + "'.");
        }
        
        try {
        
            return parse(baseUri, Json.createParser(stream));
            
        } catch (JsonException e) {
            throw new DocumentException(e);
        }
    }

    @Override
    public Document parse(final URI baseUri, final String mediaType, final Reader reader) throws DocumentException {

        if (reader == null) {
            throw new IllegalArgumentException();
        }
        
        if (!canParse(mediaType)) {
            throw new DocumentException("The parser does not support '" + mediaType + "'.");
        }
        
        try {
        
            return parse(baseUri, Json.createParser(reader));
            
        } catch (JsonException e) {
            throw new DocumentException(e);
        }
    }
    
    private static final Document parse(URI baseUri, JsonParser parser)  throws DocumentException {
        
        try {
        
            if (!parser.hasNext()) {
                throw new DocumentException("Expected JSON object but was an empty input");
            }
                
            final Event event = parser.next();
            
            if (!Event.START_OBJECT.equals(event)) {
                throw new DocumentException("Expected JSON object but was " + event);
            }
            
            final JsonObject rootObject = parser.getObject();
            
            if (!rootObject.containsKey(JsonConstants.ROOT)) {
                throw new InvalidDocumentException(DocumentError.MISSING_ROOT, "Property '" + JsonConstants.ROOT + "' is not present");
            }
            
            final JsonValue alpsObject = rootObject.get(JsonConstants.ROOT);
            
            if (JsonUtils.isNotObject(alpsObject)) {
                throw new InvalidDocumentException(DocumentError.MISSING_ROOT, "Property '" + JsonConstants.ROOT + "' does not contain JSON object");
            }
                
            return JsonDocument.parse(baseUri, alpsObject.asJsonObject());
            
        } catch (JsonParsingException e) {
            throw new MalformedDocumentException(e.getLocation().getLineNumber(), e.getLocation().getColumnNumber(), "Document is not valid JSON document.");
        }
    }
}