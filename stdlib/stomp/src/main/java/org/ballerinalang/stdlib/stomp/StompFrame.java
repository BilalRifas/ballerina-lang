/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.HashMap;
import java.util.Map;

/**
 * Initialize the server stomp endpoint.
 *
 * @since 0.990.2
 */
public class StompFrame {

public StompCommand command;
public Map<String, String> header = new HashMap<String, String>();
public String body;

/** constructor.
*  
*/
public StompFrame() {
}

/** constructor.
* @param command type of frame 
*/
public StompFrame(StompCommand command) {
this.command = command;
}

public String toString() {
return String.format("command: %s, header: %s, body: %s", this.command,
this.header.toString(), this.body);
}

/** getBytes convert frame object to array of bytes.
* @return array of bytes.
*/
public byte[] getBytes() {
String frame = this.command.toString() + '\n';
for (String key : this.header.keySet()) {
frame += key + ":" + this.header.get(key) + '\n';
}
frame += '\n';

if (this.body != null) {
frame += this.body;
}
frame += "\0";
return frame.getBytes();
}

/** parse string to frame object.
* @param raw frame as string.
* @return frame object.
*/
public static StompFrame parse(String raw) {
StompFrame frame = new StompFrame();

String commandheaderSections = raw.split("\n\n")[0];
String[] headerLines = commandheaderSections.split("\n");

frame.command = StompCommand.valueOf(headerLines[0]);

for (int i = 1; i < headerLines.length; i++) {
String key = headerLines[i].split(":")[0];
frame.header.put(key, headerLines[i].substring(key.length() + 1));
}

frame.body = raw.substring(commandheaderSections.length() + 2);

return frame;
}

}
