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
package org.apache.isis.viewer.wicket.viewer.integration;

import org.apache.wicket.Session;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;

import org.apache.isis.applib.clock.VirtualClock;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.session.SessionLoggingService;
import org.apache.isis.applib.services.user.ImpersonatedUserHolder;
import org.apache.isis.applib.services.user.UserMemento;
import org.apache.isis.applib.services.user.UserMemento.AuthenticationSource;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.core.runtime.context.IsisAppCommonContext.HasCommonContext;
import org.apache.isis.core.security.authentication.AuthenticationRequestPassword;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;
import org.apache.isis.viewer.wicket.model.models.BookmarkedPagesModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.breadcrumbs.BreadcrumbModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.breadcrumbs.BreadcrumbModelProvider;
import org.apache.isis.viewer.wicket.ui.pages.BookmarkedPagesModelProvider;

import lombok.Getter;
import lombok.val;

/**
 * Viewer-specific implementation of {@link AuthenticatedWebSession}, which
 * delegates to the Isis' configured {@link AuthenticationManager}, and which
 * also tracks thread usage (so that multiple concurrent requests are all
 * associated with the same session).
 */
public class AuthenticatedWebSessionForIsis
extends AuthenticatedWebSession
implements BreadcrumbModelProvider, BookmarkedPagesModelProvider, HasCommonContext {

    private static final long serialVersionUID = 1L;

    public static final String USER_ROLE = "org.apache.isis.viewer.wicket.roles.USER";

    public static AuthenticatedWebSessionForIsis get() {
        return (AuthenticatedWebSessionForIsis) Session.get();
    }

    @Getter protected transient IsisAppCommonContext commonContext;

    private BreadcrumbModel breadcrumbModel;
    private BookmarkedPagesModel bookmarkedPagesModel;

    /**
     * As populated in {@link #signIn(String, String)}.
     */
    private InteractionContext authentication;

    public AuthenticatedWebSessionForIsis(Request request) {
        super(request);
    }

    public void init(IsisAppCommonContext commonContext) {
        this.commonContext = commonContext;
        bookmarkedPagesModel = new BookmarkedPagesModel(commonContext);
        breadcrumbModel = new BreadcrumbModel(commonContext);

    }

    @Override
    public synchronized boolean authenticate(final String username, final String password) {
        val authenticationRequest = new AuthenticationRequestPassword(username, password);
        authenticationRequest.addRole(USER_ROLE);
        this.authentication = getAuthenticationManager().authenticate(authenticationRequest);
        if (this.authentication != null) {
            log(SessionLoggingService.Type.LOGIN, username, null);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public synchronized void invalidateNow() {

        //
        // similar code in Restful Objects viewer (UserResourceServerside#logout)
        //
        // this needs to be done here because Wicket will expire the HTTP session
        // while the Shiro authenticator uses the session to obtain the details of the
        // principals for it to logout
        //

        getAuthenticationManager().closeSession(getAuthentication());

        super.invalidateNow();

    }

    @Override
    public synchronized void onInvalidate() {

        String userName = null;
        val authentication = getAuthentication();
        if (authentication != null) {
            userName = authentication.getUser().getName();
        }

        super.onInvalidate();

        val causedBy = RequestCycle.get() != null
                ? SessionLoggingService.CausedBy.USER
                : SessionLoggingService.CausedBy.SESSION_EXPIRATION;


        log(SessionLoggingService.Type.LOGOUT, userName, causedBy);
    }

    /**
     * If there is an {@link InteractionContext} already (as some authentication mechanisms setup in filters, eg
     * SpringSecurityFilter), then just use it.
     *
     * <p>
     *     This method is called early on by {@link WebRequestCycleForIsis}.
     * </p>
     */
    public void syncExternalAuthenticationIfAvailable() {

        val interactionService = commonContext.lookupServiceElseFail(InteractionService.class);
        val interactionContextIfAny = interactionService.currentInteractionContext();
        interactionContextIfAny.ifPresent(interactionContext -> this.authentication = interactionContext);
    }

    /**
     * Returns an {@link InteractionContext} either as authenticated (and then cached on the session subsequently),
     * or taking into account {@link ImpersonatedUserHolder impersonation}.
     *
     * <p>
     *     The session must still {@link AuthenticationManager#isSessionValid(InteractionContext) be valid}, though
     *     note that this will always be true for externally authenticated users.
     * </p>
     */
    public synchronized InteractionContext getAuthentication() {

        if(this.authentication == null) {
            return null;
        }
        if(!getAuthenticationManager().isSessionValid(this.authentication)) {
            return null;
        }
        signIn(true);

        val impersonatedUserHolder = commonContext.lookupServiceElseFail(ImpersonatedUserHolder.class);
        return impersonatedUserHolder.getUserMemento()
                .map(x -> this.authentication.withUser(x))
                .orElse(this.authentication);
    }

    /**
     * This is a no-op if the {@link #getAuthentication() authentication session}'s
     * {@link UserMemento#getAuthenticationSource() source} is
     * {@link AuthenticationSource#EXTERNAL external}
     * (eg as managed by keycloak).
     */
    @Override
    public void invalidate() {
        if(this.authentication.getUser().getAuthenticationSource().isExternal()) {
            return;
        }
        // otherwise
        super.invalidate();
    }

    @Override
    public synchronized Roles getRoles() {
        if (!isSignedIn()) {
            return null;
        }

        final Roles roles = new Roles();
        getAuthentication().getUser().streamRoleNames()
        .forEach(roles::add);
        return roles;
    }

    @Override
    public synchronized void detach() {
        breadcrumbModel.detach();
        super.detach();
    }

    // /////////////////////////////////////////////////
    // Breadcrumbs and Bookmarks support
    // /////////////////////////////////////////////////

    @Override
    public BreadcrumbModel getBreadcrumbModel() {
        return breadcrumbModel;
    }

    @Override
    public BookmarkedPagesModel getBookmarkedPagesModel() {
        return bookmarkedPagesModel;
    }

    protected AuthenticationManager getAuthenticationManager() {
        return commonContext.getAuthenticationManager();
    }

    private void log(
            final SessionLoggingService.Type type,
            final String username,
            final SessionLoggingService.CausedBy causedBy) {


        val interactionFactory = getInteractionService();
        val sessionLoggingServices = getSessionLoggingServices();

        final Runnable loggingTask = ()->{

            val now = virtualClock().nowAsJavaUtilDate();

            // use hashcode as session identifier, to avoid re-binding http sessions if using Session#getId()
            int sessionHashCode = System.identityHashCode(AuthenticatedWebSessionForIsis.this);
            sessionLoggingServices.forEach(sessionLoggingService ->
                sessionLoggingService.log(type, username, now, causedBy, Integer.toString(sessionHashCode))
            );
        };

        if(interactionFactory!=null) {
            interactionFactory.runAnonymous(loggingTask::run);
        } else {
            loggingTask.run();
        }
    }

    protected Can<SessionLoggingService> getSessionLoggingServices() {
        return commonContext.getServiceRegistry().select(SessionLoggingService.class);
    }

    protected InteractionService getInteractionService() {
        return commonContext.lookupServiceElseFail(InteractionService.class);
    }

    private VirtualClock virtualClock() {
        try {
            return commonContext.getServiceRegistry()
                    .lookupService(ClockService.class)
                    .map(ClockService::getClock)
                    .orElseGet(this::nowFallback);
        } catch (Exception e) {
            return nowFallback();
        }
    }

    private VirtualClock nowFallback() {
        return VirtualClock.system();
    }

    @Override
    public void replaceSession() {
        // do nothing here because this will lead to problems with Shiro
        // see https://issues.apache.org/jira/browse/ISIS-1018
    }



}
