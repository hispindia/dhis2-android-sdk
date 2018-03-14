/*
 * Copyright (c) 2017, University of Oslo
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.core.utils;

import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.BaseIdentifiableObjectModel;
import org.hisp.dhis.android.core.common.BaseModel;
import org.hisp.dhis.android.core.common.BaseNameableObjectModel;

import java.text.ParseException;
import java.util.Date;

/**
 * A collection of convenience functions/abstractions to be used by the tests.
 */
public class FillPropertiesTestUtils {
    public static final String UID = "test_uid";
    public static final String CODE = "test_code";
    public static final String NAME = "test_name";
    public static final String DISPLAY_NAME = "test_display_name";
    public static final Date CREATED = parseDate("2012-10-20T18:20:27.132");
    public static final Date LAST_UPDATED = parseDate("2017-12-20T15:08:27.882");

    public static final String SHORT_NAME = "test_short_name";
    public static final String DISPLAY_SHORT_NAME = "test_display_short_name";
    public static final String DESCRIPTION = "test_description";
    public static final String DISPLAY_DESCRIPTION = "test_display_description";

    public static final Date START_DATE = parseDate("2012-10-20T18:20:27.132");
    public static final Date END_DATE = parseDate("2017-12-20T15:08:27.882");

    public static final Boolean IS_DEFAULT = false;

    public static final boolean DELETED = false;

    private static Date parseDate(String dateStr) {
        try {
            return BaseIdentifiableObject.DATE_FORMAT.parse(dateStr);
        } catch (ParseException e) {
            return new Date();
        }
    }

    private static void fillModelProperties(BaseModel.Builder builder) {
        /*builder
                .id(2L);*/
    }

    public static void fillIdentifiableModelProperties(BaseIdentifiableObjectModel.Builder builder) {
        fillModelProperties(builder);
        builder
                .uid(UID)
                .code(CODE)
                .name(NAME)
                .displayName(DISPLAY_NAME)
                .created(CREATED)
                .lastUpdated(LAST_UPDATED);
    }

    public static void fillNameableModelProperties(BaseNameableObjectModel.Builder builder) {
        fillIdentifiableModelProperties(builder);
        builder
                .shortName(SHORT_NAME)
                .displayShortName(DISPLAY_SHORT_NAME)
                .description(DESCRIPTION)
                .displayDescription(DISPLAY_DESCRIPTION);
    }
}
