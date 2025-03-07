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
package org.apache.isis.extensions.secman.applib.permission.spi;

import java.util.Collection;

import org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermissionRule;
import org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermissionValue;

/**
 * An implementation whereby a VETO permission for a feature overrides an ALLOW (for same scope).
 *
 * @since 2.0 {@index}
 *
 * @deprecated - use <code>application.yml</code> config properties instead;
 * corresponds to <code>isis.extensions.secman.permissions-evaluation-policy=ALLOW_BEATS_VETO</code>,
 * with supported values <code>ALLOW_BEATS_VETO</code> (default) and <code>VETO_BEATS_ALLOW</code>
 *
 */
@Deprecated
public class PermissionsEvaluationServiceAllowBeatsVeto
extends PermissionsEvaluationServiceAbstract {

    private static final long serialVersionUID = 1L;

    /**
     * Returns the lists unchanged.
     *
     * <p>
     * This implementation relies on the fact that the {@link org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermissionValue}s are
     * passed through in natural order, with the leading part based on the
     * {@link org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermissionValue#getRule() rule} and with
     * {@link ApplicationPermissionRule} in turn comparable so that {@link ApplicationPermissionRule#ALLOW allow}
     * is ordered before {@link ApplicationPermissionRule#VETO veto}.
     * </p>
     */
    @Override
    protected Collection<ApplicationPermissionValue> ordered(
            final Collection<ApplicationPermissionValue> permissionValues) {

        return permissionValues;
    }

}
