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

# Defines the possible values for the ack type in STOMP `ACKNOWLEDGEMENT`.
#
# `auto`: ACK type auto
# `client`: ACK type client
# `client-individual`: ACK type client-individual
public type AckType AUTO|CLIENT|CLIENT_INDIVIDUAL;

# Constant for STOMP ack type auto.
public const AUTO = "auto";

# Constant for STOMP ack type client.
public const CLIENT = "client";

# Constant for STOMP ack type client-individual.
public const CLIENT_INDIVIDUAL = "client-individual";


