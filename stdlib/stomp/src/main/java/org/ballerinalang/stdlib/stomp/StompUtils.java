package org.ballerinalang.stdlib.stomp;

import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.bre.Context;

import static org.ballerinalang.stdlib.stomp.StompConstants.STOMP_PACKAGE;

public class StompUtils {

    private static final String STOMP_ERROR_CODE = "{ballerina/stomp}StompError";
    private static final String STOMP_ERROR = "StompError";
    /**
     * Get error struct.
     *
     * @param context Represent ballerina context
     * @param errMsg  Error message
     * @return Error struct
     */
    public static BError getError(Context context, String errMsg) {
        BMap<String, BValue> artemisErrorRecord = createStompErrorRecord(context);
        artemisErrorRecord.put(StompConstants.STOMP_ERROR_MESSAGE, new BString(errMsg));
        return BLangVMErrors.createError(context, true, BTypes.typeError, StompConstants.STOMP_ERROR_CODE,
                artemisErrorRecord);
    }

    private static BMap<String, BValue> createStompErrorRecord(Context context) {
        return BLangConnectorSPIUtil.createBStruct(context, STOMP_PACKAGE,
                StompConstants.STOMP_ERROR_RECORD);
    }

    public static BError getError(Context context, Exception exception) {
        if (exception.getMessage() == null) {
            return getError(context, "Artemis connector error");
        } else {
            return getError(context, exception.getMessage());
        }
    }

    public static BError createSocketError(ProgramFile programFile, String errMsg) {
        BMap<String, BValue> errorRecord = BLangConnectorSPIUtil
                .createBStruct(programFile, STOMP_PACKAGE, STOMP_ERROR);
        errorRecord.put("message", new BString(errMsg));
        return BLangVMErrors.createError(STOMP_ERROR_CODE, errorRecord);
    }


}
