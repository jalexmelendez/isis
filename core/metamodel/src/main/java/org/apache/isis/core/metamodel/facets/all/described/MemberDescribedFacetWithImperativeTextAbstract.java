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
package org.apache.isis.core.metamodel.facets.all.described;

import java.lang.reflect.Method;

import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.commons.internal.base._Either;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.all.i8n.imperative.HasImperativeText;
import org.apache.isis.core.metamodel.facets.all.i8n.imperative.HasImperativeTextFacetAbstract;
import org.apache.isis.core.metamodel.facets.all.i8n.staatic.HasStaticText;

import lombok.Getter;

/**
 * One of two bases for the {@link MemberDescribedFacet}.
 *
 * @see MemberDescribedFacetWithStaticTextAbstract
 * @since 2.0
 */
public abstract class MemberDescribedFacetWithImperativeTextAbstract
extends HasImperativeTextFacetAbstract
implements MemberDescribedFacet {

    private static final Class<? extends Facet> type() {
        return MemberDescribedFacet.class;
    }

    @Getter(onMethod_ = {@Override})
    private final _Either<HasStaticText, HasImperativeText> specialization = _Either.right(this);

    protected MemberDescribedFacetWithImperativeTextAbstract(
            final Method method,
            final FacetHolder holder) {
        super(type(),
                TranslationContext.forTranslationContextHolder(holder.getFeatureIdentifier()),
                method,
                holder);
    }

}
