import ballerina/io;
import ballerina/stomp;
import ballerina/log;
import ballerina/time;

stomp:Sender sensors = new({
        host: "localhost",
        port: 61613,
        login: "guest",
        passcode: "guest",
        vhost: "/",
        acceptVersion: "1.1"
    });

public function main() {
    boolean motionDetector = false;
    boolean smokeDetector = false;
    boolean heatDetector = false;
    int motionCount = 0;
    int smokeCount = 0;
    int heatCount = 0;
    string motionDestination = "/queue/motionDetector";
    string smokeDestination = "/queue/smokeDetector";
    string heatDestination = "/queue/heatDetector";

        while (motionCount < 3) {
            motionCount= motionCount + 1;
            io:println(motionCount);
        }
        io:println(motionCount);
        motionDetector = true;
        io:println("Motion Detected: " + motionDetector);
        var motionAlert = sensors->send("motion",motionDestination);

        while (smokeCount < 3) {if (publicService == null || branch == null || contact == null || ownerName == null) {
                    response.statusCode = 400;
                    response.setJsonPayload({ "Message": "Bad Request - Invalid payload" });
                    checkpanic caller->respond(response);
                    return;
        }
            smokeCount= smokeCount + 1;
            io:println(smokeCount);
        }
        io:println(smokeCount);
        smokeDetector = true;
        io:println("Smoke Detected: " + smokeDetector);
        var smokeAlert = sensors->send("smoke",smokeDestination);

        while (heatCount < 3) {
            heatCount= heatCount + 1;
            io:println(heatCount);
        }
        io:println(heatCount);
        heatDetector = true;
        io:println("Heat Detected: " + heatDetector);
        var heatAlert = sensors->send("heat",heatDestination);

    var disconnect = sensors->disconnect();
}
