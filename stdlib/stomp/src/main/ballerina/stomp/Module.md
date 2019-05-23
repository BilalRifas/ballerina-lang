## Module overview

This module provides the capability to connect with a Stomp broker using the [Core API](https://stomp.github.io/stomp-specification-1.2.html).

## Samples

### Stomp Producer

The following program produces message to the Stomp broker.

```ballerina
import ballerina/stomp;

stomp:Sender stompSender = new({
        host: "localhost",
        port: 61613,
        login: "guest",
        passcode: "guest",
        vhost: "/",
        acceptVersion: "1.1"
    });

public function main() {
        // This sends the Ballerina message to the stomp broker.
        string message = "Hello World From Ballerina";
        string destination = "/queue/sports";
        var publish = stompSender->send(message,destination);
        var disconnect = stompSender->disconnect();
}
```
### Stomp consumer

The following program will consume a message from the Stomp broker.

```ballerina
import ballerina/stomp;
import ballerina/log;

listener stomp:Listener consumerEndpoint = new({
        host: "localhost",
        port: 61613,
        login: "guest",
        passcode: "guest",
        vhost: "/",
        acceptVersion: "1.1"
    });

@stomp:ServiceConfig{
        destination:"/queue/sports",
        ackMode: stomp:AUTO
}

service stompListenerSports on consumerEndpoint  {
    // This resource is invoked when a message is received.
    // Message object only gives us the string message.
    resource function onMessage(stomp:Message message) {
        var content = message.getContent();
        log:printInfo(content);
    }

    // This resource is invoked when the connection is interrupted.
    resource function onError(error er) {
        log:printError("An error occured", err = er);
    }
}
```