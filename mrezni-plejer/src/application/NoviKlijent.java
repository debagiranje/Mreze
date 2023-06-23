package application;



import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class NoviKlijent {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8888;

    public static void main(String[] args) throws LineUnavailableException {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
            System.out.println("Connected to Jukebox server");

            // Send the request to the server specifying the audio file path
            OutputStream outputStream = socket.getOutputStream();
            String audioFilePath = "C:\\\\Users\\\\libor\\\\OneDrive\\\\Desktop\\\\pjesma.mp3\\"; // Replace with your audio file path
            String request = "play:" + audioFilePath;
            outputStream.write(request.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();

            // Receive the audio data from the server and play it
            InputStream inputStream = socket.getInputStream();
            AudioFormat format = new AudioFormat(44100, 16, 2, true, true);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine dataLine = (SourceDataLine) AudioSystem.getLine(info);
            dataLine.open(format);
            dataLine.start();

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                // Write the received audio data to the audio output
                dataLine.write(buffer, 0, bytesRead);
            }

            dataLine.drain();
            dataLine.close();
        } catch (IOException e) {
            System.err.println("Error in the client: " + e.getMessage());
            e.printStackTrace();
        }
    }
}




