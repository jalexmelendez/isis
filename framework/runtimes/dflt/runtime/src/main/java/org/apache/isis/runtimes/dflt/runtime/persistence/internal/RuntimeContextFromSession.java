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

package org.apache.isis.runtimes.dflt.runtime.persistence.internal;

import java.util.List;

import org.apache.isis.applib.ApplicationException;
import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.applib.query.Query;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProviderAbstract;
import org.apache.isis.core.metamodel.adapter.DomainObjectServices;
import org.apache.isis.core.metamodel.adapter.DomainObjectServicesAbstract;
import org.apache.isis.core.metamodel.adapter.LocalizationProviderAbstract;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectDirtier;
import org.apache.isis.core.metamodel.adapter.ObjectDirtierAbstract;
import org.apache.isis.core.metamodel.adapter.ObjectPersistor;
import org.apache.isis.core.metamodel.adapter.ObjectPersistorAbstract;
import org.apache.isis.core.metamodel.adapter.QuerySubmitter;
import org.apache.isis.core.metamodel.adapter.QuerySubmitterAbstract;
import org.apache.isis.core.metamodel.adapter.ServicesProvider;
import org.apache.isis.core.metamodel.adapter.ServicesProviderAbstract;
import org.apache.isis.core.metamodel.adapter.map.AdapterMap;
import org.apache.isis.core.metamodel.adapter.map.AdapterMapAbstract;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacetUtils;
import org.apache.isis.core.metamodel.runtimecontext.DependencyInjector;
import org.apache.isis.core.metamodel.runtimecontext.DependencyInjectorAware;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContextAbstract;
import org.apache.isis.core.metamodel.services.container.query.QueryCardinality;
import org.apache.isis.core.metamodel.spec.ObjectInstantiationException;
import org.apache.isis.core.metamodel.spec.ObjectInstantiator;
import org.apache.isis.core.metamodel.spec.ObjectInstantiatorAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.runtimes.dflt.runtime.persistence.container.DomainObjectContainerObjectChanged;
import org.apache.isis.runtimes.dflt.runtime.persistence.container.DomainObjectContainerResolve;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.runtime.system.session.IsisSession;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.MessageBroker;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.UpdateNotifier;

/**
 * Provides services to the metamodel based on the currently running
 * {@link IsisSession session} (primarily the {@link PersistenceSession}).
 */
public class RuntimeContextFromSession extends RuntimeContextAbstract {

    private final AuthenticationSessionProvider authenticationSessionProvider;
    private final AdapterMap adapterManager;
    private final ObjectDirtier objectDirtier;
    private final ObjectInstantiator objectInstantiator;
    private final ObjectPersistor objectPersistor;
    private final ServicesProvider servicesProvider;
    private final DependencyInjector dependencyInjector;
    private final QuerySubmitter querySubmitter;
    private final DomainObjectServices domainObjectServices;
    private final LocalizationProviderAbstract localizationProvider;

    // //////////////////////////////////////////////////////////////////
    // Constructor
    // //////////////////////////////////////////////////////////////////

