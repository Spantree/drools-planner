/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.planner.examples.nurserostering;

import java.io.File;

import org.drools.planner.config.EnvironmentMode;
import org.drools.planner.examples.common.app.SolverPerformanceTest;
import org.drools.planner.examples.common.persistence.SolutionDao;
import org.drools.planner.examples.nurserostering.persistence.NurseRosteringDaoImpl;
import org.junit.Test;

public class NurseRosteringPerformanceTest extends SolverPerformanceTest {

    @Override
    protected String createSolverConfigResource() {
        return "/org/drools/planner/examples/nurserostering/solver/nurseRosteringSolverConfig.xml";
    }

    @Override
    protected SolutionDao createSolutionDao() {
        return new NurseRosteringDaoImpl();
    }

    // ************************************************************************
    // Tests
    // ************************************************************************

    @Test(timeout = 600000)
    public void solveMedium_late01_initialized() {
        File unsolvedDataFile = new File("data/nurserostering/unsolved/medium_late01_initialized.xml");
        runSpeedTest(unsolvedDataFile, "-45hard/-3335soft");
    }

    @Test(timeout = 600000)
    public void solveMedium_late01_initializedDebug() {
        File unsolvedDataFile = new File("data/nurserostering/unsolved/medium_late01_initialized.xml");
        runSpeedTest(unsolvedDataFile, "-57hard/-3387soft", EnvironmentMode.DEBUG);
    }

}
