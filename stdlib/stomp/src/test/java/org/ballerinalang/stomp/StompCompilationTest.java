/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.messaging.artemis;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test Stomp Listener Service Compilation.
 *
 * @since 0.995.0
 */
public class StompCompilationTest {
    private static final Path TEST_PATH = Paths.get("src", "test", "resources", "test-src");

    @Test(description = "Successfully compiling Stomp service")
    public void testValidService() {
        CompileResult compileResult = BCompileUtil.compile(TEST_PATH.resolve("stomp_success.bal").toAbsolutePath()
                                                                   .toString());

        Assert.assertEquals(compileResult.toString(), "Compilation Successful");
    }

    private void assertExpectedDiagnosticsLength(CompileResult compileResult) {
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
    }
}
