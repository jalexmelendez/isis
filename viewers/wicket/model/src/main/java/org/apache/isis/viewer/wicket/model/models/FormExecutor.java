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
package org.apache.isis.viewer.wicket.model.models;

import java.io.Serializable;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;

/**
 * Passed through the {@link ActionModelImpl} or {@link ScalarModel}, allowing
 * two different Wicket UI components (eg owning <code>ActionPanel</code> and
 * <code>ActionParametersFormPanel</code> to interact.
 */
public interface FormExecutor extends Serializable {

    enum FormExecutionOutcome {
        FAILURE_SO_STAY_ON_PAGE,
        SUCCESS_SO_REDIRECT_TO_RESULT_PAGE,
        SUCCESS_IN_NESTED_CONTEXT_SO_STAY_ON_PAGE;

        public boolean isFailure() { return this == FAILURE_SO_STAY_ON_PAGE; }
        public boolean isSuccess() { return this != FAILURE_SO_STAY_ON_PAGE; }
        public boolean isSuccessWithRedirect() { return this == SUCCESS_SO_REDIRECT_TO_RESULT_PAGE; }
        public boolean isSuccessWithinNestedContext() { return this == SUCCESS_IN_NESTED_CONTEXT_SO_STAY_ON_PAGE; }
    }

    FormExecutionOutcome executeAndProcessResults(
            Page page,
            AjaxRequestTarget targetIfAny,
            Form<?> feedbackFormIfAny,
            FormExecutorContext formExecutorContext);
}
