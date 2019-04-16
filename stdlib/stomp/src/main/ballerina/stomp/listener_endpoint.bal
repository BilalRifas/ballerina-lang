import ballerina/io;
import ballerina/log;
import ballerina/runtime;

public type Listener object {

  *AbstractListener;

  public ListenerConfig config = { };

    // TODO return the error for Init as well, from new release
    public function __init(ListenerConfig stompConfig ){
        self.config = stompConfig;
        var result = self.initListener(stompConfig);
        if (result is error) {
            panic result;
        }
    }

    public function __start() returns error? {
       // ignored
    }

    public function __stop() returns error? {
        return self.stop();
    }

    public function __attach(service s, map<any> annotationData) returns error? {
        return self.register(s, annotationData);
    }

    extern function initListener(ListenerConfig config) returns error?;

    extern function register(service s, map<any> annotationData) returns error?;

    extern function start() returns error?;

    extern function stop() returns error?;
};

public type StompServiceConfig record {
    string destination = "/queue/test";
    string ackMode = "AUTO";
    string durable = "false";
    string autoDelete = "false";
};

public annotation<service> ServiceConfig StompServiceConfig;

# Represents the socket server configuration.
#
# + host - host to connect the tcp socket
# + port - port to connect the tcp socket
# + login - the login username for broker
# + passcode - the password for broker
# + vhost - virtual host
# + acceptVersion - accept version supported by broker & listener
public type ListenerConfig record {
    string host = "";
    int port = 0;
    string login  = "";
    string passcode = "";
    string vhost = "";
    string acceptVersion = "";
};

