import ballerina/http;
import ballerina/log;
import ballerina/io;

listener http:Listener helloListener = new(9091);
@http:ServiceConfig {
    basePath: "/"
}
service hello on helloListener {

    @http:ResourceConfig {
        path: "/remote",
        methods: ["POST"]
    }
    resource function sayHello(http:Caller caller, http:Request req) {
        json resp = {"hello": "Bilal"};
        var result = caller->respond(resp);
        log:printInfo("Your response is successfully passed");
        io:println(resp);

        if (result is error) {
            log:printError("Error sending response", err = result);
        }
    }
}
