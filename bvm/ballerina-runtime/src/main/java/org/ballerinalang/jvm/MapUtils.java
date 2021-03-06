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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.jvm;

import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.jvm.util.exceptions.RuntimeErrors;
import org.ballerinalang.jvm.values.MapValue;

/**
 * Common utility methods used for MapValue insertion/manipulation.
 *
 * @since 0.995.0
 */
public class MapUtils {

    public static void handleMapStore(MapValue<String, Object> mapValue, String fieldName, Object value) {
        BType mapType = mapValue.getType();
        BType valuesType = value == null ? BTypes.typeNull : TypeChecker.getType(value);

        switch (mapType.getTag()) {
            case TypeTags.MAP_TAG:
                if (!TypeChecker.checkIsType(value, ((BMapType) mapType).getConstrainedType())) {
                    BType expType = ((BMapType) mapType).getConstrainedType();
                    throw BallerinaErrors.createError(BallerinaErrorReasons.INHERENT_TYPE_VIOLATION_ERROR,
                            BLangExceptionHelper.getErrorMessage(RuntimeErrors.INVALID_MAP_INSERTION, expType,
                                    valuesType));
                }
                mapValue.put(fieldName, value);
                break;
            case TypeTags.RECORD_TYPE_TAG:
                BRecordType recType = (BRecordType) mapType;
                BField recField = recType.getFields().get(fieldName);
                BType recFieldType;

                if (recField != null) {
                    // If there is a corresponding field in the record, use it
                    recFieldType = recField.type;
                } else if (recType.restFieldType != null) {
                    // If there isn't a corresponding field, but there is a rest field, use it
                    recFieldType = recType.restFieldType;
                } else {
                    // If both of the above conditions fail, the implication is that this is an attempt to insert a
                    // value to a non-existent field in a closed record.
                    throw BallerinaErrors.createError(BallerinaErrorReasons.KEY_NOT_FOUND_ERROR,
                            BLangExceptionHelper.getErrorMessage(RuntimeErrors.INVALID_RECORD_FIELD_ACCESS, fieldName,
                                    recType));
                }

                if (!TypeChecker.checkIsType(value, recFieldType)) {
                    throw BallerinaErrors.createError(BallerinaErrorReasons.INHERENT_TYPE_VIOLATION_ERROR,
                            BLangExceptionHelper.getErrorMessage(RuntimeErrors.INVALID_RECORD_FIELD_ADDITION, fieldName,
                                    recFieldType, valuesType));
                }

                mapValue.put(fieldName, value);
                break;
        }
    }
}
