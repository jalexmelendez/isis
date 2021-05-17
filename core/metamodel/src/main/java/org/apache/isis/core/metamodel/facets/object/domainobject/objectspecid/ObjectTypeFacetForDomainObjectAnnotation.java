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

package org.apache.isis.core.metamodel.facets.object.domainobject.objectspecid;

import java.util.Optional;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.objectspecid.ObjectTypeFacet;
import org.apache.isis.core.metamodel.facets.object.objectspecid.ObjectTypeFacetAbstract;

public class ObjectTypeFacetForDomainObjectAnnotation extends ObjectTypeFacetAbstract {

    public static ObjectTypeFacet create(
            final Optional<DomainObject> domainObjectIfAny,
            final FacetHolder holder) {

        return domainObjectIfAny
                .map(DomainObject::objectType)
                .filter(_Strings::isNotEmpty)
                .map(objectType -> new ObjectTypeFacetForDomainObjectAnnotation(objectType, holder))
                .orElse(null);
    }

    private ObjectTypeFacetForDomainObjectAnnotation(final String value,
            final FacetHolder holder) {
        super(value, holder);
    }
}