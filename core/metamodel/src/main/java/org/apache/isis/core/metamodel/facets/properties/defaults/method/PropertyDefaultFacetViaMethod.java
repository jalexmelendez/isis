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
package org.apache.isis.core.metamodel.facets.properties.defaults.method;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;

import org.apache.isis.applib.exceptions.unrecoverable.UnknownTypeException;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.properties.defaults.PropertyDefaultFacetAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public class PropertyDefaultFacetViaMethod
extends PropertyDefaultFacetAbstract
implements ImperativeFacet {

    @Getter(onMethod_ = {@Override}) private final @NonNull Can<Method> methods;

    public PropertyDefaultFacetViaMethod(
            final Method method,
            final FacetHolder holder) {
        super(holder);
        this.methods = ImperativeFacet.singleMethod(method);
    }

    @Override
    public Intent getIntent(final Method method) {
        return Intent.DEFAULTS;
    }

    @Override
    public ManagedObject getDefault(final ManagedObject owningAdapter) {
        val method = methods.getFirstOrFail();
        final Object result = ManagedObjects.InvokeUtil.invoke(method, owningAdapter);
        if (result == null) {
            return null;
        }
        return createAdapter(method.getReturnType(), result);
    }

    private ManagedObject createAdapter(final Class<?> type, final Object object) {
        val specification = getSpecificationLoader().loadSpecification(type);
        if (specification.isNotCollection()) {
            return getObjectManager().adapt(object);
        } else {
            throw new UnknownTypeException("not an object, is this a collection?");
        }
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        ImperativeFacet.visitAttributes(this, visitor);
    }

}
