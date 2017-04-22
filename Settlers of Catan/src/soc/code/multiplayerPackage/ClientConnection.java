package soc.code.multiplayerPackage;

import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;

import soc.code.logicPackage.Player;

/**
 * This class is responsible for handling one connection to one client. It is an
 * individual thread that sucks all incoming data from the client and spits all
 * the outgoing data to this client.
 * 
 * @author Greg
 */
public class ClientConnection extends Thread {

	// the socket for this connection.
	private Socket clientSocket = null;

	// this is the a local copy of the player variable that will be updated when
	// new info comes into the server.
	private Player clientPlayer = null;
	// will determine if the player is ready or not:
	private boolean isReady = false;

	public ClientConnection(Socket s) {
		clientSocket = s;
		initializePlayer();
	}

	public void run() {

	}

	public void startGameProcess(HostSetup hostManager) {
		// making sure the client is ready...
		ConnectionHelper.printString("startingGame", clientSocket);
		while (!ConnectionHelper.readLine(clientSocket).equals("ready"))
			;
		System.out.println("Player " + clientPlayer.getUsername() + " is ready.");
		hostManager.broadcast("//Player " + clientPlayer.getUsername() + " is ready.");
		isReady = true;

		// sending the information the client needs:
	}

	/**
	 * This method is responsible for disconnecting the client that this
	 * connction is attatched to. It sends a message to the client telling it to
	 * disconnect and displays messages to the user updating them on when they
	 * have been disconnected.
	 */
	public void disconnectClient() {
		ConnectionHelper.printString("disconnect", clientSocket);
		try {
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("[ERROR] Unable to close socket " + clientSocket.getInetAddress());
			e.printStackTrace();
		}
		System.out.println(clientPlayer.getUsername() + " disconnected.");
	}

	/**
	 * This method gets the player's data from the client and then initializes
	 * the player object inside this class based on the data from the client.
	 */
	public void initializePlayer() {
		ConnectionHelper.printString("playerbio", clientSocket);

		String username = ConnectionHelper.readLine(clientSocket);
		clientPlayer = new Player(username.substring(username.indexOf(":") + 1));
		System.out.println("Client at " + clientSocket.getInetAddress() + " sent Bio file...");
		System.out.println("Username: \"" + clientPlayer.getUsername() + "\"");

		// parsing the color from its line:
		// <r>,<g>,<b>
		String colorLine = ConnectionHelper.readLine(clientSocket);
		int r = Integer.parseInt(colorLine.substring(colorLine.indexOf(":") + 1, colorLine.indexOf(",")));
		colorLine = colorLine.substring(colorLine.indexOf(",") + 1);
		int g = Integer.parseInt(colorLine.substring(0, colorLine.indexOf(",")));
		colorLine = colorLine.substring(colorLine.indexOf(",") + 1);
		int b = Integer.parseInt(colorLine);

		// setting the prefered color variable of the client:
		clientPlayer.setPreferedColor(r, g, b);
	}

	public Player getPlayer() {
		return clientPlayer;
	}
	
	public Socket getClientSocket() {
		return clientSocket;
	}

}
