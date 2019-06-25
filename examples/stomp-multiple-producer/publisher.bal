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
        string message1 = "Hello World From Ballerina - Sports";
        string message2 = "Hello World From Ballerina - News";
        string destination1 = "/queue/sports";
        string destination2 = "/queue/news";
        map<string> customHeaderMap = {};
        var publish1 = stompSender->send(message1,destination1,customHeaderMap);
        var publish2 = stompSender->send(message2,destination2,customHeaderMap);
        var disconnect = stompSender->disconnect();
}
