package server;

import java.io.*;
import java.net.*;

public class MachineHandler implements Runnable {
    private Socket socket;
    private BufferedWriter out;  // Use BufferedWriter instead of PrintWriter
    private BufferedReader in;
    private Server server;

    public MachineHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            // Set up input and output streams for machine communication
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received from machine:" + message);
                // Forward the message to all other machines
                server.forwardMessage(message, this);
            }

        } catch (IOException e) {
            System.err.println("Error handling machine communication: " + e.getMessage());
        } finally {
            try {
                // When the machine disconnects, remove them from the server's machine list
                server.removeMachine(this);
                socket.close();
                System.out.println("Client disconnected.");
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    // Method to send a message to this client using BufferedWriter
    public void sendMessage(String message) {
        try {
            out.write(message);  // Write the message to the machine
            out.newLine();       // Add a new line after each message
            out.flush();         // Ensure the message is sent immediately
        } catch (IOException e) {
            System.err.println("Error sending message to machine: " + e.getMessage());
        }
    }
}
