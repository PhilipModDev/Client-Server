package org.echo.IO.client;

import io.github.pixee.security.BoundedLineReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class Client {

    /* The port number for the client. */
    private final int port;
    //The address to the server.
    private InetAddress address;
    //The socket that connects to the server.
    private SocketChannel client;

    public Client(int port,String address){
        this.port = port;
        bindAddress(address);
    }
    private void bindAddress(String address){
        try {
            this.address = InetAddress.getByName(address);
        }catch (Exception exception){
            exception.printStackTrace();
        }
    }

    public void connectToServer() throws IOException {
        try {
            //Open the socket connection to the server.
            client = SocketChannel.open(new InetSocketAddress(address, port));
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Please enter a message.");
           while (client.isConnected()){
               //Reads and sends a message to the server.
               String message = BoundedLineReader.readLine(reader, 5_000_000);
               if (message.equalsIgnoreCase("stop")) break;
               byte[] data = message.getBytes(StandardCharsets.UTF_8);
               ByteBuffer buffer = ByteBuffer.allocateDirect(data.length);
               buffer.put(data);
               buffer.flip();
               client.write(buffer);
               //Reads the response from the server.
               ByteBuffer readBuffer = ByteBuffer.allocate(data.length);
               client.read(readBuffer);
               String response = extractedMessage(readBuffer);
               System.out.println("[From Server to Client]:"+ response);
           }
        }catch (Exception exception){
            exception.printStackTrace();
            client.close();
        }finally {
            client.close();
        }
    }
    private String extractedMessage(ByteBuffer buffer){
        String message = "";
        byte[] array = buffer.array();
        String dataMessage = new String(array, StandardCharsets.UTF_8);
        for (int i = 0; i < buffer.limit(); i++) {
            byte data = buffer.get(i);
            if (data == 0) break;
            message = dataMessage.substring(0,i + 1);
        }
        return message;
    }
}
