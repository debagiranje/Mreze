package application;

import java.io.IOException;
import java.net.*;
import java.time.Instant;

public class NoviServer {
    private static final int PORT = 8888;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Jukebox server is running. Waiting for connections...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                Thread clientThread = new Thread(() -> handleClient(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            System.err.println("Error in the server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try {
            // Perform time synchronization
            Instant time = Instant.now();
            sendTimeSyncResponse(clientSocket, time);

            // Continue with other operations or commands from the client
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void sendTimeSyncResponse(Socket clientSocket, Instant time) throws IOException {
        DatagramSocket timeSocket = new DatagramSocket();
        InetAddress clientAddress = clientSocket.getInetAddress();
        int clientPort = clientSocket.getPort();

        byte[] buffer = String.valueOf(time.toEpochMilli()).getBytes();
        DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);

        timeSocket.send(responsePacket);
        timeSocket.close();
    }
}
