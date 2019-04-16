import ballerina/io;
import ballerina/log;

public type Message object {

    // TODO change to get Text
    public extern function getContent() returns string|error;

    // TODO add ack method
};

public type MessageConfiguration record {

};

