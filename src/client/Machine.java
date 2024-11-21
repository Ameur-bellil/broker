package client;

import java.io.*;
import java.net.*;

public class Machine {
    private String serverAddress;
    private int serverPort;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public Machine(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public void startClient() {
        try {
            socket = new Socket(serverAddress, serverPort);  // Connect to the server
            System.out.println("Connected to the server at " + serverAddress + ":" + serverPort);

            // Set up input and output streams for communication
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);  // Send messages to server

            // Start a thread to listen for incoming messages from the server
            Thread listenerThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    System.err.println("Error reading from server: " + e.getMessage());
                }
            });
            listenerThread.start();

            // Read messages from the user and send them to the server
            String userMessage;
            while (true) {
                userMessage = userInput.readLine();
                if (userMessage.equalsIgnoreCase("exit")) {
                    break;
                }
                out.println(userMessage);  // Send message to server
            }

        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        String serverAddress = "127.0.0.1";  // Replace with the server machine's IP address
        int serverPort = 8888;
        Machine client = new Machine(serverAddress, serverPort);
        client.startClient();
    }
}
