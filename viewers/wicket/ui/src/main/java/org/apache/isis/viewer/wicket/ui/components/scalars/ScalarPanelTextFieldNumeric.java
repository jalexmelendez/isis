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
package org.apache.isis.viewer.wicket.ui.components.scalars;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;

import org.apache.isis.core.metamodel.commons.ScalarRepresentation;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import lombok.NonNull;
import lombok.val;

/**
 * Panel for rendering numeric scalars.
 */
public abstract class ScalarPanelTextFieldNumeric<T extends Serializable>
extends ScalarPanelTextFieldAbstract<T> {

    private static final long serialVersionUID = 1L;

    protected ScalarPanelTextFieldNumeric(
            final String id,
            final ScalarModel scalarModel,
            final Class<T> cls) {
        super(id, scalarModel, cls);
    }

    @Override
    protected Component createComponentForCompact() {
        val label = Wkt.labelAddWithConverter(
                getCompactFragment(CompactType.SPAN),
                ID_SCALAR_IF_COMPACT, newTextFieldValueModel(), cls, getConverter(getModel()));
        label.setEnabled(false);
        return label;
    }

    @Override
    protected IModel<String> obtainInlinePromptModel() {
        return super.toStringConvertingModelOf(getConverter(scalarModel));
    }

    protected final IConverter<T> getConverter(final ScalarModel scalarModel) {
        return getConverter(scalarModel.getMetaModel(), scalarModel.isEditMode()
                ? ScalarRepresentation.EDITING
                : ScalarRepresentation.VIEWING);
    }

    /**
     * Converter that is used for the either regular (editing) or compact (HTML) view of the panel,
     * based on argument {@code scalarRepresentation}.
     */
    protected abstract IConverter<T> getConverter(
            @NonNull ObjectFeature propOrParam,
            @NonNull ScalarRepresentation scalarRepresentation);

}
