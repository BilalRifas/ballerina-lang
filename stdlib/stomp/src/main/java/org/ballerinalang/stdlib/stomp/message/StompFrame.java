package org.ballerinalang.stdlib.stomp.message;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
/**
 * Stomp server frame creation.
 *
 * @since 0.995.0
 */
public class StompFrame {

    public StompCommand command;
    public Map<String, String> header = new HashMap<>();
    public String body;

    public StompFrame() {
    }

    public StompFrame(StompCommand command) {
        this.command = command;
    }

    public String toString() {
        return String.format("command: %s, header: %s, body: %s", this.command,
                this.header.toString(), this.body);
    }

    public byte[] getBytes() {
        StringBuilder frame = new StringBuilder(this.command.toString() + '\n');
        for (Map.Entry<String, String> entry : this.header.entrySet()) {
            String key = entry.getKey();
            frame.append(key).append(":").append(this.header.get(key)).append('\n');
        }

        frame.append('\n');

        if (this.body != null) {
            frame.append(this.body);
        }
        frame.append("\0");
        return frame.toString().getBytes(Charset.forName("UTF-8"));
    }

    public static StompFrame parse(String raw) {
        StompFrame frame = new StompFrame();

        String commandheaderSections = raw.split("\n\n")[0];
        String[] headerLines = commandheaderSections.split("\n");

        frame.command = StompCommand.valueOf(headerLines[0]);

        for (int i = 1; i < headerLines.length; i++) {
            String key = headerLines[i].split(":")[0];
            frame.header.put(key, headerLines[i].substring(key.length() + 1));
        }

        frame.body = raw.substring(commandheaderSections.length() + 2);

        return frame;
    }

}
