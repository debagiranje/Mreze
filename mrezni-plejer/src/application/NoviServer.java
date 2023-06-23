package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class NoviServer {
    private static final int PORT = 8888;
    private static boolean isPlaying = false;
    private static List<Socket> clientSockets = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Jukebox server is running. Waiting for connections...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                // Add the client socket to the collection
                clientSockets.add(clientSocket);

                // Handle the client connection in a separate thread
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
            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream();
            byte[] buffer = new byte[4096];

            int bytesRead = inputStream.read(buffer);
            String request = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
            if (request.startsWith("play:")) {
                String audioFilePath = request.substring(5);
                playAudioFile(outputStream, audioFilePath);
            } else {
                System.err.println("Invalid request: " + request);
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                // Remove the client socket from the collection
                clientSockets.remove(clientSocket);
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void playAudioFile(OutputStream outputStream, String audioFilePath) {
        try {
            Path path = Paths.get(audioFilePath);
            byte[] audioData = Files.readAllBytes(path);
            outputStream.write(audioData);
            outputStream.flush();
        } catch (IOException e) {
            System.err.println("Error sending audio data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
