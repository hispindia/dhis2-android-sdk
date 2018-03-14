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
package org.hisp.dhis.android.core.dataelement;

import org.hisp.dhis.android.core.common.GenericHandler;
import org.hisp.dhis.android.core.common.GenericHandlerImpl;
import org.hisp.dhis.android.core.common.IdentifiableObjectStore;
import org.hisp.dhis.android.core.data.database.DatabaseAdapter;
import org.hisp.dhis.android.core.option.OptionSet;
import org.hisp.dhis.android.core.option.OptionSetModel;

public class DataElementHandler extends GenericHandlerImpl<DataElement, DataElementModel> {
    private final GenericHandler<OptionSet, OptionSetModel> optionSetHandler;

    DataElementHandler(IdentifiableObjectStore<DataElementModel> dataSetStore,
                       GenericHandler<OptionSet, OptionSetModel> optionSetHandler) {
        super(dataSetStore);
        this.optionSetHandler = optionSetHandler;
    }

    @Override
    protected DataElementModel pojoToModel(DataElement dataElement) {
        return DataElementModel.factory.fromPojo(dataElement);
    }

    public static DataElementHandler create(DatabaseAdapter databaseAdapter,
                                            GenericHandler<OptionSet, OptionSetModel> optionSetHandler) {
        return new DataElementHandler(DataElementStore.create(databaseAdapter), optionSetHandler);
    }

    @Override
    protected void afterObjectPersisted(DataElement dateElement) {
        optionSetHandler.handle(dateElement.optionSet());
    }
}