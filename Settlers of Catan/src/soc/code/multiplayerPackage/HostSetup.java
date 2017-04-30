package soc.code.multiplayerPackage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import soc.code.logicPackage.Board;
import soc.code.logicPackage.Player;
import soc.code.renderPackage.GUI;

/**
 * This class is responsible for storing and keeping track of all the client
 * connection objects. It will contain an array of them. It will also contain
 * methods for broadcasting common data to all clients at once.
 * 
 * @author Greg
 */
public class HostSetup extends Thread {

	// the serversocket object that will handle sending data back and forth:
	private ServerSocket gameDataHub = null;
	public static final int SERVER_PORT = 9890; // the port that this program
												// uses

	// list of client connections that the host is storing:
	private ArrayList<ClientConnection> clientConnectionList = null;

	// variable that conrols whether or not to keep searching for new clients:
	private boolean keepSearching = true;

	// the reference to the main game board.
	private Board gameBoard = null;

	public HostSetup(Board b) {
		clientConnectionList = new ArrayList<ClientConnection>();
		gameBoard = b;
	}

	public void run() {
		initializeServerSocket(SERVER_PORT);

		searchForClientConnections();
	}

	/**
	 * Tells the client connection handler to start the client's turn. Then it
	 * waits for the turn to be finished all while constantly updating the main
	 * gameboard with whatever the client whose turn it is is doing to it.
	 * 
	 * @param playerIndex
	 *            - the index of the player whose turn it is.
	 * @param mainGameBoard
	 *            - the main board that is updated constantly by the client
	 *            whose turn it is.
	 * @param gui
	 *            - the gui of the host. It is passed so it can be repainted
	 *            each time the board is updated.
	 * @return the final board of the player whose turn it is.
	 */
	public Board doTurn(int playerIndex, Board mainGameBoard, GUI gui) {

		// Telling the client that it is their turn.
		clientConnectionList.get(playerIndex).startTurn();

		// waiting for the player's turn to be over...
		while (clientConnectionList.get(playerIndex).isTurn()) {
			// keeping the main gameboard updated in real time:
			gui.repaint();
		}

		// sending the board that is stored in that client to the board that is
		// kept on the server.
		return clientConnectionList.get(playerIndex).getGameBoard();
	}

	/**
	 * This method starts the actual game for each of the clients. It is in
	 * charge of sending all of the map data to the clients as well as other
	 * player data to the clients. It also starts all of the threads for each of
	 * the client connections.
	 * 
	 * @param gameBoard
	 */
	public void startGameProcess(Board gameBoard) {
		for (ClientConnection i : clientConnectionList) {
			i.start();
			i.startGameProcess(this, gameBoard);
		}
	}

	// the while loop that will wait for a new client to connect and then store
	// their information to be playable with.
	private void searchForClientConnections() {
		while (keepSearching) {
			try {
				getClientConnection();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("[WARNING] Interupted Client Connection accepting process.");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Gets the combined readiness of all of the clients. It is essentially a
	 * many input AND gate for all of the individual client readiness status'.
	 * 
	 * @return - the combined readiness of all the clients.
	 */
	public boolean getClientReadiness() {
		boolean readiness = true;
		for (ClientConnection i : clientConnectionList)
			if (!i.getReadiness()) {
				readiness = false;
				break;
			}
		return readiness;
	}

	/**
	 * This method kicks the client with the specified username off of the
	 * server. It returns whether or not the kicking was successful due to the
	 * specified username being in the array or not.
	 * 
	 * @param username
	 *            - the username of the client to be kicked.
	 * @return - the success of the disconnection.
	 */
	public boolean kickClient(String username) {
		for (ClientConnection CC : clientConnectionList)
			if (CC.getPlayer().getUsername().equals(username)) {
				CC.disconnectClient();
				return true;
			}
		return false;
	}

	// initializes the server socket variable on the specified port or prints an
	// error:
	private void initializeServerSocket(int PORT) {
		try {
			gameDataHub = new ServerSocket(SERVER_PORT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("[ERROR] Not able to establish host on port " + PORT + ".");
			e.printStackTrace();
		}
	}

	// When the user is finished searching for clients it broadcasts all the
	// appropriate data to the clients about the other clients and about the map
	// and all that. Then it starts their communication reciever threads.
	public void startConnectionThreads() {
		for (ClientConnection i : clientConnectionList) {
			i.start();
		}
	}

	/**
	 * This method is for accepting one client connection into the server andthe
	 * client connection array. It throws an IO exception only when the server
	 * socket is closed.
	 * 
	 * @throws IOException
	 */
	private void getClientConnection() throws IOException {
		if (clientConnectionList.size() == 0)
			System.out.println("Accepting Clients...");
		Socket incomingClient = gameDataHub.accept();
		System.out.println("Client Seeking connection from " + incomingClient.getInetAddress());
		System.out.println("Testing connection at Client's ip Address...");
		// testsetHosting the response time of the client.
		ConnectionHelper.printString("//Testing Latency...", incomingClient);
		int max = -1;
		int averagePing = 0;
		int pingAccuracy = 10;
		for (int i = 0; i < pingAccuracy; i++) {
			int ping = (int) ConnectionHelper.getResponseTime(incomingClient);
			if (ping > max)
				max = ping;
			averagePing += ping;
		}
		averagePing /= pingAccuracy;
		System.out.println("Latency Retrieved " + pingAccuracy + " times...");
		System.out.println("Max Ping = " + max + "ms.");
		System.out.println("Average ping = " + averagePing + "ms.");
		// sending results to the client.
		ConnectionHelper.printString("//Latency test Complete...", incomingClient);
		ConnectionHelper.printString("//Average Ping = " + averagePing, incomingClient);
		if (averagePing < 300) {
			System.out.println("Latency is acceptable, seeking player data...");
			ConnectionHelper.printString("//Test Passed.", incomingClient);
			clientConnectionList.add(new ClientConnection(incomingClient, gameBoard));
		} else {
			System.out.println("[WARNING] Latency is higher than recommended (300ms)...");
			ConnectionHelper.printString("//WARNING: Test Failed... try getting a better connection.", incomingClient);
			clientConnectionList.add(new ClientConnection(incomingClient, gameBoard));
		}
	}

	public void updateAllBuildSites(int clientThatIsUpdating) {
		// setting the board on the server to have the same build sites.
		System.out.println("Updating Build Sites.");
		broadcast("Updating Build Sites.");
		gameBoard.overwriteBuildSites(clientConnectionList.get(clientThatIsUpdating).getGameBoard().getBuildSites());
		for (int i = 0; i < clientConnectionList.size(); i++)
			if (i != clientThatIsUpdating)
				// sending the new build sites to all the clients except the one
				// that sent it in the first place.
				ConnectionHelper.sendBoardBuildSites(gameBoard, clientConnectionList.get(i).getClientSocket());
	}

	// sends the given message to each of the clients
	public void broadcast(String message) {
		for (ClientConnection i : clientConnectionList)
			ConnectionHelper.printString("//" + message, i.getClientSocket());
	}

	public ArrayList<ClientConnection> getClientConnections() {
		return clientConnectionList;
	}

}
