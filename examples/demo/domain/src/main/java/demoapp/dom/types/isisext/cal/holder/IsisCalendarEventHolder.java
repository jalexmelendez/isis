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
package demoapp.dom.types.isisext.cal.holder;

import org.apache.isis.extensions.fullcalendar.applib.value.CalendarEvent;

//tag::class[]
public interface IsisCalendarEventHolder {

    CalendarEvent getReadOnlyProperty();
    void setReadOnlyProperty(CalendarEvent c);

    CalendarEvent getReadWriteProperty();
    void setReadWriteProperty(CalendarEvent c);

    CalendarEvent getReadOnlyOptionalProperty();
    void setReadOnlyOptionalProperty(CalendarEvent c);

    CalendarEvent getReadWriteOptionalProperty();
    void setReadWriteOptionalProperty(CalendarEvent c);

}
//end::class[]
