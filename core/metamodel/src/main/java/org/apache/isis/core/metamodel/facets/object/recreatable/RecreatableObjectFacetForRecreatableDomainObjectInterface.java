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
package org.apache.isis.core.metamodel.facets.object.recreatable;

import org.apache.isis.applib.RecreatableDomainObject;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.HasPostConstructMethodCache;

public class RecreatableObjectFacetForRecreatableDomainObjectInterface
extends RecreatableObjectFacetAbstract {

    public RecreatableObjectFacetForRecreatableDomainObjectInterface(
            final FacetHolder holder,
            final HasPostConstructMethodCache postConstructMethodCache) {
        super(holder, RecreationMechanism.INITIALIZES, postConstructMethodCache);
    }

    @Override
    protected void doInitialize(final Object pojo, final String memento) {
        final RecreatableDomainObject viewModel = (RecreatableDomainObject)pojo;
        viewModel.__isis_recreate(memento);
    }

    @Override
    public String memento(final Object pojo) {
        final RecreatableDomainObject recreatableDomainObject = (RecreatableDomainObject)pojo;
        return recreatableDomainObject.__isis_memento();
    }

}