    public RuntimeContextFromSession() {
        this.authenticationSessionProvider = new AuthenticationSessionProviderAbstract() {

            @Override
            public AuthenticationSession getAuthenticationSession() {
                return IsisContext.getAuthenticationSession();
            }
        };
        this.adapterManager = new AdapterMapAbstract() {

            @Override
            public ObjectAdapter getAdapterFor(final Object pojo) {
                return getRuntimeAdapterManager().getAdapterFor(pojo);
            }

            @Override
            public ObjectAdapter adapterFor(final Object pojo) {
                return getRuntimeAdapterManager().adapterFor(pojo);
            }

            @Override
            public ObjectAdapter adapterFor(final Object pojo, final ObjectAdapter ownerAdapter) {
                return getRuntimeAdapterManager().adapterFor(pojo, ownerAdapter);
            }

            @Override
            public ObjectAdapter adapterFor(final Object pojo, final ObjectAdapter ownerAdapter, final OneToManyAssociation collection) {
                return getRuntimeAdapterManager().adapterFor(pojo, ownerAdapter, collection);
            }
        };
        this.objectInstantiator = new ObjectInstantiatorAbstract() {

            @Override
            public Object instantiate(final Class<?> cls) throws ObjectInstantiationException {
                return getPersistenceSession().getObjectFactory().instantiate(cls);
            }
        };

        this.objectDirtier = new ObjectDirtierAbstract() {

            @Override
            public void objectChanged(final ObjectAdapter adapter) {
                getPersistenceSession().objectChanged(adapter);
            }

            @Override
            public void objectChanged(final Object object) {
                new DomainObjectContainerObjectChanged().objectChanged(object);
            }
        };
        this.objectPersistor = new ObjectPersistorAbstract() {
            @Override
            public void makePersistent(final ObjectAdapter adapter) {
                getPersistenceSession().makePersistent(adapter);
            }

            @Override
            public void remove(final ObjectAdapter adapter) {
                getUpdateNotifier().addDisposedObject(adapter);
                getPersistenceSession().destroyObject(adapter);
            }
        };
        this.servicesProvider = new ServicesProviderAbstract() {
            @Override
            public List<ObjectAdapter> getServices() {
                return getPersistenceSession().getServices();
            }
        };
        this.domainObjectServices = new DomainObjectServicesAbstract() {

            @Override
            public ObjectAdapter createTransientInstance(final ObjectSpecification spec) {
                return getPersistenceSession().createInstance(spec);
            }

            @Override
            public ObjectAdapter createAggregatedInstance(final ObjectSpecification spec, final ObjectAdapter parent) {
                return getPersistenceSession().createInstance(spec, parent);
            };

            @Override
            public void resolve(final Object parent) {
                new DomainObjectContainerResolve().resolve(parent);
            }

            @Override
            public void resolve(final Object parent, final Object field) {
                new DomainObjectContainerResolve().resolve(parent, field);
            }

            @Override
            public boolean flush() {
                return getTransactionManager().flushTransaction();
            }

            @Override
            public void commit() {
                getTransactionManager().endTransaction();
            }

            @Override
            public void informUser(final String message) {
                getMessageBroker().addMessage(message);
            }

            @Override
            public void warnUser(final String message) {
                getMessageBroker().addWarning(message);
            }

            @Override
            public void raiseError(final String message) {
                throw new ApplicationException(message);
            }

            @Override
            public String getProperty(final String name) {
                return RuntimeContextFromSession.this.getProperty(name);
            }

            @Override
            public List<String> getPropertyNames() {
                return RuntimeContextFromSession.this.getPropertyNames();
            }

        };
        this.querySubmitter = new QuerySubmitterAbstract() {

            @Override
            public <T> List<ObjectAdapter> allMatchingQuery(final Query<T> query) {
                final ObjectAdapter instances = getPersistenceSession().findInstances(query, QueryCardinality.MULTIPLE);
                return CollectionFacetUtils.convertToAdapterList(instances);
            }

            @Override
            public <T> ObjectAdapter firstMatchingQuery(final Query<T> query) {
                final ObjectAdapter instances = getPersistenceSession().findInstances(query, QueryCardinality.SINGLE);
                final List<ObjectAdapter> list = CollectionFacetUtils.convertToAdapterList(instances);
                return list.size() > 0 ? list.get(0) : null;
            }
        };
        this.dependencyInjector = new DependencyInjector() {

            @Override
            public void injectDependenciesInto(final Object object) {
                getPersistenceSession().getServicesInjector().injectDependenciesInto(object);
            }

            @Override
            public void injectDependenciesInto(List<Object> objects) {
                getPersistenceSession().getServicesInjector().injectDependenciesInto(objects);
            }
            
            @Override
            public void injectInto(Object candidate) {
                if (DependencyInjectorAware.class.isAssignableFrom(candidate.getClass())) {
                    final DependencyInjectorAware cast = DependencyInjectorAware.class.cast(candidate);
                    cast.setDependencyInjector(this);
                }
            }
        };
        this.localizationProvider = new LocalizationProviderAbstract() {

            @Override
            public Localization getLocalization() {
                return IsisContext.getLocalization();
            }
        };
    }

    // //////////////////////////////////////////////////////////////////
    // Components
    // //////////////////////////////////////////////////////////////////

    @Override
    public AuthenticationSessionProvider getAuthenticationSessionProvider() {
        return authenticationSessionProvider;
    }

    @Override
    public AdapterMap getAdapterMap() {
        return adapterManager;
    }

    @Override
    public ObjectInstantiator getObjectInstantiator() {
        return objectInstantiator;
    }

    @Override
    public DomainObjectServices getDomainObjectServices() {
        return domainObjectServices;
    }

    @Override
    public ServicesProvider getServicesProvider() {
        return servicesProvider;
    }

    @Override
    public LocalizationProviderAbstract getLocalizationProvider() {
        return localizationProvider;
    }

    @Override
    public ObjectDirtier getObjectDirtier() {
        return objectDirtier;
    }

    @Override
    public ObjectPersistor getObjectPersistor() {
        return objectPersistor;
    }

    @Override
    public DependencyInjector getDependencyInjector() {
        return dependencyInjector;
    }

    @Override
    public QuerySubmitter getQuerySubmitter() {
        return querySubmitter;
    }

    // ///////////////////////////////////////////
    // Dependencies (from context)
    // ///////////////////////////////////////////

    private static PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    private static AdapterManager getRuntimeAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    private static UpdateNotifier getUpdateNotifier() {
        return IsisContext.getUpdateNotifier();
    }

    private static IsisTransactionManager getTransactionManager() {
        return getPersistenceSession().getTransactionManager();
    }

    private static MessageBroker getMessageBroker() {
        return IsisContext.getMessageBroker();
    }


}
