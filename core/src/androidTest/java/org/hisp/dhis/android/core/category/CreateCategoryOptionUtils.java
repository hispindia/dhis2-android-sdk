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

package org.hisp.dhis.android.core.category;

import android.content.ContentValues;

public class CreateCategoryOptionUtils {

    public static final String TEST_CODE = "test_code";
    public static final String TEST_NAME = "test_name";
    public static final String TEST_DISPLAY_NAME = "test_display_name";
    public static final String TEST_CREATED = "2001-02-07T16:04:40.387";
    public static final String TEST_LAST_UPDATED = "2001-02-07T16:04:40.387";

    public static ContentValues create(long id, String uid) {
        ContentValues categoryOption = new ContentValues();
        categoryOption.put(CategoryOptionModel.Columns.ID, id);
        categoryOption.put(CategoryOptionModel.Columns.UID, uid);
        categoryOption.put(CategoryOptionModel.Columns.CODE, TEST_CODE);
        categoryOption.put(CategoryOptionModel.Columns.NAME, TEST_NAME);
        categoryOption.put(CategoryOptionModel.Columns.DISPLAY_NAME, TEST_DISPLAY_NAME);
        categoryOption.put(CategoryOptionModel.Columns.CREATED, TEST_CREATED);
        categoryOption.put(CategoryOptionModel.Columns.LAST_UPDATED, TEST_LAST_UPDATED);
        return categoryOption;
    }
}
