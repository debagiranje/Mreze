package application;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.IOException;
import java.net.*;
//import java.time.Duration;
import java.time.Instant;

public class NoviKlijent extends Application {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8888;
    private MediaPlayer mediaPlayer;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Jukebox Client");

        Button playButton = new Button("Play");
        playButton.setOnAction(e -> playSong());

        primaryStage.setScene(new Scene(playButton, 200, 100));
        primaryStage.show();
    }

    private void playSong() {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
            System.out.println("Connected to Jukebox server");

            Instant startTime = synchronizeTime(socket);

            // Replace the song path with your desired audio file
            String audioFilePath = "C:\\Users\\libor\\OneDrive\\Desktop\\pjesma.mp3";

            Media audioMedia = new Media("file:///" + audioFilePath.replace("\\", "/"));
            mediaPlayer = new MediaPlayer(audioMedia);

            long offsetMillis = startTime.toEpochMilli() - Instant.now().toEpochMilli();
            mediaPlayer.setOnReady(() -> {
                mediaPlayer.seek(Duration.millis(offsetMillis));
            });

            mediaPlayer.play();
        } catch (IOException e) {
            System.err.println("Error in the client: " + e.getMessage());
            e.printStackTrace();
        }
    }



    private Instant synchronizeTime(Socket socket) throws IOException {
        DatagramSocket timeSocket = new DatagramSocket();
        InetAddress address = InetAddress.getByName(SERVER_ADDRESS);

        byte[] buffer = new byte[256];
        DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length, address, SERVER_PORT);
        DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);

        // Send time synchronization request
        timeSocket.send(requestPacket);

        // Receive time synchronization response
        timeSocket.receive(responsePacket);
        timeSocket.close();

        String response = new String(responsePacket.getData()).trim();
        long serverTime = Long.parseLong(response);
        long clientTime = System.currentTimeMillis();

        long timeDiff = serverTime - clientTime;
        return Instant.now().plusMillis(timeDiff);
    }

    @Override
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
}



