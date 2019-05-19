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
package domainapp.dom.events;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.services.eventbus.EventBusService;

@DomainService(nature=NatureOfService.VIEW_MENU_ONLY)
@DomainObjectLayout(named="Event Demo")
public class EventLogMenu {

	@Inject private EventLogRepository eventLog;
	@Inject private EventBusService eventBusService;
	
	@Action
	public List<EventLogEntry> listEvents(){
		return eventLog.listAll();
	}
	
	public static class EventTestProgrammaticEvent extends ActionDomainEvent<EventLogMenu>  {
		private static final long serialVersionUID = 1L;
	}
	
	@Action
	@ActionLayout(cssClassFa="fa-bolt")
	public List<EventLogEntry> triggerEvent(){
		eventBusService.post(new EventTestProgrammaticEvent());
		return listEvents();
	}

	
}
