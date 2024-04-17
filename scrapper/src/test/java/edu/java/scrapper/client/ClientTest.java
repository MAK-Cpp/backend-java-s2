package edu.java.scrapper.client;

import java.io.IOException;
import java.net.ServerSocket;

public abstract class ClientTest {
    static int getPort() {
        for (int port = 1024; port <= 49151; ++port) {
            try (ServerSocket ignored = new ServerSocket(port)) {
                return port;
            } catch (IOException e) {
            }
        }
        return -1;
    }
}
