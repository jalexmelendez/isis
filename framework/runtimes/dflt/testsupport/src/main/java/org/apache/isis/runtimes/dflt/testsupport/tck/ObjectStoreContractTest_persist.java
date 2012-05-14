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

package org.apache.isis.runtimes.dflt.testsupport.tck;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;

import java.util.Date;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.runtime.installerregistry.installerapi.PersistenceMechanismInstaller;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.ObjectStore;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindByTitle;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.testsupport.IsisSystemWithFixtures;
import org.apache.isis.tck.dom.eg.ExamplePojoWithValues;

public abstract class ObjectStoreContractTest_persist {

    @Rule
    public IsisSystemWithFixtures iswf = IsisSystemWithFixtures.builder()
        .with(createPersistenceMechanismInstaller())
        .with(iswfListener()).build();

    
    /**
     * Mandatory hook.
     */
    protected abstract PersistenceMechanismInstaller createPersistenceMechanismInstaller();

    protected IsisSystemWithFixtures.Listener iswfListener() {
        return null;
    }

    /**
     * hook method
     */
    protected void resetPersistenceStore() {
    }
    
    protected ObjectAdapter epv2Adapter;
    protected ObjectSpecification epvSpecification;

    protected ObjectStore getStore() {
        PersistenceSession psos = IsisContext.getPersistenceSession();
        return (ObjectStore) psos.getObjectStore();
    }


    @Before
    public void setUpFixtures() throws Exception {
        epv2Adapter = iswf.adapterFor(iswf.fixtures.epv2);
        epvSpecification = iswf.loadSpecification(ExamplePojoWithValues.class);
    }


    @Test
    public void getInstances_usingFindByTitle() throws Exception {

        // given nothing in DB
        resetPersistenceStore();
        
        // when search for any object
        boolean hasInstances = getStore().hasInstances(epvSpecification);
        
        // then find none
        assertFalse(hasInstances);
        
        // given now persisted
        final ExamplePojoWithValues epv2 = iswf.fixtures.epv2;
        epv2.setName("foo");
        epv2.setDate(new Date());
        epv2.setNullable(1234567890L);
        epv2.setSize(123);
        
        iswf.persist(epv2);

        iswf.bounceSystem();
        
        // when search for object
        ObjectAdapter[] retrievedInstance = getStore().getInstances(new PersistenceQueryFindByTitle(epvSpecification, epv2Adapter.titleString()));
        
        // then find
        assertEquals(1, retrievedInstance.length);
        final ObjectAdapter retrievedAdapter = retrievedInstance[0];

        assertNotSame(epv2Adapter, retrievedAdapter);
        assertEquals(((ExamplePojoWithValues)epv2Adapter.getObject()).getName(), ((ExamplePojoWithValues)retrievedAdapter.getObject()).getName());
        assertEquals(epv2Adapter.getOid(), retrievedAdapter.getOid());

        // and when search for some other title
        retrievedInstance = getStore().getInstances(new PersistenceQueryFindByTitle(epvSpecification, "some other title"));
        
        // then don't find
        assertEquals(0, retrievedInstance.length);
    }


    @Test
    public void updateInstance() throws Exception {

        // given persisted
        resetPersistenceStore();
        ObjectAdapter adapter = iswf.persist(iswf.fixtures.epv2);
        final RootOid oid = (RootOid) adapter.getOid();
        iswf.bounceSystem();
        
        // when change
        adapter = iswf.reload(oid);
        
        ExamplePojoWithValues epv = (ExamplePojoWithValues) adapter.getObject();
        epv.setName("changed");

        iswf.bounceSystem();

        // then found
        ObjectAdapter[] retrievedInstance = getStore().getInstances(new PersistenceQueryFindByTitle(epvSpecification, adapter.titleString()));
        assertEquals(1, retrievedInstance.length);
        
        final ObjectAdapter retrievedAdapter = retrievedInstance[0];
        assertNotSame(adapter, retrievedAdapter);
        assertEquals(((ExamplePojoWithValues)adapter.getObject()).getName(), ((ExamplePojoWithValues)retrievedAdapter.getObject()).getName());
        assertEquals(adapter.getOid(), retrievedAdapter.getOid());
    }

    @Test
    public void removeInstance() throws Exception {

        // given persisted
        resetPersistenceStore();
        ObjectAdapter adapter = iswf.persist(iswf.fixtures.epv2);
        final RootOid oid = (RootOid) adapter.getOid();
        iswf.bounceSystem();

        // when destroy
        adapter = iswf.reload(oid);
        
        ExamplePojoWithValues epv = (ExamplePojoWithValues) adapter.getObject();
        iswf.destroy(epv);
        iswf.bounceSystem();

        // then not found
        assertEquals(false, getStore().hasInstances(epvSpecification));
    }
}
