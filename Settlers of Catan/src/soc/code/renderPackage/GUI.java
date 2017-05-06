package soc.code.renderPackage;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import soc.code.logicPackage.Board;
import soc.code.logicPackage.BuildSite;
import soc.code.logicPackage.Tile;
import soc.code.multiplayerPackage.ClientSetup;

/**
 * This class is the frame that will hold the game and all Graphical features
 * involved with the game. It will hold the panels necessary for displaying the
 * GameSetup JPanel and the Game JPanel. It will Also store high level methods
 * for controlling these and switching between them. Finally, it will be
 * responsible for funneling the mouse events down to the Game JPanel and
 * subsequently the UI Object Inside of the Game JPanel
 * 
 * @author Greg
 */
public class GUI extends JFrame implements ActionListener, KeyListener {

	private GamePanel mainPanel = null;
	private boolean isHostGUI = false;
	private ClientSetup clientManager = null;

	private boolean enabledIO = false;
	private JMenuItem buildSettlement, endTurn, buildCity, buildRoad = null;

	// keeps track of whether the user is in the process of selecting a road.
	private boolean selectingRoad = false;

	public GUI(Board gameBoard, ClientSetup clientManager, boolean isHost) {

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(Tile.TILE_WIDTH * 6, Tile.TILE_WIDTH * 6);
		this.setTitle("Stettlers of Catan");
		this.setResizable(false);

		this.isHostGUI = isHost;
		this.clientManager = clientManager;

		mainPanel = new GamePanel(gameBoard, clientManager);
		this.add(mainPanel);

		if (!isHostGUI) {

			JMenuBar menuBar = new JMenuBar();
			menuBar.setPreferredSize(new Dimension(this.getWidth(), 25));

			JMenu commandMenu = new JMenu("Commands");

			buildSettlement = new JMenuItem("Build Settlement");
			buildSettlement.addActionListener(this);

			buildCity = new JMenuItem("Build City");
			buildCity.addActionListener(this);

			buildRoad = new JMenuItem("Build Road");
			buildRoad.addActionListener(this);

			endTurn = new JMenuItem("End Turn");
			endTurn.addActionListener(this);

			this.addKeyListener(this);

			commandMenu.add(buildSettlement);
			commandMenu.add(buildCity);
			commandMenu.add(buildRoad);
			commandMenu.add(endTurn);

			menuBar.add(commandMenu);

			this.setJMenuBar(menuBar);

			// starting the IO disabled until it is the client's turn.
			closeIO();
		}

		// only shows the board if it is a client doing the controlling.
		if (!isHostGUI)
			this.setVisible(true);
	}

	/**
	 * Will enable all of the components that the user can interact with
	 */
	public void openIO() {
		buildSettlement.setEnabled(true);
		buildCity.setEnabled(true);
		buildRoad.setEnabled(true);
		endTurn.setEnabled(true);
		enabledIO = true;
	}

	/**
	 * will disable all of the components that the user can interact with
	 */
	public void closeIO() {
		buildSettlement.setEnabled(false);
		buildCity.setEnabled(false);
		buildRoad.setEnabled(false);
		endTurn.setEnabled(false);
		enabledIO = false;
	}

	public GamePanel getMainPanel() {
		return mainPanel;
	}

	public void setMainPanel(GamePanel mainPanel) {
		this.mainPanel = mainPanel;
	}

	public boolean isEnabledIO() {
		return enabledIO;
	}

	public void setEnabledIO(boolean enabledIO) {
		this.enabledIO = enabledIO;
	}

