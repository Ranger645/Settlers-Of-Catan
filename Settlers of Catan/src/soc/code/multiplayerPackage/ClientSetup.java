package soc.code.multiplayerPackage;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import soc.code.logicPackage.Board;
import soc.code.logicPackage.Player;

/**
 * This class is meant to handle the multiplayer connection to the host for this
 * client. It will have methods for sending and recieving data to and from the
 * host (the server).
 * 
 * @author Greg
 */
public class ClientSetup extends Thread {

	private Socket clientSocket = null;
	private Player localPlayer;
	private boolean isReady = false;

	private boolean alive = true;

	// This is the local copy of the game board variable. It is updated in this
	// class because incoming changes from the server will be recieved here.
	private Board gameBoard = null;

	public ClientSetup(Player p) {
		localPlayer = p;
	}

	public void connectToHost(String ipAddress) {
		try {
			System.out.println("Attempting to connect to " + ipAddress + " on port: " + HostSetup.SERVER_PORT + "...");
			clientSocket = new Socket(ipAddress, HostSetup.SERVER_PORT);
			System.out.println("Successfully connected to Host: " + ipAddress + " on port: " + HostSetup.SERVER_PORT);
			System.out.println("Replying to Connection Tests...");
			this.start();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			System.out.println("[ERROR] Unable to connect to the host: Host not found.");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {

		while (alive) {

			String data = ConnectionHelper.readLine(clientSocket);

			doServerCommand(data);

			try {
				this.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		System.out.println("Killing the thread...");
		// the only time it breaks out of this while loop is if the socket
		// disconnects from the host.;
	} // end run method

	private void doServerCommand(String data) {
		// The following set of if statements will contaion the programming
		// for what a client should do when a host sends a particular
		// message to it. They will all be individual if statments.

		// this just means that the message that the server is sending
		// should be displayed in the client's console window.
		if (data.length() > 1 && data.substring(0, 2).equals("//"))
			System.out.println("[SERVER MESSAGE] " + data.substring(2));

		// This if statement is for if the host is requesting the ping of
		// the client.
		if (data.equals("ping")) {
			// the server is requesting a connection test:
			System.out.println("Latency request recieved... Replying to Latency Test.");
			ConnectionHelper.recievePingRequest(clientSocket);
		}

		// This is for when the client is sending basic information about
		// the player to the server. This information includes the player's
		// username, desired color (in RGB).
		if (data.equals("playerbio")) {
			sendPlayer();
		}

		// This is a command from the host that signals the client to
		// disconnect. This could be for a number of different reasons. Most
		// likely, the host shut down the game or manually kicked the client
		// from the game.
		if (data.equals("disconnect")) {
			try {
				clientSocket.close();
				alive = false;
				System.out.println("You have been disconnected from the server.");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				// [ERROR] Unable to close the socket.
				e.printStackTrace();
			}
		}

		// this is the command for starting the game. The host will send out
		// this message and waits for the readiness of each of the players.
		if (data.equals("startingGame")) {
			startGameProcess();
		}
	}

	/**
	 * This method sends the player data to the server for their local storage.
	 */
	private void sendPlayer() {
		// The data is printed in this order:
		// USERNAME:<username>
		// COLOR:<preferedRed>,<preferedGreen>,<preferedBlue>
		System.out.println("Sending BIO file...");
		ConnectionHelper.printString("USERNAME:" + localPlayer.getUsername(), clientSocket);
		ConnectionHelper.printString("COLOR:" + localPlayer.getPreferedColor().getRed() + ","
				+ localPlayer.getPreferedColor().getGreen() + "," + localPlayer.getPreferedColor().getBlue(),
				clientSocket);
		System.out.println("BIO file send.");
	}

	private void startGameProcess() {
		System.out.println("Host has initiated the start of the game, type \"ready\" to ready up for the game.");
		while (!isReady) // waiting for the user to ready up.
			try {
				this.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		// telling the server that this client is ready.
		ConnectionHelper.printString("ready", clientSocket);
		System.out.println("You are now ready for the game, recieving game data...");

		// recieving the tile data and using it to initialize the game board
		// from the server's incoming message.
		gameBoard = initializeGameBoard();
		System.out.println("Tile data Recieved, Board initialized.");
	}

	/**
	 * This method creates the gameboard based on the tile values sent by the
	 * server.
	 * 
	 * @return
	 */
	private Board initializeGameBoard() {
		System.out.println("Recieving Tile Data from the Host...");
		String[] tileData = new String[19];
		for (int i = 0; i < 19; i++) {
			tileData[i] = ConnectionHelper.readLine(clientSocket);
			// if the line is not from the tile transmission then it needs to be
			// rid of.
			if (tileData[i].charAt(tileData[i].length() - 1) != '|')
				doServerCommand(tileData[i--]);
		}
		Board b = new Board(tileData);
		return b;
	}

	/**
	 * @return whethor or not the socket bound to this host has been
	 *         disconnected or not.
	 */
	public boolean getAliveState() {
		return alive;
	}

	public boolean isReady() {
		return isReady;
	}

	public void setReady(boolean isReady) {
		this.isReady = isReady;
	}

}
