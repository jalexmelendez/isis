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
package org.apache.isis.testing.unittestsupport.applib.jmocking;

import org.jmock.auto.Mock;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.testing.unittestsupport.applib.jmocking.JUnitRuleMockery2.Allowing;
import org.apache.isis.testing.unittestsupport.applib.jmocking.JUnitRuleMockery2.ClassUnderTest;
import org.apache.isis.testing.unittestsupport.applib.jmocking.JUnitRuleMockery2.Mode;

public class JUnitRuleMockery2Test_mockAnnotatedWithAllowing {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Allowing
    @Mock
    private Collaborator collaborator;

    @ClassUnderTest
    private CollaboratingUsingConstructorInjection collaborating;

    // no longer necessary :-)
    //    @Before
    //	public void setUp() throws Exception {
    //    	collaborating = (CollaboratingUsingConstructorInjection) context.getClassUnderTest();
    //	}

    @Test
    public void invocationOnCollaboratorIsIgnored() {
        collaborating.collaborateWithCollaborator();
    }

    @Test
    public void lackOfInvocationOnCollaboratorIsIgnored() {
        collaborating.dontCollaborateWithCollaborator();
    }


}
