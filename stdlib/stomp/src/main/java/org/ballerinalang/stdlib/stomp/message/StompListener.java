package org.ballerinalang.stdlib.stomp.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URI;
import java.util.UUID;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
/**
 * Stomp Client.
 *
 * @since 0.995.0
 */
public abstract class StompListener {
    private static final Logger log = LoggerFactory.getLogger(StompListener.class);
    private final URI connectionUri;
    private Thread readerThread;
    private volatile boolean running = true;
    private Socket socketConnection;
    private String sessionId;
    private String clientUuid;

    public StompListener(URI uri) {
        this.connectionUri = uri;
    }

    public abstract void onConnected(String sessionId);

    public abstract void onMessage(String messageId, String body, String destination);

    public abstract void onError(String message, String description);

    public abstract void onCriticalError(Exception e);

    public void connect() {
        try {
            // Connecting to STOMP broker.
            if (connectionUri.getScheme().equals("tcp")) {
                socketConnection = new Socket(this.connectionUri.getHost(), this.connectionUri.getPort());
            } else if (connectionUri.getScheme().equals("tcps")) {
                SocketFactory socketFactory = SSLSocketFactory.getDefault();
                socketConnection = socketFactory.createSocket(
                        this.connectionUri.getHost(), this.connectionUri.getPort());
            } else {
                throw new StompException("Library is not support this scheme");
            }

            // Initialize reader thread.
            readerThread = new Thread(new Runnable() {
                public void run() {
                    reader();
                }
            });

            // Start reader thread.
            readerThread.start();

            String uuid = UUID.randomUUID().toString().replace("-", "");
            // Sending CONNECT command.
            StompFrame connectionFrame = new StompFrame(StompCommand.CONNECT);
            if (connectionUri.getUserInfo() != null) {
                String[] credentials = connectionUri.getUserInfo().split(":");
                if (credentials.length == 2) {
                    connectionFrame.header.put("login", credentials[0]);
                    connectionFrame.header.put("passcode", credentials[1]);
                    connectionFrame.header.put("client-id", uuid);
                    this.clientUuid = uuid;
                }
            }

            sendFrame(connectionFrame);

            // Wait for CONNECTED broker command.
            synchronized (this) {
                wait();
            }

        } catch (StompException ex) {
            ex.initCause(ex);
        } catch (Exception e) {
            try {
                throw e;
            } catch (IOException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

//    public void retryConnect() {
//        int retryCount;
//        int countLimit = 3;
//
//        for (retryCount = 0; retryCount < countLimit; retryCount++) {
//            this.connect();
//        }
//    }

    private void reader() {
        try {
            InputStream inputStream = this.socketConnection.getInputStream();
            StringBuilder stringBuilder = new StringBuilder();
            while (running) {
                try {
                    stringBuilder.setLength(0);
                    int character;

                    // Skip lead trash.
                    do {
                        character = inputStream.read();
                        if (character < 0) {
                            onCriticalError(new IOException("stomp server disconnected!"));
                            return;
                        }
                    } while (character < 'A' || character > 'Z');

                    // Read frame.
                    do {
                        stringBuilder.append((char) character);
                    } while ((character = inputStream.read()) != 0);

                    // Parsing raw data to StompFrame format.
                    StompFrame frame = StompFrame.parse(stringBuilder.toString());

                    // Run handlers.
                    switch (frame.command) {
                        case CONNECTED:
                            synchronized (this) {
                                notify();
                            }
                            sessionId = frame.header.get("session");
                            onConnected(sessionId);
                            break;
                        case MESSAGE:
                            String messageId = frame.header.get("message-id");
                            String messageDestination = frame.header.get("destination");
                            onMessage(messageId, frame.body, messageDestination);
                            break;
                        case ERROR:
                            String message = frame.header.get("message");
                            onError(message, frame.body);
                            break;
                        default:
                            break;
                    }
                } catch (IOException e) {
                    onCriticalError(e);
                    return;
                }
            }
        } catch (IOException e) {
            onCriticalError(e);
            return;
        }
    }

    public void subscribe(String destination, String ack) {
        StompFrame frame = new StompFrame(StompCommand.SUBSCRIBE);
        frame.header.put("destination", destination);
        frame.header.put("session", sessionId);
        frame.header.put("ack", ack);
        sendFrame(frame);
    }

    public void durableSubscribe(String destination, String ack) {
        StompFrame frame = new StompFrame(StompCommand.SUBSCRIBE);
        frame.header.put("destination", destination);
        frame.header.put("session", sessionId);
        frame.header.put("ack", ack);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        frame.header.put("id", uuid);
        frame.header.put("durable", "true");
        frame.header.put("auto-delete", "false");
        frame.header.put("client-id", this.clientUuid);
        sendFrame(frame);
    }

    public void acknowledge(String messageId) {
        StompFrame frame = new StompFrame(StompCommand.ACK);
        frame.header.put("message-id", messageId);
        sendFrame(frame);
    }

    private synchronized void sendFrame(StompFrame frame) {
        try {
            socketConnection.getOutputStream().write(frame.getBytes());
        } catch (IOException e) {
            StompException ex = new StompException("Problem with sending frame");
            ex.initCause(e);
            throw ex;
        }
    }
}
