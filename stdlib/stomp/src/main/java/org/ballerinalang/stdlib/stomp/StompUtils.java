/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.stomp;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
/**
 * Utility class for Stomp.
 *
 * @since 0.995.0
 */
public class StompUtils {

    private static final String STOMP_ERROR_CODE = "{ballerina/stomp}StompError";
    private static final String STOMP_ERROR = "StompError";

    public static BError createStompError(Context context, String errMsg) {
        BMap<String, BValue> errorRecord = BLangConnectorSPIUtil.createBStruct(context,
                StompConstants.STOMP_PACKAGE, STOMP_ERROR);
        errorRecord.put("message", new BString(errMsg));
        return BLangVMErrors.createError(context, true, BTypes.typeError, STOMP_ERROR_CODE, errorRecord);
    }

    public static BError createStompError(ProgramFile programFile, String errMsg) {
        BMap<String, BValue> errorRecord = BLangConnectorSPIUtil
                .createBStruct(programFile, StompConstants.STOMP_PACKAGE, STOMP_ERROR);
        errorRecord.put("message", new BString(errMsg));
        return BLangVMErrors.createError(STOMP_ERROR_CODE, errorRecord);
    }

    /**
     * GetError
     * @param context Represent ballerina context
     * @param errMsg  Error message
     *
     * return BLangVMErrors
     */
    static BError getError(Context context, String errMsg) {
        BMap<String, BValue> stompErrorRecord = createStompErrorRecord(context);
        stompErrorRecord.put(StompConstants.STOMP_ERROR_MESSAGE, new BString(errMsg));
        return BLangVMErrors.createError(context, true, BTypes.typeError, StompConstants.STOMP_ERROR_CODE,
                stompErrorRecord);
    }

    private static BMap<String, BValue> createStompErrorRecord(Context context) {
        return BLangConnectorSPIUtil.createBStruct(context, StompConstants.STOMP_PACKAGE,
                StompConstants.STOMP_ERROR_RECORD);
    }

    public static BError getError(Context context, Exception exception) {
        if (exception.getMessage() == null) {
            return getError(context, "Stomp error");
        } else {
            return getError(context, exception.getMessage());
        }
    }
}
