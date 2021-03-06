/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * This server socket will use to mock the backend server.
 */
public class MockSocketServer implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(MockSocketServer.class);

    static final int SERVER_PORT = 47826;
    static final String SERVER_HOST = "localhost";
    private String receivedString;
    private boolean execute = true;
    private Selector selector = null;

    private synchronized void answerWithEcho(ByteBuffer buffer, SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        final int read = client.read(buffer);
        if (read == -1) {
            client.close();
            return;
        }
        byte[] readBytes = buffer.array();
        receivedString = new String(readBytes, StandardCharsets.UTF_8.name()).trim();
        buffer.flip();
        client.write(buffer);
        buffer.clear();
    }

    private static void register(Selector selector, ServerSocketChannel serverSocket) throws IOException {
        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }

    String getReceivedString() {
        return receivedString;
    }

    @Override
    public void run() {
        try {
            selector = Selector.open();
            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress("localhost", SERVER_PORT));
            serverSocket.configureBlocking(false);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
            ByteBuffer buffer = ByteBuffer.allocate(256);

            while (execute) {
                try {
                    final int select = selector.select();
                    if (select == 0) {
                        continue;
                    }
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iter = selectedKeys.iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();
                        if (key.isAcceptable()) {
                            register(selector, serverSocket);
                        } else if (key.isReadable()) {
                            answerWithEcho(buffer, key);
                        }
                    }
                } catch (Throwable e) {
                    log.error("Error in MockSocketServer loop: " + e.getMessage());
                }
            }
            serverSocket.close();
        } catch (Throwable e) {
            log.error(e.getMessage());
        }
    }

    void stop() {
        execute = false;
        if (selector == null) {
            return;
        }
        selector.wakeup();
        try {
            selector.close();
        } catch (IOException e) {
            // Do nothing.
        }
    }
}