	/**
	 * Adds a road starting at the selected build site. It then waits for the
	 * user to select a VALID build site and then creates the road logically.
	 */
	public void addRoad() {
		// Making sure this is true so nothing else happens:
		System.out.println("Choose an adjacent build site then press Lock Road.");
		selectingRoad = true;
		this.closeIO();
		buildRoad.setEnabled(true);
		buildRoad.setText("Lock Road");

		BuildSite roadStartSite = mainPanel.getSelectedBuildSite();

		// getting a selected road from the user as long as it takes to be a
		// valid road.
		boolean inValidSite;
		do {
			// waiting for the user to pick the road.
			while (selectingRoad) {
				System.out.println("Waiting...");
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			inValidSite = !mainPanel.getGameBoard().areBuildSitesAdjacent(
					new Point(roadStartSite.getArrX(), roadStartSite.getArrY()),
					new Point(mainPanel.getSelectedBuildSite().getArrX(), mainPanel.getSelectedBuildSite().getArrY()));

			// displaying status message:
			if (inValidSite) {
				System.out.println("That build site is not adjacent to the selected Build Site.");
				selectingRoad = true;
			} else
				System.out.println("Building Road from (" + roadStartSite.getArrX() + ", " + roadStartSite.getArrY()
						+ ") to (" + mainPanel.getSelectedBuildSite().getArrX() + ", "
						+ mainPanel.getSelectedBuildSite().getArrY() + ")");
		} while (inValidSite);

		buildRoad.setText("Build Road");
		openIO();

		// After both build sites have been recieved, now they have to be
		// propperly formatted.
		BuildSite[] adjacentSites = mainPanel.getGameBoard().getAdjacentBuildSites(roadStartSite.getArrX(),
				roadStartSite.getArrY());

		if (adjacentSites[0] == mainPanel.getSelectedBuildSite()) {
			// Then the road has to be built to the left of the first selected
			// build site.
			roadStartSite.setRoadIDValue(BuildSite.ROAD_LEFT_ID, clientManager.getPlayerIndex());
			mainPanel.getSelectedBuildSite().setRoadIDValue(BuildSite.ROAD_RIGHT_ID, clientManager.getPlayerIndex());
		}

		if (adjacentSites[2] == mainPanel.getSelectedBuildSite()) {
			// Then the road has to be built to the right of the first selected
			// build site.
			roadStartSite.setRoadIDValue(BuildSite.ROAD_RIGHT_ID, clientManager.getPlayerIndex());
			mainPanel.getSelectedBuildSite().setRoadIDValue(BuildSite.ROAD_LEFT_ID, clientManager.getPlayerIndex());
		}

		if (adjacentSites[1] == mainPanel.getSelectedBuildSite()) {
			// Then the road has to be built below or above the first selected
			// build site.
			roadStartSite.setRoadIDValue(BuildSite.ROAD_MIDDLE_ID, clientManager.getPlayerIndex());
			mainPanel.getSelectedBuildSite().setRoadIDValue(BuildSite.ROAD_MIDDLE_ID, clientManager.getPlayerIndex());
		}

	}

	/**
	 * Updates the build site that is selected in the main game panel.
	 */
	public void sendSelectedBuildSite() {
		// Finding the proper build site to send which needs to be
		// updated.
		for (int i = 0; i < clientManager.getGameBoard().getBuildSites().size(); i++)
			for (int n = 0; n < clientManager.getGameBoard().getBuildSites().get(i).size(); n++)
				if (clientManager.getGameBoard().getBuildSites().get(i).get(n) == mainPanel.getSelectedBuildSite()) {
					// telling the server to updated the build
					// sites.
					clientManager.sendUpdatedBuildSite(n, i);
					break;
				}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// A button on the gui has been pressed and therefore must have its
		// action executed. This means that it is this clients turn because
		// buttons are only enabled on the clients turn. Every time a button is
		// pressed the client must send the updated build site array lists to
		// the server so the server can distribute the updated ones to the
		// client.
		if (enabledIO)
			if (e.getSource() == buildSettlement) {
				if (mainPanel.getSelectedBuildSite().buildSettlement(clientManager.getPlayerIndex())) {
					// printing status message
					System.out.println("Building settlement at (" + mainPanel.getSelectedBuildSite().getX() + ", "
							+ mainPanel.getSelectedBuildSite().getY() + ")");
					// updating build site array in the network
					sendSelectedBuildSite();
				} else
					// printing statis message.
					System.out.println("Failed to build settlement.");
			} else if (e.getSource() == endTurn) {
				// ends the turn.
				clientManager.endTurn();
			} else if (e.getSource() == buildCity) {
				// building a city.
				if (mainPanel.getSelectedBuildSite().buildCity(clientManager.getPlayerIndex())) {
					// printing statis message.
					System.out.println("Building City at (" + mainPanel.getSelectedBuildSite().getX() + ", "
							+ mainPanel.getSelectedBuildSite().getY() + ")");
					// updating build site array in the network
					sendSelectedBuildSite();
				} else
					// printing statis message.
					System.out.println("Failed to build City.");
			} else if (e.getSource() == buildRoad) {
				// adding a road at the selected build site.
				if (mainPanel.getSelectedBuildSite().getPlayerID() == clientManager.getPlayerIndex()
						&& !selectingRoad) {
					Thread addRoadThread = new Thread(){
						public void run(){
							addRoad();
						}
					};
					addRoadThread.start();
				} else {
					selectingRoad = false;
				}

			}

	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		if (enabledIO && e.getKeyChar() == 'b')
			if (mainPanel.getSelectedBuildSite().buildSettlement(clientManager.getPlayerIndex()))
				System.out.println("Building settlement.");
			else
				System.out.println("Failed to build settlement.");

		// finding the proper build site to send which needs to be updated.
		for (int i = 0; i < clientManager.getGameBoard().getBuildSites().size(); i++)
			for (int n = 0; n < clientManager.getGameBoard().getBuildSites().get(i).size(); n++)
				if (clientManager.getGameBoard().getBuildSites().get(i).get(n) == mainPanel.getSelectedBuildSite()) {
					// telling the server to updated the build sites.
					clientManager.sendUpdatedBuildSite(n, i);
					break;
				}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

}
