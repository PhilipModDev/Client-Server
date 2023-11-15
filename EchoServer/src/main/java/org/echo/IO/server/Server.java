package org.echo.IO.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Server {
    //The port bind to the server.
    private final int port;
    //The Address assigned to server.
    private InetAddress address;
    //The server socket.
    private ServerSocketChannel server;
    //The threads executor for incoming clients.
    private final Executor serverThreads;
    public Server(int port, int threads){
        this.port = port;
        serverThreads = Executors.newFixedThreadPool(threads);
    }
    public Server(int port,String address,int threads) {
        this.port = port;
        bindSetAddress(address);
        serverThreads = Executors.newFixedThreadPool(threads);
    }
    private void bindSetAddress(String address){
        try {
            this.address = InetAddress.getByName(address);
        }catch (Exception exception){
            exception.printStackTrace();
        }
    }

    public void startServer() throws IOException {
        try {
            //Opens the server.
            server = ServerSocketChannel.open();
            if (address == null) server.bind(new InetSocketAddress(port));
            else server.bind(new InetSocketAddress(address,port));
            System.out.println("Server is open on:"+server.socket().toString());
            while (server.isOpen()) {
                //Accepts a client for connecting to the server.
                SocketChannel connectedClient = server.accept();
                if (connectedClient.isConnected()) {
                    ClientHandler clientHandler = new ClientHandler(connectedClient);
                    serverThreads.execute(clientHandler);
                }
            }
        }catch (Exception exception){
            exception.printStackTrace();
            this.server.socket().close();
        } finally {
            this.server.socket().close();
        }
    }

    //The client handler for the incoming clients to server.
    public static class ClientHandler implements Runnable {
        private final SocketChannel clientConnection;
        private final int BUFFER_SIZE = 4096;
        public ClientHandler(SocketChannel clientConnection){
            this.clientConnection = clientConnection;
        }

        @Override
        public void run() {
            String message;
            try {
                do {
                    ByteBuffer buffer = ByteBuffer.allocate(this.BUFFER_SIZE);
                    clientConnection.read(buffer);
                    message = extractedMessage(buffer);
                    if (message.isBlank()) break;
                    System.out.println("[Server]:"+message);
                    buffer.clear();
                    ByteBuffer writeBuffer = ByteBuffer.allocate(buffer.limit());
                    writeBuffer.put(message.getBytes(StandardCharsets.UTF_8));
                    writeBuffer.flip();
                    clientConnection.write(writeBuffer);
                }while (!message.equalsIgnoreCase("stop") && clientConnection.isConnected());
            }catch (Exception exception){
                exception.printStackTrace();
            }
        }

        private String extractedMessage(ByteBuffer buffer){
            String message = "";
            String dataMessage = new String(buffer.array(), StandardCharsets.UTF_8);
            for (int i = 0; i < buffer.limit(); i++) {
                byte data = buffer.get(i);
                if (data == 0) break;
                message = dataMessage.substring(0,i + 1);
            }
            return message;
        }
    }
}
