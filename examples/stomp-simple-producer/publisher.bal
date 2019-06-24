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
        string destination = "/queue/sports";
        map<string> customHeaderMap = {};
        var publish = stompSender->send(message,destination,customHeaderMap);
        var disconnect = stompSender->disconnect();
}
