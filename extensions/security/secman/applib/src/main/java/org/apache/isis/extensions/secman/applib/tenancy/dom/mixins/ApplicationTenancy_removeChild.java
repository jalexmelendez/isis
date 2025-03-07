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
package org.apache.isis.extensions.secman.applib.tenancy.dom.mixins;

import java.util.Collection;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.extensions.secman.applib.IsisModuleExtSecmanApplib;
import org.apache.isis.extensions.secman.applib.tenancy.dom.ApplicationTenancy;
import org.apache.isis.extensions.secman.applib.tenancy.dom.ApplicationTenancyRepository;
import org.apache.isis.extensions.secman.applib.tenancy.dom.mixins.ApplicationTenancy_removeChild.DomainEvent;

import lombok.RequiredArgsConstructor;

@Action(
        domainEvent = DomainEvent.class,
        semantics = SemanticsOf.IDEMPOTENT
)
@ActionLayout(
        associateWith = "children",
        named = "Remove",
        sequence = "2"
)
@RequiredArgsConstructor
public class ApplicationTenancy_removeChild {

    public static class DomainEvent
            extends IsisModuleExtSecmanApplib.ActionDomainEvent<ApplicationTenancy_removeChild> {}

    @Inject private ApplicationTenancyRepository applicationTenancyRepository;

    private final ApplicationTenancy target;

    @MemberSupport public ApplicationTenancy act(final ApplicationTenancy child) {
        applicationTenancyRepository.clearParentOnTenancy(child);
        return target;
    }

    @MemberSupport public Collection<? extends ApplicationTenancy> choices0Act() {
        return applicationTenancyRepository.getChildren(target);
    }
    @MemberSupport public String disableAct() { return choices0Act().isEmpty()? "No children to remove": null; }

}
