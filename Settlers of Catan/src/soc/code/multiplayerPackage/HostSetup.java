package soc.code.multiplayerPackage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import soc.code.logicPackage.Player;

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

	public HostSetup() {
		clientConnectionList = new ArrayList<ClientConnection>();
	}

	public void run() {
		initializeServerSocket(SERVER_PORT);

		searchForClientConnections();
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
	 * This method kicks the client with the specified username off of the
	 * server. It returns whether or not the kicking was successful due to the
	 * specified username being in the array or not.
	 * 
	 * @param username - the username of the client to be kicked.
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
		int max = -1;
		int averagePing = 0;
		int pingAccuracy = 10;
		for (int i = 0; i < pingAccuracy; i++) {
			int ping = (int) ConnectionHelper.getResponseTime(incomingClient);
			if (ping > max)
				max = ping;
			averagePing += ping;
		}
		System.out.println("Latency Retrieved " + pingAccuracy + "times...");
		System.out.println("Max Ping = " + max + "ms.");
		System.out.println("Average ping = " + averagePing / pingAccuracy + "ms.");
		if (averagePing < 300) {
			System.out.println("Latency is acceptable, seeking player data...");
			clientConnectionList.add(new ClientConnection(incomingClient));
		} else
			System.out.println("[WARNING] Latency is higher than recommended (300ms)...");
	}

	// sends the given message to each of the clients.
	public void broadcast(String message) {

	}

}
