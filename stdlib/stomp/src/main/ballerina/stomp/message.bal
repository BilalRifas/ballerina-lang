import ballerina/io;
import ballerina/log;

public type Message object {
    private string content = "";

    # Get message content.
    #
    # + return - message content as a 'string' liternal.
    public function getContent() returns string {
        return self.content;
    }

    public function ack() returns error? = external;
};


