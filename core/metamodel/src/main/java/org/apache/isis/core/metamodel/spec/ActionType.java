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
package org.apache.isis.core.metamodel.spec;

import org.apache.isis.commons.collections.ImmutableEnumSet;

public enum ActionType {
    USER,
    PROTOTYPE;

    public String getName() {
        return name();
    }

    public boolean isPrototype() {
        return this == PROTOTYPE;
    }

    public boolean isUser() {
        return this == USER;
    }

    public static final ImmutableEnumSet<ActionType> USER_ONLY = ImmutableEnumSet.of(ActionType.USER);
    public static final ImmutableEnumSet<ActionType> USER_AND_PROTOTYPE = ImmutableEnumSet.of(ActionType.USER, ActionType.PROTOTYPE);
    public static final ImmutableEnumSet<ActionType> ANY = ImmutableEnumSet.allOf(ActionType.class);
}
