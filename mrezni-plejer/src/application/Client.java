package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class Client {

	private Socket socket;
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	private String username;

	public Client(Socket socket, String username) {
		try {
			this.socket = socket;
			this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.username = username;
		} catch (IOException e) {
			closeEverything();
		}
	}

	public void sendMessage(String messageToSend) {
		try {
			bufferedWriter.write(messageToSend);
			bufferedWriter.newLine();
			bufferedWriter.flush();
		} catch (IOException e) {
			closeEverything();
		}
	}

	public void listenForMessage() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String msgFromGroupChat;

				while (socket.isConnected()) {
					try {
						msgFromGroupChat = bufferedReader.readLine();
						System.out.println(msgFromGroupChat);
						if (msgFromGroupChat.startsWith("SONG:")) {
							String songName = msgFromGroupChat.substring(5);
							playSong(songName);
						}
					} catch (IOException e) {
						closeEverything();
					}
				}
			}
		}).start();
	}

	public void playSong(String songName) {
		try {
			Player player = new Player(getClass().getResourceAsStream(songName));
			player.play();
		} catch (JavaLayerException e) {
			e.printStackTrace();
		}
	}

	public void closeEverything() {
		try {
			if (bufferedReader != null) {
				bufferedReader.close();
			}
			if (bufferedWriter != null) {
				bufferedWriter.close();
			}
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter your username for the group chat: ");
		String username = scanner.nextLine();
		Socket socket = new Socket("localhost", 1234);
		Client client = new Client(socket, username);

		client.listenForMessage();

		while (socket.isConnected()) {
			String messageToSend = scanner.nextLine();
			client.sendMessage(username + ": " + messageToSend);
		}
	}
}
