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

package org.hisp.dhis.android.core.dataset;

import android.support.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import org.hisp.dhis.android.core.common.PeriodType;
import org.hisp.dhis.android.core.data.api.Field;

import java.util.Date;

@AutoValue
public abstract class Period {
    private static final String PERIOD_TYPE = "periodType";
    private static final String START_DATE = "startDate";
    private static final String END_DATE = "endDate";

    public static final Field<Period, PeriodType> periodType = Field.create(PERIOD_TYPE);
    public static final Field<Period, Date> startDate = Field.create(START_DATE);
    public static final Field<Period, Date> endDate = Field.create(END_DATE);

    @Nullable
    @JsonProperty(PERIOD_TYPE)
    public abstract PeriodType periodType();

    @Nullable
    @JsonProperty(START_DATE)
    public abstract Date startDate();

    @Nullable
    @JsonProperty(END_DATE)
    public abstract Date endDate();

    @JsonCreator
    public static Period create(
            @JsonProperty(PERIOD_TYPE) PeriodType periodType,
            @JsonProperty(START_DATE) Date startDate,
            @JsonProperty(END_DATE) Date endDate) {

        return new AutoValue_Period(periodType, startDate, endDate);

    }
}
