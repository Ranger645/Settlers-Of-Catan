package soc.code.multiplayerPackage;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import soc.code.logicPackage.Board;
import soc.code.logicPackage.BuildSite;
import soc.code.logicPackage.Player;
import soc.code.logicPackage.Tile;

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

		while (true) {

			// getting a command from the client.
			String data = ConnectionHelper.readLine(clientSocket);

			// executing the client's command
			doClientCommand(data);

			try {
				this.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * This method does the given string command that is provided by the client.
	 * 
	 * @param data
	 *            - the command that the client wants executed.
	 */
	public void doClientCommand(String data) {
		if(data.equals("buildsite")){
			
		}
	}

	public void startGameProcess(HostSetup hostManager, Board gameBoard) {
		// making sure the client is ready...
		ConnectionHelper.printString("startingGame", clientSocket);
		while (!ConnectionHelper.readLine(clientSocket).equals("ready"))
			;
		System.out.println("Player " + clientPlayer.getUsername() + " is ready.");
		hostManager.broadcast("//Player " + clientPlayer.getUsername() + " is ready.");
		isReady = true;

		// initializing the client's board object by sending the hosts.
		sendBoardTiles(gameBoard);

		// then sending the other players:

	}

	/**
	 * This method sends the tiles in the gameboard to the client socket saved
	 * in this object. It sends it linearly row by row from top to bottom. Each
	 * tile appears to the client like <RES_TYPE><RES_NUMBER>
	 * 
	 * @param gameBoard
	 */
	private void sendBoardTiles(Board gameBoard) {
		/*
		 * In order to send the board, the program lays out the tiles one row at
		 * a time linearly. Each tile will be sent in the same notation. To send
		 * one tile, the host must send the tile resource type and the tile's
		 * resource number. It will spell out the actual word for the resource
		 * type and then the resource collection number. It ends the
		 * transmission with a | to tell the client what is in the transmission.
		 * Ex) wood8|
		 */
		for (ArrayList<Tile> arr : gameBoard.getTileArray())
			for (Tile i : arr) {
				String messageToSend = "";
				messageToSend += i.toString();
				messageToSend += i.getResourceNumber() + "|";
				ConnectionHelper.printString(messageToSend, clientSocket);
			}
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
