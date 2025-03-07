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
package org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions;

import java.util.List;

import org.apache.wicket.MarkupContainer;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.common.model.decorator.confirm.ConfirmUiModel;
import org.apache.isis.viewer.common.model.decorator.confirm.ConfirmUiModel.Placement;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.links.ListOfLinksModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.linkandlabel.ActionLink;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;
import org.apache.isis.viewer.wicket.ui.util.Decorators;
import org.apache.isis.viewer.wicket.ui.util.Tooltips;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import lombok.val;

public class AdditionalLinksPanel
extends PanelAbstract<List<LinkAndLabel>, ListOfLinksModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_ADDITIONAL_LINK_LIST = "additionalLinkList";
    private static final String ID_ADDITIONAL_LINK_ITEM = "additionalLinkItem";
    private static final String ID_ADDITIONAL_LINK_TITLE = "additionalLinkTitle";
    public  static final String ID_ADDITIONAL_LINK = "additionalLink";

    public enum Style {
        INLINE_LIST {
            @Override
            AdditionalLinksPanel newPanel(final String id, final Can<LinkAndLabel> links) {
                return new AdditionalLinksAsListInlinePanel(id, links);
            }
        },
        DROPDOWN {
            @Override
            AdditionalLinksPanel newPanel(final String id, final Can<LinkAndLabel> links) {
                return new AdditionalLinksAsDropDownPanel(id, links);
            }
        };
        abstract AdditionalLinksPanel newPanel(String id, Can<LinkAndLabel> links);
    }

    public static AdditionalLinksPanel addAdditionalLinks(
            final MarkupContainer markupContainer,
            final String id,
            final Can<LinkAndLabel> links,
            final Style style) {
        if(links.isEmpty()) {
            Components.permanentlyHide(markupContainer, id);
            return null;
        }
        return Wkt.add(markupContainer, style.newPanel(id, links));
    }

    protected AdditionalLinksPanel(
            final String id,
            final Can<LinkAndLabel> linksDoNotUseDirectlyInsteadUseOfListOfLinksModel) {

        super(id, new ListOfLinksModel(linksDoNotUseDirectlyInsteadUseOfListOfLinksModel));
        setOutputMarkupId(true);

        val container = Wkt.add(this, Wkt.containerWithVisibility(ID_ADDITIONAL_LINK_LIST,
                    ()->AdditionalLinksPanel.this.getModel().hasAnyVisibleLink()));

        Wkt.listViewAdd(container, ID_ADDITIONAL_LINK_ITEM, getModel(), item->{

            val linkAndLabel = item.getModelObject();
            val link = linkAndLabel.getUiComponent();
            val action = linkAndLabel.getManagedAction().getAction();

            Tooltips.addTooltip(link, link instanceof ActionLink
                        && _Strings.isNotEmpty(((ActionLink) link).getReasonDisabledIfAny())
                    ? ((ActionLink) link).getReasonDisabledIfAny()
                    : linkAndLabel.getDescription().orElse(null));

            if(ObjectAction.Util.returnsBlobOrClob(action)) {
                Wkt.cssAppend(link, "noVeil");
            }
            if(action.isPrototype()) {
                Wkt.cssAppend(link, "prototype");
            }
            Wkt.cssAppend(link, linkAndLabel.getFeatureIdentifier());

            if (action.getSemantics().isAreYouSure()) {
                if(action.getParameterCount()==0) {
                    val hasDisabledReason = link instanceof ActionLink
                            ? _Strings.isNotEmpty(((ActionLink)link).getReasonDisabledIfAny())
                            : false;
                    if (!hasDisabledReason) {
                        val confirmUiModel = ConfirmUiModel.ofAreYouSure(getTranslationService(), Placement.BOTTOM);
                        Decorators.getConfirm().decorate(link, confirmUiModel);
                    }
                }
                // ensure links receive the danger style
                // don't care if expressed twice
                Decorators.getDanger().decorate(link);
            }

            linkAndLabel
            .getAdditionalCssClass()
            .ifPresent(cssClass->Wkt.cssAppend(link, cssClass));

            val viewTitleLabel = Wkt.labelAdd(link, ID_ADDITIONAL_LINK_TITLE,
                    linkAndLabel::getFriendlyName);

            val fontAwesome = linkAndLabel.getFontAwesomeUiModel();
            Decorators.getIcon().decorate(viewTitleLabel, fontAwesome);
            Decorators.getMissingIcon().decorate(viewTitleLabel, fontAwesome);

            item.addOrReplace(link);
        });
    }

}
