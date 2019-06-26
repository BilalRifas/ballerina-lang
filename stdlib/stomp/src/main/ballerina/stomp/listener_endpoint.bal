// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

public type Listener object {

  *AbstractListener;

  public ListenerConfig config = { };

    public function __init(ListenerConfig stompConfig ) {
        self.config = stompConfig;
        var result = self.initListener(stompConfig);
        if (result is error) {
            panic result;
        }
    }

    public function __start() returns error? {
        return self.start();
    }

    public function __stop() returns error? {
        return self.stop();
    }

    public function __attach(service s, string? name = ()) returns error? {
        return self.register(s, name);
    }

    public function initListener(ListenerConfig config) returns error? = external;

    public function register(service s, string? name) returns error? = external;

    public function start() returns error? = external;

    public function stop() returns error? = external;
};

public type StompServiceConfig record {
    string destination = "";
    AckType ackMode = AUTO;
    boolean durable = false;
    string durableId = "";
};

public annotation<service> ServiceConfig StompServiceConfig;

# Represents the socket server configuration.
#
# + host - host to connect the tcp socket.
# + port - port to connect the tcp socket.
# + username - the login username for broker.
# + password - the password for broker.
# + vhost - virtual host.
# + acceptVersion - accept version supported by broker & listener.
public type ListenerConfig record {
    string host = "";
    int port = 0;
    string username  = "";
    string password = "";
    string vhost = "";
    string acceptVersion = "";
};
