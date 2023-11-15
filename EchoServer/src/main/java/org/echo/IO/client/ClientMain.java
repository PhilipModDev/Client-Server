package org.echo.IO.client;


import java.io.IOException;

public class ClientMain {
    public static void main(String[] args) throws IOException {
        Client client = new Client(180,"localhost");
        client.connectToServer();
    }
}
