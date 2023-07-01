package application; 
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SNovi {
    private static final int SERVER_PORT = 6666;
    private static final String MUSIC_FOLDER = "C:/Users/libor/OneDrive/Desktop/zika/";

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Server started. Listening on port " + SERVER_PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected");

                new Thread(() -> {
                    try {
                        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                        String songName = dataInputStream.readUTF();

                        File musicFolder = new File("C:/Users/libor/OneDrive/Desktop/zika/");
                        File songFile = findSongFile(musicFolder, songName);

                        if (songFile != null) {
                            // Send the song data to the client
                            sendSongToClient(socket, songFile);
                        } else {
                            // Song not found, send error message to the client
                            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                            dataOutputStream.writeUTF("Song not found: " + songName);
                            dataOutputStream.flush();
                        }

                        socket.close();
                        System.out.println("Client disconnected");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
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
    }


}

