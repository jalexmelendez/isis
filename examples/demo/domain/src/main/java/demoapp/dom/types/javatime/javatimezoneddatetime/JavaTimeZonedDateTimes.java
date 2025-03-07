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
package demoapp.dom.types.javatime.javatimezoneddatetime;

import java.time.ZoneOffset;
import java.util.List;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.ObjectSupport;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.SemanticsOf;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.types.javatime.javatimezoneddatetime.persistence.JavaTimeZonedDateTimeEntity;
import demoapp.dom.types.javatime.javatimezoneddatetime.vm.JavaTimeZonedDateTimeVm;

@XmlRootElement(name = "Demo")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(nature=Nature.VIEW_MODEL, logicalTypeName = "demo.JavaTimeZonedDateTimes", editing=Editing.ENABLED)
//@Log4j2
public class JavaTimeZonedDateTimes implements HasAsciiDocDescription {

    @ObjectSupport public String title() {
        return "java.time.ZonedDateTime data type";
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(promptStyle = PromptStyle.DIALOG_MODAL)
    public JavaTimeZonedDateTimeVm openViewModel(final java.time.ZonedDateTime initialValue) {
        return new JavaTimeZonedDateTimeVm(initialValue);
    }
    @MemberSupport public java.time.ZonedDateTime default0OpenViewModel() {
        return java.time.ZonedDateTime.of(120,1,1, 1, 1, 1, 1, ZoneOffset.UTC);
    }

    @Collection
    public List<? extends JavaTimeZonedDateTimeEntity> getEntities() {
        return entities.all();
    }

    @Inject
    @XmlTransient
    ValueHolderRepository<java.time.ZonedDateTime, ? extends JavaTimeZonedDateTimeEntity> entities;


}
