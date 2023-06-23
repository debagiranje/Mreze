package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

	private Socket socket;
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	private String clientUsername;
	private Server server;

	public ClientHandler(Socket socket, Server server) {
		try {
			this.socket = socket;
			this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.clientUsername = bufferedReader.readLine();
			this.server = server;
			server.broadcastMessage("SERVER: " + clientUsername + " has entered the chat!");
		} catch (IOException e) {
			closeEverything();
		}
	}

	@Override
	public void run() {
		String messageFromClient;

		while (socket.isConnected()) {
			try {
				messageFromClient = bufferedReader.readLine();
				server.broadcastMessage(messageFromClient);
			} catch (IOException e) {
				closeEverything();
				break;
			}
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

	public void closeEverything() {
		server.removeClientHandler(this);
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

}
