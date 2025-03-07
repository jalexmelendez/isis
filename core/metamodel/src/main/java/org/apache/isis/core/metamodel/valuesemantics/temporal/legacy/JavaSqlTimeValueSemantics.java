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
package org.apache.isis.core.metamodel.valuesemantics.temporal.legacy;

import java.sql.Time;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.clock.VirtualClock;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.schema.common.v2.ValueType;

import lombok.Getter;
import lombok.Setter;

/**
 * Treats {@link java.sql.Time} as a time-only value type.
 *
 */
@Component
@Named("isis.val.JavaSqlTimeValueSemantics")
public class JavaSqlTimeValueSemantics
extends LegacyTemporalValueSemanticsAbstract<Time> {

    protected static void initFormats(final Map<String, DateFormat> formats) {
        formats.put(ISO_ENCODING_FORMAT, createDateEncodingFormat("HHmmssSSS"));
        formats.put("short", DateFormat.getTimeInstance(DateFormat.SHORT));
    }

    @Inject ClockService clockService;

    @Override
    public Class<Time> getCorrespondingClass() {
        return Time.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.LOCAL_TIME;
    }

    @Getter @Setter
    private String configuredFormat;

    public JavaSqlTimeValueSemantics(final IsisConfiguration config) {
        super(java.sql.Time.class, 8);

        final Map<String, DateFormat> formats = formats();
        configuredFormat = config.getValueTypes().getJavaSql().getTime().getFormat();
        format = formats.get(configuredFormat);
        if (format == null) {
            setMask(configuredFormat);
        }
    }

    @Override
    protected void clearFields(final Calendar cal) {
        cal.set(Calendar.YEAR, 1970);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
    }

    @Override
    protected DateFormat format() {

        final Locale locale = Locale.getDefault();
        final DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
        dateFormat.setTimeZone(UTC_TIME_ZONE);
        return dateFormat;
    }

    @Override
    protected List<DateFormat> formatsToTry() {
        List<DateFormat> formats = new ArrayList<DateFormat>();

        final Locale locale = Locale.getDefault();

        formats.add(DateFormat.getTimeInstance(DateFormat.LONG, locale));
        formats.add(DateFormat.getTimeInstance(DateFormat.MEDIUM, locale));
        formats.add(DateFormat.getTimeInstance(DateFormat.SHORT, locale));
        formats.add(createDateFormat("HH:mm:ss.SSS"));
        formats.add(createDateFormat("HHmmssSSS"));
        formats.add(createDateFormat("HH:mm:ss"));
        formats.add(createDateFormat("HHmmss"));

        for (DateFormat format : formats) {
            format.setTimeZone(UTC_TIME_ZONE);
        }

        return formats;
    }

    private static Map<String, DateFormat> formats = _Maps.newHashMap();

    static {
        initFormats(formats);
    }


    @Override
    public Time add(final Time original, final int years, final int months, final int days, final int hours, final int minutes) {
        final java.sql.Time time = original;
        final Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        cal.set(Calendar.YEAR, 0);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 0);
        cal.set(Calendar.MILLISECOND, 0);

        cal.add(Calendar.HOUR, hours);
        cal.add(Calendar.MINUTE, minutes);

        return setDate(cal.getTime());
    }

    @Override
    public java.util.Date dateValue(final Object object) {
        final java.sql.Time time = (Time) object;
        return time == null ? null : new java.util.Date(time.getTime());
    }

    @Override
    protected Map<String, DateFormat> formats() {
        return formats;
    }

    @Override
    protected Time now() {
        return Optional.ofNullable(clockService)
                .map(ClockService::getClock)
                .map(VirtualClock::nowAsEpochMilli)
                .map(Time::new)
                .orElseGet(()->new Time(System.currentTimeMillis())); // fallback to system time
    }

    @Override
    protected Time setDate(final Date date) {
        return new java.sql.Time(date.getTime());
    }

}
