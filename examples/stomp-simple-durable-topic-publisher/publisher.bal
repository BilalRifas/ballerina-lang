// This is the publisher implementation for STOMP protocol.
import ballerina/stomp;

// This initializes a STOMP connection with the STOMP broker.
stomp:Sender stompSender = new({
        host: "localhost",
        port: 61613,
        username: "guest",
        password: "guest",
        vhost: "/",
        acceptVersion: "1.1"
    });

public function main() {
        // This sends the Ballerina message to the stomp broker.
        string message = "Hello World From Ballerina";
        string destination = "/topic/my-durable";
        map<string> customHeaderMap = {};
        customHeaderMap["persistent"] = "persistent:true";
        var publish = stompSender->send(message,destination,customHeaderMap);
        var disconnect = stompSender->disconnect();
}
