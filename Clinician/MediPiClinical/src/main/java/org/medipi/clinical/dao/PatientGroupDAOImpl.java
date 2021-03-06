/*
 Copyright 2016  Richard Robinson @ NHS Digital <rrobinson@nhs.net>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.medipi.clinical.dao;

import java.util.List;
import org.medipi.clinical.entities.PatientGroup;
import org.springframework.stereotype.Repository;

/**
 * Implementation of Data Access Object for Patient
 * @author rick@robinsonhq.com
 */
@Repository
public class PatientGroupDAOImpl extends GenericDAOImpl<PatientGroup> implements PatientGroupDAO {
    @Override
    public List<PatientGroup> getAllGroups() {
        return this.getEntityManager().createNamedQuery("PatientGroup.findAll", PatientGroup.class)
                .getResultList();
    }
}