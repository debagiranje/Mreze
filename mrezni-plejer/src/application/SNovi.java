package application;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SNovi {
    private static final int SERVER_PORT = 6666;
    private static final String MUSIC_FOLDER = "C:/Users/libor/OneDrive/Desktop/zika/";

    private static boolean isSendingMusic = false;
    private static Queue<String> songQueue = new LinkedList<>();
    private static List<Socket> connectedClients = new LinkedList<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Server started. Listening on port " + SERVER_PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected");

                synchronized (connectedClients) {
                    connectedClients.add(socket);
                }

                Thread clientThread = new Thread(() -> {
                    try {
                        handleClient(socket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        synchronized (connectedClients) {
                            connectedClients.remove(socket);
                        }
                    }
                });
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket socket) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        String songName = dataInputStream.readUTF();

        File musicFolder = new File("C:/Users/libor/OneDrive/Desktop/zika/");
        File songFile = findSongFile(musicFolder, songName);

        if (songFile != null) {
            synchronized (songQueue) {
                songQueue.add(songName);
                if (!isSendingMusic) {
                    isSendingMusic = true;
                    sendMusicToClients();
                }
            }
        } else {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeUTF("Song not found: " + songName);
            dataOutputStream.flush();
        }

        socket.close();
        System.out.println("Client disconnected");
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

    private static void sendMusicToClients() {
        while (true) {
            String songName;
            synchronized (songQueue) {
                if (songQueue.isEmpty()) {
                    isSendingMusic = false;
                    break;
                }
                songName = songQueue.poll();
            }

            try {
                File musicFolder = new File("C:/Users/libor/OneDrive/Desktop/zika/");
                File songFile = findSongFile(musicFolder, songName);

                if (songFile != null) {
                    broadcastSong(songFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void broadcastSong(File songFile) throws IOException {
        byte[] buffer = new byte[8192];
        FileInputStream fileInputStream = new FileInputStream(songFile);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

        synchronized (connectedClients) {
            for (Socket clientSocket : connectedClients) {
                OutputStream outputStream = clientSocket.getOutputStream();

                long songSize = songFile.length();
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                dataOutputStream.writeLong(songSize);

                int bytesRead;
                while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.flush();
            }
        }

        bufferedInputStream.close();
        fileInputStream.close();
    }
}
