package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private final int port;
    private final List<MachineHandler> machines;

    public Server(int port) {
        this.port = port;
        this.machines = new ArrayList<>();
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started! \uD83D\uDE80 \uD83D\uDE80");
            System.out.println("Waiting for clients to connect...");

            while (true) {
                // Accept incoming machine connections
                Socket socket = serverSocket.accept();
                System.out.println("New client connected.");

                // Create a new machine Handler for each machine and add to the list
                MachineHandler machineHandler = new MachineHandler(socket, this);
                synchronized (machines) {
                    machines.add(machineHandler);
                }

                // Start a new thread to handle machine communication
                Thread thread = new Thread(machineHandler);
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    // Method to forward a message from one machine to all others
    public void forwardMessage(String message, MachineHandler sender) {
        synchronized (machines) {
            for (MachineHandler machine : machines) {
                if (machine != sender) {  // Don't send the message back to the sender
                    machine.sendMessage(message);
                }
            }
        }
    }

    // Remove machine when they disconnect
    public void removeMachine(MachineHandler machineHandler) {
        synchronized (machines) {
            machines.remove(machineHandler);
        }
    }

    public static void main(String[] args) {
        int port = 8888;
        Server server = new Server(port);
        server.startServer();
    }
}
