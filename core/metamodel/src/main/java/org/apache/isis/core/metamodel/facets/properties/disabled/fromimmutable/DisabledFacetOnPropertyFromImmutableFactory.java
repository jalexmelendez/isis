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
package org.apache.isis.core.metamodel.facets.properties.disabled.fromimmutable;

import javax.inject.Inject;

import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;

import lombok.val;

public class DisabledFacetOnPropertyFromImmutableFactory
extends FacetFactoryAbstract {

    @Inject
    public DisabledFacetOnPropertyFromImmutableFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        val declaringClass = processMethodContext.getMethod().getDeclaringClass();
        val spec = getSpecificationLoader().loadSpecification(declaringClass);

        spec.lookupNonFallbackFacet(ImmutableFacet.class)
        .ifPresent(immutableFacet->{
            val facetHolder = processMethodContext.getFacetHolder();

            val semantics = facetHolder.lookupNonFallbackFacet(DisabledFacet.class)
            .map(DisabledFacet::getSemantics)
            .orElse(DisabledFacet.Semantics.ENABLED);

            if(semantics.isEnabled()) {
                return;
            }
            addFacet(
                    DisabledFacetOnPropertyFromImmutable
                    .forImmutable(facetHolder, immutableFacet));
        });
    }

}
