/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.viewer.restfulobjects.viewer.resources;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.JsonValueEncoder;
import org.apache.isis.viewer.restfulobjects.rendering.util.Util;

import lombok.val;

/**
 * Utility class that encapsulates the logic for parsing some content (JSON, or a simple string that is JSON)
 * into an{@link ManagedObject} of a specified
 * {@link ObjectSpecification type}.
 */
public class JsonParserHelper {

    private static final Pattern OBJECT_OID = Pattern.compile(".*objects\\/([^/]+)\\/(.+)");

    private final IResourceContext resourceContext;
    private final ObjectSpecification objectSpec;
    private final JsonValueEncoder jsonValueEncoder;

    public JsonParserHelper(IResourceContext resourceContext, ObjectSpecification objectSpecification) {
        this.objectSpec = objectSpecification;
        this.resourceContext = resourceContext;
        this.jsonValueEncoder = resourceContext.getMetaModelContext().getServiceRegistry()
                .lookupServiceElseFail(JsonValueEncoder.class);
    }


    /**
     * @param bodyAsString
     *            - as per {@link org.apache.isis.viewer.restfulobjects.rendering.util.Util#asStringUtf8(java.io.InputStream)}
     * @return
     */
    ManagedObject parseAsMapWithSingleValue(final String bodyAsString) {
        final JsonRepresentation arguments = Util.readAsMap(bodyAsString);
        return parseAsMapWithSingleValue(arguments);
    }

    ManagedObject parseAsMapWithSingleValue(final JsonRepresentation arguments) {
        final JsonRepresentation representation = arguments.getRepresentation("value");
        if (arguments.size() != 1 || representation == null) {
            throw RestfulObjectsApplicationException.createWithMessage(RestfulResponse.HttpStatusCode.BAD_REQUEST, "Body should be a map with a single key 'value' whose value represents an instance of type '%s'", resourceFor(objectSpec));
        }

        return objectAdapterFor(arguments);
    }

    /**
     *
     * @param argRepr
     *            - expected to be either a String or a Map (ie from within a
     *            List, built by parsing a JSON structure).
     */
    ManagedObject objectAdapterFor(final JsonRepresentation argRepr) {

        if (argRepr == null) {
            return null;
        }

        if(!argRepr.mapHas("value")) {
            String reason = "No 'value' key";
            argRepr.mapPut("invalidReason", reason);
            throw new IllegalArgumentException(reason);
        }

        if (objectSpec == null) {
            String reason = "ObjectSpec is null, cannot validate";
            argRepr.mapPut("invalidReason", reason);
            throw new IllegalArgumentException(reason);
        }

        final JsonRepresentation argValueRepr = argRepr.getRepresentation("value");

        // value (encodable)
        if (objectSpec.isEncodeable()) {
            try {
                return jsonValueEncoder.asAdapter(objectSpec, argValueRepr, null);
            }catch(IllegalArgumentException ex) {
                argRepr.mapPut("invalidReason", ex.getMessage());
                throw ex;
            }catch(Exception ex) {
                StringBuilder buf = new StringBuilder("Failed to parse representation ");
                try {
                    final String reprStr = argRepr.getString("value");
                    buf.append("'").append(reprStr).append("' ");
                } catch(Exception ex2) {
                }
                buf.append("as value of type '").append(objectSpec.getShortIdentifier()).append("'");
                String reason = buf.toString();
                argRepr.mapPut("invalidReason", reason);
                throw new IllegalArgumentException(reason);
            }
        }

        // reference
        if (!argValueRepr.isLink()) {
            final String reason = "Expected a link (because this object's type is not a value) but found no 'href'";
            argRepr.mapPut("invalidReason", reason);
            throw new IllegalArgumentException(reason);
        }
        final String oidFromHref = encodedOidFromLink(argValueRepr);
        if (oidFromHref == null) {
            final String reason = "Could not parse 'href' to identify the object's OID";
            argRepr.mapPut("invalidReason", reason);
            throw new IllegalArgumentException(reason);
        }

        val objectAdapter = resourceContext.getObjectAdapterForOidFromHref(oidFromHref)
                .orElseThrow(()->{
                    val reason = "'href' does not reference a known entity";
                    argRepr.mapPut("invalidReason", reason);
                    return new IllegalArgumentException(reason);
                });
        return objectAdapter;
    }

    static String encodedOidFromLink(final JsonRepresentation link) {
        final String href = link.getString("href");

        final Matcher matcher = OBJECT_OID.matcher(href);
        if (!matcher.matches()) {
            return null;
        }
        String domainType = matcher.group(1);
        String instanceId = matcher.group(2);

        return Bookmark.forLogicalTypeNameAndIdentifier(domainType, instanceId).stringify();
    }

    private static String resourceFor(final ObjectSpecification objectSpec) {
        // TODO: should return a string in the form
        // http://localhost:8080/types/xxx
        return objectSpec.getFullIdentifier();
    }


}
