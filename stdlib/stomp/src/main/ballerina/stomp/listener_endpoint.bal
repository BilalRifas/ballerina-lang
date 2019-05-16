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
    string ackMode = "AUTO";
    boolean durable = false;
    boolean autoDelete = false;
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

