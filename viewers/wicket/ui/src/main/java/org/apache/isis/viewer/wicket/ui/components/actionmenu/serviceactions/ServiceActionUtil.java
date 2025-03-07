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
package org.apache.isis.viewer.wicket.ui.components.actionmenu.serviceactions;

import java.util.List;
import java.util.function.Consumer;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.Fragment;

import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.common.model.menu.MenuItemDto;
import org.apache.isis.viewer.common.model.menu.MenuUiModel;
import org.apache.isis.viewer.common.model.menu.MenuVisitor;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.LinkAndLabelFactory;
import org.apache.isis.viewer.wicket.ui.util.Decorators;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
//@Log4j2
public final class ServiceActionUtil {

    static void addLeafItem(
            final IsisAppCommonContext commonContext,
            final CssMenuItem menuItem,
            final ListItem<CssMenuItem> listItem,
            final MarkupContainer parent) {

        val actionUiModel = menuItem.getLinkAndLabel();
        val menuItemActionLink = actionUiModel.getUiComponent();
        val menuItemLabel = Wkt.labelAdd(menuItemActionLink, "menuLinkLabel", menuItem.getName());

        Decorators.getActionLink().decorateMenuItem(
                listItem,
                actionUiModel,
                commonContext.getTranslationService());

        val fontAwesome = actionUiModel.getFontAwesomeUiModel();
        Decorators.getIcon().decorate(menuItemLabel, fontAwesome);
        Decorators.getMissingIcon().decorate(menuItemActionLink, fontAwesome);

        val leafItem = new Fragment("content", "leafItem", parent);
        leafItem.add(menuItemActionLink);

        listItem.add(leafItem);
    }

    static void addFolderItem(
            final IsisAppCommonContext commonContext,
            final CssMenuItem subMenuItem,
            final ListItem<CssMenuItem> listItem,
            final MarkupContainer parent) {

        Wkt.cssAppend(listItem, "dropdown-submenu");

        Fragment folderItem = new Fragment("content", "folderItem", parent);
        listItem.add(folderItem);

        Wkt.labelAdd(folderItem, "folderName", ()->subMenuItem.getLinkAndLabel().getFriendlyName());
        final List<CssMenuItem> menuItems = subMenuItem.getSubMenuItems();

        Wkt.listViewAdd(folderItem, "subMenuItems", menuItems, item->{
            CssMenuItem menuItem = listItem.getModelObject();

            if (menuItem.hasSubMenuItems()) {
                addFolderItem(commonContext, menuItem, item, parent);
            } else {
                addLeafItem(commonContext, menuItem, item, parent);
            }
        });

    }

    @RequiredArgsConstructor(staticName = "of")
    private static class MenuBuilderWkt implements MenuVisitor {

        private final IsisAppCommonContext commonContext;
        private final Consumer<CssMenuItem> onNewMenuItem;

        private CssMenuItem currentTopLevelMenu = null;

        @Override
        public void addTopLevel(final MenuItemDto menuDto) {
            currentTopLevelMenu = CssMenuItem.newMenuItem(menuDto.getName());
            onNewMenuItem.accept(currentTopLevelMenu);
        }

        @Override
        public void addSectionSpacer() {
            val menuSection = CssMenuItem.newSpacer();
            currentTopLevelMenu.addSubMenuItem(menuSection);
        }

        @Override
        public void addSubMenu(final MenuItemDto menuDto) {
            val managedAction = menuDto.getManagedAction();

            val menuItem = CssMenuItem.newMenuItem(menuDto.getName());
            currentTopLevelMenu.addSubMenuItem(menuItem);

            menuItem.setLinkAndLabel(newActionLink(managedAction));
        }

        @Override
        public void addSectionLabel(final String named) {
            val menuSectionLabel = CssMenuItem.newSectionLabel(named);
            currentTopLevelMenu.addSubMenuItem(menuSectionLabel);
        }

        private LinkAndLabel newActionLink(
                final ManagedAction managedAction) {

            val serviceModel = EntityModel.ofAdapter(commonContext, managedAction.getOwner());

            return LinkAndLabelFactory.forMenu(serviceModel)
                    .apply(managedAction.getAction());
        }

    }

    public static void buildMenu(
            final IsisAppCommonContext commonContext,
            final MenuUiModel menuUiModel,
            final Consumer<CssMenuItem> onNewMenuItem) {

        menuUiModel.buildMenuItems(commonContext,
                MenuBuilderWkt.of(
                        commonContext,
                        onNewMenuItem));
    }

}
