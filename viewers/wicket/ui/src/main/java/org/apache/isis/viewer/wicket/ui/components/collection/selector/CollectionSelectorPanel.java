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
package org.apache.isis.viewer.wicket.ui.components.collection.selector;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.isis.core.metamodel.commons.StringExtensions;
import org.apache.isis.core.metamodel.interactions.managed.nonscalar.DataTableModel;
import org.apache.isis.viewer.wicket.model.hints.IsisSelectorEvent;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.util.ComponentHintKey;
import org.apache.isis.viewer.wicket.ui.CollectionContentsAsFactory;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import lombok.val;

/**
 * Provides a list of links for selecting other views that support
 * {@link org.apache.isis.viewer.common.model.components.ComponentType#COLLECTION_CONTENTS} with a backing
 * {@link org.apache.isis.viewer.wicket.model.models.EntityCollectionModel}.
 */
public class CollectionSelectorPanel
extends PanelAbstract<DataTableModel, EntityCollectionModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_VIEWS = "views";
    private static final String ID_VIEW_LIST = "viewList";
    private static final String ID_VIEW_LINK = "viewLink";
    private static final String ID_VIEW_ITEM = "viewItem";
    private static final String ID_VIEW_ITEM_TITLE = "viewItemTitle";
    private static final String ID_VIEW_ITEM_ICON = "viewItemIcon";

    private static final String ID_VIEW_BUTTON_TITLE = "viewButtonTitle";
    private static final String ID_VIEW_BUTTON_ICON = "viewButtonIcon";

    private final CollectionSelectorHelper selectorHelper;
    private final ComponentHintKey componentHintKey;

    private ComponentFactory selectedComponentFactory;

    public CollectionSelectorPanel(
            final String id,
            final EntityCollectionModel model) {
        this(id, model, ComponentHintKey.noop());
    }

    public CollectionSelectorPanel(
            final String id,
            final EntityCollectionModel model,
            final ComponentHintKey componentHintKey) {
        super(id, model);
        this.componentHintKey = componentHintKey;

        selectorHelper = new CollectionSelectorHelper(
                model, getComponentFactoryRegistry(), componentHintKey);
    }

    /**
     * Build UI only after added to parent.
     */
    @Override
    public void onInitialize() {
        super.onInitialize();
        addDropdown();
    }

    private void addDropdown() {
        final List<ComponentFactory> componentFactories = selectorHelper.getComponentFactories();
        final String selected = selectorHelper.honourViewHintElseDefault(this);

        // selector
        if (componentFactories.size() <= 1) {
            permanentlyHide(ID_VIEWS);
        } else {
            final Model<ComponentFactory> componentFactoryModel = new Model<>();

            this.selectedComponentFactory = selectorHelper.find(selected);
            componentFactoryModel.setObject(this.selectedComponentFactory);

            final WebMarkupContainer views = new WebMarkupContainer(ID_VIEWS);

            final Label viewButtonTitle = Wkt.labelAdd(views, ID_VIEW_BUTTON_TITLE, "Hidden");
            final Label viewButtonIcon = Wkt.labelAdd(views, ID_VIEW_BUTTON_ICON, "");

            final WebMarkupContainer container = new WebMarkupContainer(ID_VIEW_LIST);

            views.addOrReplace(container);
            views.setOutputMarkupId(true);

            this.setOutputMarkupId(true);

            Wkt.listViewAdd(container, ID_VIEW_ITEM, componentFactories, item->{
                final ComponentFactory componentFactory = item.getModelObject();

                val link = Wkt.linkAdd(item, ID_VIEW_LINK, target->{
                    CollectionSelectorPanel linksSelectorPanel = CollectionSelectorPanel.this;
                    linksSelectorPanel.setViewHintAndBroadcast(componentFactory.getName(), target);

                    linksSelectorPanel.selectedComponentFactory = componentFactory;

                    CollectionSelectorPanel.this.getModel().parentedHintingBookmark()
                    .ifPresent(bookmark->componentHintKey.set(bookmark, componentFactory.getName()));

                    target.add(linksSelectorPanel, views);
                });

                final IModel<String> title = nameFor(componentFactory);
                Wkt.labelAdd(link, ID_VIEW_ITEM_TITLE, title);
                final Label viewItemIcon = Wkt.labelAdd(link, ID_VIEW_ITEM_ICON, "");

                final boolean isSelected = componentFactory == CollectionSelectorPanel.this.selectedComponentFactory;
                if (isSelected) {
                    viewButtonTitle.setDefaultModel(title);
                    IModel<String> cssClass = cssClassFor(componentFactory, viewButtonIcon);
                    viewButtonIcon.add(AttributeModifier.replace("class", "ViewLinkItem " + cssClass.getObject()));
                    link.setVisible(false);
                } else {
                    Wkt.cssAppend(viewItemIcon, cssClassFor(componentFactory, viewItemIcon));
                }

                item.add(link);
            });

            addOrReplace(views);
        }
    }

    private static IModel<String> cssClassFor(final ComponentFactory componentFactory, final Label viewIcon) {
        IModel<String> cssClass = null;
        if (componentFactory instanceof CollectionContentsAsFactory) {
            CollectionContentsAsFactory collectionContentsAsFactory = (CollectionContentsAsFactory) componentFactory;
            cssClass = collectionContentsAsFactory.getCssClass();
            viewIcon.setDefaultModelObject("");
            viewIcon.setEscapeModelStrings(true);
        }
        if (cssClass == null) {
            String name = componentFactory.getName();
            cssClass = Model.of(StringExtensions.asLowerDashed(name));
            // Small hack: if there is no specific CSS class then we assume that background-image is used
            // the span.ViewItemLink should have some content to show it
            // FIX: find a way to do this with CSS (width and height don't seems to help)
            viewIcon.setDefaultModelObject("&#160;&#160;&#160;&#160;&#160;");
            viewIcon.setEscapeModelStrings(false);
        }
        return cssClass;
    }

    private static IModel<String> nameFor(final ComponentFactory componentFactory) {
        IModel<String> name = null;
        if (componentFactory instanceof CollectionContentsAsFactory) {
            CollectionContentsAsFactory collectionContentsAsFactory = (CollectionContentsAsFactory) componentFactory;
            name = collectionContentsAsFactory.getTitleLabel();
        }
        if (name == null) {
            name = Model.of(componentFactory.getName());
        }
        return name;
    }

    protected void setViewHintAndBroadcast(final String viewName, final AjaxRequestTarget target) {
        final CollectionSelectorPanel component = CollectionSelectorPanel.this;
        final IsisSelectorEvent selectorEvent =
                new IsisSelectorEvent(component, CollectionSelectorHelper.UIHINT_EVENT_VIEW_KEY, viewName, target);
        send(getPage(), Broadcast.EXACT, selectorEvent);
    }
}



