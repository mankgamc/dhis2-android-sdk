/*
 * Copyright (c) 2015, University of Oslo
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

package org.hisp.dhis.android.sdk.organisationunit;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.hisp.dhis.android.sdk.common.base.AbsIdentifiableObjectStore;
import org.hisp.dhis.android.sdk.common.base.IMapper;
import org.hisp.dhis.android.sdk.flow.DataSet$Flow;
import org.hisp.dhis.android.sdk.flow.OrganisationUnit$Flow;
import org.hisp.dhis.android.sdk.flow.UnitToDataSetRelationShip$Flow;
import org.hisp.dhis.android.sdk.flow.UnitToDataSetRelationShip$Flow$Table;
import org.hisp.dhis.java.sdk.models.dataset.DataSet;
import org.hisp.dhis.java.sdk.organisationunit.IOrganisationUnitStore;
import org.hisp.dhis.java.sdk.models.organisationunit.OrganisationUnit;

import java.util.ArrayList;
import java.util.List;

public final class OrganisationUnitStore extends AbsIdentifiableObjectStore<OrganisationUnit, OrganisationUnit$Flow> implements IOrganisationUnitStore {

    public OrganisationUnitStore(IMapper<OrganisationUnit, OrganisationUnit$Flow> mapper) {
        super(mapper);
    }

    @Override
    public List<DataSet> query(OrganisationUnit organisationUnit) {
        List<UnitToDataSetRelationShip$Flow> relationShipFlows = new Select()
                .from(UnitToDataSetRelationShip$Flow.class)
                .where(Condition.column(UnitToDataSetRelationShip$Flow$Table
                        .ORGANISATIONUNIT_ORGANISATIONUNIT).is(organisationUnit.getUId()))
                .queryList();

        List<DataSet> dataSets = new ArrayList<>();
        for (UnitToDataSetRelationShip$Flow relationShip : relationShipFlows) {
            DataSet$Flow dataSetFlow = relationShip.getDataSet();
            dataSets.add(DataSet$Flow.toModel(dataSetFlow));
        }

        return dataSets;
    }
}