package application;

import javafx.application.Application;
//blablabla
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class Main extends Application {
    private static final String SERVER_IP = "192.168.56.1"; // Replace with the server IP address
    private static final int SERVER_PORT = 6666;

    private MediaPlayer mediaPlayer;
    private Socket socket;
    private Button playNextButton;

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox();
        TextField songTextField = new TextField();
        Button addButton = new Button("Add to Queue");
        Button playButton = new Button("Play");
        playNextButton = new Button("Play Next");
        Button closeButton = new Button("Close Connection");
        root.getChildren().addAll(songTextField, addButton, playButton, playNextButton, closeButton);

        addButton.setOnAction(event -> {
            String songName = songTextField.getText();
            if (!songName.isEmpty()) {
                addToQueue(songName);
                songTextField.clear();
            }
        });

        playButton.setOnAction(event -> {
            if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.stop();
            }
            requestNextSongFromServer();
        });

        playNextButton.setOnAction(event -> {
            if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.stop();
            }
            requestNextSongFromServer();
        });

        closeButton.setOnAction(event -> {
            closeConnection();
            Platform.exit();
        });

        Scene scene = new Scene(root, 200, 200);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Jukebox Client");
        primaryStage.show();
    }

    private void addToQueue(String songName) {
        new Thread(() -> {
            try {
                if (socket == null || socket.isClosed()) {
                    socket = new Socket(SERVER_IP, SERVER_PORT);
                    System.out.println("Connected to server to add to queue");
                }

                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeUTF(songName);
                dataOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void requestNextSongFromServer() {
        new Thread(() -> {
            try {
                if (socket == null || socket.isClosed()) {
                    socket = new Socket(SERVER_IP, SERVER_PORT);
                    System.out.println("Connected to server for song request");
                }

                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeUTF("NEXT");
                dataOutputStream.flush();

                InputStream inputStream = socket.getInputStream();
                DataInputStream dataInputStream = new DataInputStream(inputStream);

                // Read the response from the server
                String response = dataInputStream.readUTF();
                if (response.startsWith("Song not found")) {
                    System.out.println(response);
                } else {
                    // Play the received song data
                    playSong(inputStream);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void playSong(InputStream inputStream) {
        try {
            DataInputStream dataInputStream = new DataInputStream(inputStream);

            // Read the size of the song data
            long songSize = dataInputStream.readLong();

            byte[] buffer = new byte[8192];
            int bytesRead;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            // Read the song data from the input stream
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
                if (byteArrayOutputStream.size() >= songSize) {
                    break;
                }
            }

            byte[] songData = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();

            // Create a temporary file from the received data
            File tempFile = File.createTempFile("temp_song", ".mp3");
            FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
            fileOutputStream.write(songData);
            fileOutputStream.close();

            // Play the temporary file
            String tempFilePath = tempFile.toURI().toString();
            Media media = new Media(tempFilePath);
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();

            mediaPlayer.setOnEndOfMedia(() -> {
                mediaPlayer.dispose();
                tempFile.delete();
                enablePlayNextButton();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enablePlayNextButton() {
        Platform.runLater(() -> playNextButton.setDisable(false));
    }

    private void closeConnection() {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
                System.out.println("Connection closed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

