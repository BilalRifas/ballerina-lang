/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.system.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.stdlib.system.utils.SystemConstants;
import org.ballerinalang.stdlib.system.utils.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Extern function ballerina.system:remove.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = SystemConstants.ORG_NAME,
        packageName = SystemConstants.PACKAGE_NAME,
        functionName = "remove",
        isPublic = true
)
public class Remove extends BlockingNativeCallableUnit {
    private static final String CURRENT_DIR_PROPERTY_KEY = "user.dir";

    @Override
    public void execute(Context context) {
        String inputPath = context.getStringArgument(0);
        boolean recursive = context.getBooleanArgument(0);
        File removeFile = Paths.get(inputPath).toAbsolutePath().toFile();
        BValue wdBValue = SystemUtils.getSystemProperty(CURRENT_DIR_PROPERTY_KEY);
        File wd = Paths.get(wdBValue.stringValue()).toAbsolutePath().toFile();

        try {
            if (wd.getCanonicalPath().equals(removeFile.getCanonicalPath())) {
                context.setReturnValues(SystemUtils.getBallerinaError("INVALID_OPERATION", "Cannot delete the current" +
                            " working directory " + wd.getCanonicalPath()));
                return;
            }

            if (!removeFile.exists()) {
                context.setReturnValues(SystemUtils.getBallerinaError("INVALID_OPERATION",
                        "File doesn't exist in path " + removeFile.getCanonicalPath()));
                return;
            }

            if (recursive) {
                Path directory = Paths.get(removeFile.getCanonicalPath());
                Files.walkFileTree(directory, new RecursiveFileVisitor());
            } else {
                if (!removeFile.delete()) {
                    context.setReturnValues(SystemUtils.getBallerinaError("OPERATION_FAILED",
                            "Error while deleting " + removeFile.getCanonicalPath()));
                    return;
                }
            }
            context.setReturnValues();
        } catch (IOException ex) {
            context.setReturnValues(SystemUtils.getBallerinaError("OPERATION_FAILED", ex));
        } catch (SecurityException ex) {
            context.setReturnValues(SystemUtils.getBallerinaError("PERMISSION_ERROR", ex));
        }
    }

    static class RecursiveFileVisitor extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    }
}
