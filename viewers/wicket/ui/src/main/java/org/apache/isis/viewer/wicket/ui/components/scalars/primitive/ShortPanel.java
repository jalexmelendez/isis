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
package org.apache.isis.viewer.wicket.ui.components.scalars.primitive;

import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.util.convert.IConverter;

import org.apache.isis.core.metamodel.commons.ScalarRepresentation;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.viewer.wicket.model.converter.ShortConverterWkt;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelTextFieldNumeric;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import lombok.NonNull;

/**
 * Panel for rendering scalars of type {@link Short} or <tt>short</tt>.
 */
public class ShortPanel extends ScalarPanelTextFieldNumeric<Short> {

    private static final long serialVersionUID = 1L;

    public ShortPanel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel, Short.class);
    }

    @Override
    protected AbstractTextComponent<Short> createTextFieldForRegular(final String id) {
        return Wkt.textFieldWithConverter(
                id, newTextFieldValueModel(), Short.class, getConverter(getModel()));
    }

    @Override
    protected String getScalarPanelType() {
        return "shortPanel";
    }

    @Override
    protected IConverter<Short> getConverter(
            final @NonNull ObjectFeature propOrParam,
            final @NonNull ScalarRepresentation scalarRepresentation) {
        return new ShortConverterWkt(propOrParam, scalarRepresentation);
    }
}
