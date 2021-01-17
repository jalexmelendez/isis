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
package org.apache.isis.applib.services;

import java.util.UUID;


/**
 * Mix-in interface for objects (usually created by service implementations) that are be persistable,
 * and so can be associated together using a unique identifier.
 *
 */
// tag::refguide[]
public interface HasUniqueId {

    // end::refguide[]
    /**
     * The unique identifier (a GUID) of the request/interaction/transaction.
     */
    // tag::refguide[]
    UUID getUniqueId();

}
// end::refguide[]