package soc.code.multiplayerPackage;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

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
	Player localPlayer;

	private boolean alive = true;

	public ClientSetup(Player p) {
		localPlayer = p;
	}

	public void connectToHost(String ipAddress) {
		try {
			System.out.println("Attempting to connect to " + ipAddress + " on port: " + HostSetup.SERVER_PORT + "...");
			clientSocket = new Socket(ipAddress, HostSetup.SERVER_PORT);
			System.out.println("Successfully connected to Host: " + ipAddress + " on port: " + HostSetup.SERVER_PORT);
			System.out.println("Waiting to recieve connection tests...");
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

			// The following set of if statements will contaion the programming
			// for what a client should do when a host sends a particular
			// message to it. They will all be individual if statments.

			// This if statement is for if the host is requesting the ping of
			// the client.
			if (data.equals("ping")) {
				// the server is requesting a connection test:
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

		}
		System.out.println("Killing the thread...");
		// the only time it breaks out of this while loop is if the socket
		// disconnects from the host.;
	} // end run method

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

	/**
	 * @return whethor or not the socket bound to this host has been
	 *         disconnected or not.
	 */
	public boolean getAliveState() {
		return alive;
	}

}
