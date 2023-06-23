package application;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server {

	private ServerSocket serverSocket;
	private List<ClientHandler> clientHandlers;
	private Map<String, String> songUrlMap; // Mapping of song names to their URLs

	public Server(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
		this.clientHandlers = new ArrayList<>();
		this.songUrlMap = new HashMap<>(); // Initialize the song URL map

		// Populate the song URL map with example mappings
		songUrlMap.put("song1", "https://archive.org/details/Ex_Military-9086/Death_Grips_-_02_-_Guillotine.mp3");
		songUrlMap.put("song2", "https://archive.org/details/Carmina_Burana_Carl_Orff-9275/MIT_Concert_Choir_-_01_-_O_Fortuna.mp3");
		songUrlMap.put("song3", "https://archive.org/details/Information_Chase-7869/Bit_Shifter_-_01_-_Chase_Init_.mp3");
	}

	public void startServer() {
		try {
			while (!serverSocket.isClosed()) {
				Socket socket = serverSocket.accept();
				System.out.println("A new client has connected!");

				ClientHandler clientHandler = new ClientHandler(socket, this);
				clientHandlers.add(clientHandler);

				Thread thread = new Thread(clientHandler);
				thread.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void closeServerSocket() {
		try {
			if (serverSocket != null) {
				serverSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void broadcastMessage(String messageToSend) {
		if (messageToSend.startsWith("play: ")) {
			String songName = messageToSend.substring(6);
			String songUrl = songUrlMap.get(songName);

			if (songUrl != null) {
				for (ClientHandler clientHandler : clientHandlers) {
					clientHandler.sendMessage("play: " + songUrl);
				}
			} else {
				System.out.println("Song not found: " + songName);
			}
		} else {
			for (ClientHandler clientHandler : clientHandlers) {
				clientHandler.sendMessage(messageToSend);
			}
		}
	}

	public void removeClientHandler(ClientHandler clientHandler) {
		clientHandlers.remove(clientHandler);
	}

	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = new ServerSocket(1234);
		Server server = new Server(serverSocket);
		server.startServer();
	}
}
