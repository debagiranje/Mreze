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
    private static BlockingQueue<String> songQueue = new LinkedBlockingQueue<>();

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
                        while ((request = dataInputStream.readUTF()) != null) {
                            if (request.equals("NEXT")) {
                                // Client requests next song
                                sendNextSongToClient(socket);
                            } else {
                                // Client adds song to the queue
                                addSongToQueue(request);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addSongToQueue(String songName) {
        synchronized (songQueue) {
            songQueue.add(songName);
            System.out.println("Added to queue: " + songName);
            if (!isPlaying) {
                startPlaying();
            }
        }
    }

    private static void sendNextSongToClient(Socket socket) {
        synchronized (songQueue) {
            if (songQueue.isEmpty()) {
                System.out.println("No more songs in the queue");
                return;
            }

            isPlaying = true;
            String songName = songQueue.poll();
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
            for(Socket s : connectedClients) {
            	System.out.println(connectedClients.isEmpty());
                Socket currentSocket;
                /*synchronized (connectedClients) {
                    if (connectedClients.isEmpty()) {
                        System.out.println("No clients connected. Stopping playback.");
                        isPlaying = false;
                        break;
                    }

                    currentSocket = connectedClients.get(0);
                }*/

                sendNextSongToClient(s);

                try {
                    // Sleep for a while before sending the next song
                    Thread.sleep(1000); // Adjust the delay as needed
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
