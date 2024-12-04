package proxy;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class BidirectionalProxy {
    private final int proxyPort;
    private final String serverAddress;
    private final int serverPort;

    public BidirectionalProxy(int proxyPort, String serverAddress, int serverPort) {
        this.proxyPort = proxyPort;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public void startProxy() {
        ExecutorService executor = Executors.newCachedThreadPool();

        try (ServerSocket proxySocket = new ServerSocket(proxyPort)) {
            System.out.println("Proxy started on port " + proxyPort);

            while (true) {
                // Accept a connection from a client
                Socket clientSocket = proxySocket.accept();
                System.out.println("New client connected.");

                // Connect to the main server
                Socket serverSocket = new Socket(serverAddress, serverPort);
                System.out.println("Connected to the main server at " + serverAddress + ":" + serverPort);

                // Relay traffic in both directions
                executor.submit(new TrafficRelayer(clientSocket, serverSocket)); // Client → Server
                executor.submit(new TrafficRelayer(serverSocket, clientSocket)); // Server → Client
            }
        } catch (IOException e) {
            System.err.println("Proxy error: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    private static class TrafficRelayer implements Runnable {
        private final Socket inputSocket;
        private final Socket outputSocket;

        public TrafficRelayer(Socket inputSocket, Socket outputSocket) {
            this.inputSocket = inputSocket;
            this.outputSocket = outputSocket;
        }

        @Override
        public void run() {
            try (InputStream input = inputSocket.getInputStream();
                 OutputStream output = outputSocket.getOutputStream()) {

                byte[] buffer = new byte[1024];
                int bytesRead;

                // Relay data in the specified direction
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                    output.flush();
                }
            } catch (IOException e) {
                System.err.println("Connection closed: " + e.getMessage());
            } finally {
                try {
                    inputSocket.close();
                    outputSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing sockets: " + e.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) {
        int proxyPort = 7777; // Port for clients to connect to the proxy
        String serverAddress = "127.0.0.1"; // Address of the main server
        int serverPort = 8888; // Port of the main server

        BidirectionalProxy proxy = new BidirectionalProxy(proxyPort, serverAddress, serverPort);
        proxy.startProxy();
    }
}
