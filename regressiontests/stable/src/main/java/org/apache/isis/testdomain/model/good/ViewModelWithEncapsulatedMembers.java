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
package org.apache.isis.testdomain.model.good;

import java.io.Serializable;

import org.apache.isis.applib.ViewModel;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Encapsulation;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@DomainObject(
        nature = Nature.VIEW_MODEL,
        encapsulation = Encapsulation.ENABLED)
public class ViewModelWithEncapsulatedMembers
implements
    Serializable,
    ViewModel.CloneableViaSerialization {

    private static final long serialVersionUID = 1L;

    // allowed to be private since 2.0.0-M7
    @Action
    private String myAction() {
        return "Hallo World!";
    }

    // allowed to be private since 2.0.0-M7
    @MemberSupport
    private String disableMyAction() {
        return "disabled for testing purposes";
    }

    // -- PROPERTY WITH PRIVATE GETTER AND SETTER

    @Property
    // allowed to be private since 2.0.0-M7
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private String propWithPrivateAccessors = "Foo";

    // -- PROPERTY WITHOUT GETTER OR SETTER

    // TODO should be allowed have no getter/setter since 2.0.0-M7
    // yet unclear whether thats gonna be hard to implement
    @Property
    private String propWithoutAccessors = "foo";


}
