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
package org.apache.isis.core.metamodel.valuesemantics.temporal.legacy.joda;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Named;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.adapters.EncodingException;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.valuesemantics.temporal.legacy.LegacyTemporalValueSemanticsAbstract;
import org.apache.isis.schema.common.v2.ValueType;

import lombok.Getter;
import lombok.Setter;

@Component
@Named("isis.val.JodaDateTimeValueSemantics")
public class JodaDateTimeValueSemantics
extends LegacyTemporalValueSemanticsAbstract<DateTime> {

    private static final Map<String, DateFormat> FORMATS = _Maps.newHashMap();

    static {
        FORMATS.put(ISO_ENCODING_FORMAT, createDateEncodingFormat("yyyyMMdd"));
        FORMATS.put("iso", createDateFormat("yyyy-MM-dd"));
        FORMATS.put("medium", DateFormat.getDateInstance(DateFormat.MEDIUM));
    }

    @Override
    public Class<DateTime> getCorrespondingClass() {
        return DateTime.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.JODA_DATE_TIME;
    }

    @Getter @Setter
    private String configuredFormat;

    public JodaDateTimeValueSemantics(
            final IsisConfiguration config) {
        super(DateTime.class, 12);

        final Map<String, DateFormat> formats = formats();
        configuredFormat = config.getValueTypes().getJoda().getDateTime().getFormat();
        format = formats.get(configuredFormat);
        if (format == null) {
            setMask(configuredFormat);
        }
    }

    // //////////////////////////////////////////////////////////////////
    // temporal-specific stuff
    // //////////////////////////////////////////////////////////////////

    @Override
    protected void clearFields(final Calendar cal) {
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.AM_PM, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    @Override
    protected boolean ignoreTimeZone() {
        return true;
    }

    @Override
    protected Map<String, DateFormat> formats() {
        return FORMATS;
    }

    @Override
    protected DateFormat format() {
        final Locale locale = Locale.getDefault();

        final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        dateFormat.setTimeZone(UTC_TIME_ZONE);
        return dateFormat;
    }

    @Override
    protected List<DateFormat> formatsToTry() {

        final Locale locale = Locale.getDefault();

        List<DateFormat> formats = new ArrayList<>();

        formats.add(DateFormat.getDateInstance(DateFormat.LONG, locale));
        formats.add(DateFormat.getDateInstance(DateFormat.MEDIUM, locale));
        formats.add(DateFormat.getDateInstance(DateFormat.SHORT, locale));
        formats.add(createDateFormat("yyyy-MM-dd"));
        formats.add(createDateFormat("yyyyMMdd"));

        for (DateFormat format : formats) {
            format.setTimeZone(UTC_TIME_ZONE);
        }

        return formats;
    }

    @Override
    protected DateTime add(final DateTime original, final int years, final int months, final int days, final int hours, final int minutes) {
        if(hours != 0 || minutes != 0) {
            throw new IllegalArgumentException("cannot add non-zero hours or minutes to a DateTime");
        }
        return original.plusYears(years).plusMonths(months).plusDays(days);
    }

    @Override
    protected DateTime now() {
        return new DateTime();
    }

    @Override
    protected Date dateValue(final Object value) {
        return ((DateTime) value).toDateTime().toDate();
    }

    @Override
    protected DateTime setDate(final Date date) {
        return new DateTime(date.getTime());
    }

    // //////////////////////////////////////////////////////////////////
    // EncoderDecoder
    // //////////////////////////////////////////////////////////////////

    private static final DateTimeFormatter encodingFormatter() {
        return ISODateTimeFormat.basicDateTime();
    }

    @Override
    public String toEncodedString(final DateTime dateTime) {
        return encodingFormatter().print(dateTime);
    }

    @Override
    public DateTime fromEncodedString(final String data) {
        try {
            return encodingFormatter().parseDateTime(data);
        } catch (final IllegalArgumentException e) {
            throw new EncodingException(e);
        }
    }


}
