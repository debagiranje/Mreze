package application;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SNovi {
    private static final int SERVER_PORT = 6666;
    private static final String MUSIC_FOLDER = "C:\\Users\\libor\\OneDrive\\Desktop\\zika";
    private static List<Socket> connectedClients = new ArrayList<>();
    private static boolean isPlaying = false;
    private static BlockingQueue<String> sharedSongQueue = new LinkedBlockingQueue<>();
    private static List<BlockingQueue<String>> clientQueues = new ArrayList<>(connectedClients.size());

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Server started. Listening on port " + SERVER_PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected");
                connectedClients.add(socket);

                new Thread(() -> {
                    try {
                        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                        String request;
                        while (true) {
                            try {
                                request = dataInputStream.readUTF();
                            } catch (EOFException e) {
                                break; // End the loop when the end of the stream is reached
                            }

                            if (request.equals("NEXT")) {
                                // Client requests next song
                                int clientIndex = connectedClients.indexOf(socket);
                                if (clientIndex >= 0 && clientIndex < clientQueues.size()) {
                                    BlockingQueue<String> clientSongQueue = clientQueues.get(clientIndex);
                                    sendNextSongToClient(socket, clientSongQueue);
                                }
                            } else {
                                // Client adds song to the shared queue
                                addSongToQueue(request);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }}
                

        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static synchronized void addSongToQueue(String songName) {
        sharedSongQueue.add(songName);
        System.out.println("Added to shared queue: " + songName);
        if (!isPlaying) {
            startPlaying();
        } else {
            updateClientQueues();
        }
    }

    private static synchronized void updateClientQueues() {
        List<BlockingQueue<String>> updatedClientQueues = new ArrayList<>();
        for (Socket socket : connectedClients) {
            BlockingQueue<String> clientSongQueue = new LinkedBlockingQueue<>(sharedSongQueue);
            updatedClientQueues.add(clientSongQueue);
        }
        clientQueues = updatedClientQueues;
    }

    private static void sendNextSongToClient(Socket socket, BlockingQueue<String> clientSongQueue) {
        if (clientSongQueue.isEmpty()) {
            System.out.println("No more songs in the queue for client: " + socket);
            return;
        }

        String songName = clientSongQueue.poll();
        System.out.println("Sending song to client: " + songName);

        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeUTF(songName);
            dataOutputStream.flush();

            File musicFolder = new File(MUSIC_FOLDER);
            File songFile = findSongFile(musicFolder, songName);

            if (songFile != null) {
                // Send the song data to the client
                sendSongToClient(socket, songFile);
            } else {
                // Song not found, send error message to the client
                dataOutputStream.writeUTF("Song not found: " + songName);
                dataOutputStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static File findSongFile(File folder, String songName) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().equalsIgnoreCase(songName)) {
                    return file;
                }
            }
        }
        return null;
    }

    private static void sendSongToClient(Socket socket, File songFile) throws IOException {
        byte[] buffer = new byte[8192];
        FileInputStream fileInputStream = new FileInputStream(songFile);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        OutputStream outputStream = socket.getOutputStream();

        // Send the size of the song data to the client
        long songSize = songFile.length();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.writeLong(songSize);

        // Send the song data to the client
        int bytesRead;
        while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        outputStream.flush();
        bufferedInputStream.close();
        fileInputStream.close();

        System.out.println("Song sent: " + songFile.getName());
    }

    private static void startPlaying() {
        new Thread(() -> {
            clientQueues = new ArrayList<>();
            for (Socket socket : connectedClients) {
                BlockingQueue<String> clientSongQueue = new LinkedBlockingQueue<>(sharedSongQueue);
                clientQueues.add(clientSongQueue);
            }

            boolean continuePlaying = true;
            while (continuePlaying) {
                continuePlaying = false;
                for (int i = 0; i < connectedClients.size(); i++) {
                    Socket socket = connectedClients.get(i);
                    BlockingQueue<String> clientSongQueue = clientQueues.get(i);
                    if (clientSongQueue.isEmpty()) {
                        System.out.println("No more songs in the queue for client: " + socket);
                    } else {
                        continuePlaying = true;
                        sendNextSongToClient(socket, clientSongQueue);
                    }

                    /*try {
                        // Sleep for a while before sending the next song
                        Thread.sleep(10000); // Adjust the delay as needed
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                }
            }
        }).start();
    }
}
