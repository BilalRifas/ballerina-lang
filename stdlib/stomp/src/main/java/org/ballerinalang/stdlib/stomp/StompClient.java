/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.stomp;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

/**
 * Stomp Client.
 *
 * @since 0.995.0
 */
public abstract class StompClient {
    private final URI uri;

    private Socket socket;
    private String sessionId;

    private Thread readerThread;
    private volatile boolean running = true;

    public StompClient() throws URISyntaxException {
        this("tcp://localhost:61613");
    }

    public StompClient(String url) throws URISyntaxException {
        this(new URI(url));
    }

    public StompClient(URI uri) {
        this.uri = uri;
    }

    // customs handlers.
    public abstract void onConnected(String sessionId);

    public abstract void onDisconnected();

    public abstract void onMessage(String messageId, String body, String destination);

    public abstract void onReceipt(String receiptId);

    public abstract void onError(String message, String description);

    public abstract void onCriticalError(Exception e);

    public void connect() {
        try {
            // connecting to STOMP server
            if (uri.getScheme().equals("tcp")) {
                socket = new Socket(this.uri.getHost(), this.uri.getPort());
            } else if (uri.getScheme().equals("tcps")) {
                SocketFactory socketFactory = SSLSocketFactory.getDefault();
                socket = socketFactory.createSocket(this.uri.getHost(), this.uri.getPort());
            } else {
                throw new StompException("Library is not support this scheme");
            }

            // initialize reader thread.
            readerThread = new Thread(new Runnable() {
                public void run() {
                    reader();
                }
            });

            // run reader thread.
            readerThread.start();

            // sending CONNECT command.
            StompFrame connectFrame = new StompFrame(StompCommand.CONNECT);
            if (uri.getUserInfo() != null) {
                String[] credentials = uri.getUserInfo().split(":");
                if (credentials.length == 2) {
                    connectFrame.header.put("login", credentials[0]);
                    connectFrame.header.put("passcode", credentials[1]);
                }
            }
            send(connectFrame);

            // wait CONNECTED server command.
            synchronized (this) {
                this.wait(5000);
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

    private void reader() {
        try {
            InputStream in = this.socket.getInputStream();
            StringBuilder sb = new StringBuilder();
            while (running) {
                try {
                    sb.setLength(0);
                    int ch;

                    // skip lead trash.
                    do {
                        ch = in.read();
                        if (ch < 0) {
                            onCriticalError(new IOException("stomp server disconnected!"));
                            return;
                        }
                    } while (ch < 'A' || ch > 'Z');

                    // read frame.
                    do {
                        sb.append((char) ch);
                    } while ((ch = in.read()) != 0);

                    // parsing raw data to StompFrame format.
                    StompFrame frame = StompFrame.parse(sb.toString());

                    // run handlers.
                    switch (frame.command) {
                        case CONNECTED:
                            // unblock connect().
                            synchronized (this) {
                                this.notifyAll();
                            }
                            sessionId = frame.header.get("session");
                            onConnected(sessionId);
                            break;
                        case DISCONNECTED:
                            onDisconnected();
                            break;
                        case RECEIPT:
                            String receiptId = frame.header.get("receipt-id");
                            onReceipt(receiptId);
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
        send(frame);
    }

    public void ack(String messageId) {
        StompFrame frame = new StompFrame(StompCommand.ACK);
        frame.header.put("message-id", messageId);
        send(frame);
    }

    public void ack(String messageId, String transaction) {
        StompFrame frame = new StompFrame(StompCommand.ACK);
        frame.header.put("message-id", messageId);
        frame.header.put("transaction", transaction);
        send(frame);
    }

    private synchronized void send(StompFrame frame) {
        try {
            socket.getOutputStream().write(frame.getBytes());
        } catch (IOException e) {
            StompException ex = new StompException("Problem with sending frame");
            ex.initCause(e);
            throw ex;
        }
    }

}
