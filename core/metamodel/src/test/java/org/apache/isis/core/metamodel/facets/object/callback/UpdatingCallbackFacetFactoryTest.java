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
package org.apache.isis.core.metamodel.facets.object.callback;

import org.apache.isis.core.metamodel.facets.object.callbacks.UpdatingCallbackFacet;
import org.apache.isis.core.metamodel.methods.MethodLiteralConstants.CallbackMethod;

public class UpdatingCallbackFacetFactoryTest
extends CallbackFacetFactoryTestAbstract {

    public void testUpdatingLifecycleMethodPickedUpOn() {
        class Customer {
            @SuppressWarnings("unused")
            public void updating() {
            }
        }
        assertPicksUp(1, facetFactory, Customer.class, CallbackMethod.UPDATING, UpdatingCallbackFacet.class);
    }

}