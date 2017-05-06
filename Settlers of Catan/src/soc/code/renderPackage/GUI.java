package soc.code.renderPackage;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import soc.code.logicPackage.Board;
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
	private JMenuItem buildSettlement, endTurn, buildCity = null;

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
			endTurn = new JMenuItem("End Turn");
			endTurn.addActionListener(this);

			this.addKeyListener(this);

			commandMenu.add(buildSettlement);
			commandMenu.add(buildCity);
			commandMenu.add(endTurn);
			menuBar.add(commandMenu);
			this.setJMenuBar(menuBar);

			// starting the IO disabled until it is the client's turn.
			closeIO();
		}

		// only shows the board if it is a client doing the controlling.
		if (!isHost)
			this.setVisible(true);
	}

	/**
	 * will enable all of the components that the user can interact with
	 */
	public void openIO() {
		buildSettlement.setEnabled(true);
		buildCity.setEnabled(true);
		endTurn.setEnabled(true);
		enabledIO = true;
	}

	/**
	 * will disable all of the components that the user can interact with
	 */
	public void closeIO() {
		buildSettlement.setEnabled(false);
		buildCity.setEnabled(false);
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

	@Override
	public void actionPerformed(ActionEvent e) {
		// A button on the gui has been pressed and therefore must have its
		// action executed. This means that it is this clients turn because
		// buttons are only enabled on the clients turn. Every time a button is
		// pressed the client must send the updated build site array lists to
		// the server so the server can distribute the updated ones to the
		// client.
		if (enabledIO) {
			if (e.getSource() == buildSettlement) {
				if (mainPanel.getSelectedBuildSite().buildSettlement(clientManager.getPlayerIndex())) {
					System.out.println("Building settlement.");
					// Finding the proper build site to send which needs to be
					// updated.
					for (int i = 0; i < clientManager.getGameBoard().getBuildSites().size(); i++)
						for (int n = 0; n < clientManager.getGameBoard().getBuildSites().get(i).size(); n++)
							if (clientManager.getGameBoard().getBuildSites().get(i).get(n) == mainPanel
									.getSelectedBuildSite()) {
								// telling the server to updated the build
								// sites.
								clientManager.sendUpdatedBuildSite(n, i);
								break;
							}
				} else
					System.out.println("Failed to build settlement.");
			} else if (e.getSource() == endTurn) {
				// ends the turn.
				clientManager.endTurn();
			} else if (e.getSource() == buildCity) {
				// building a city.
				if (mainPanel.getSelectedBuildSite().buildCity(clientManager.getPlayerIndex())) {
					System.out.println("Building City.");
					// Finding the proper build site to send which needs to be
					// updated.
					for (int i = 0; i < clientManager.getGameBoard().getBuildSites().size(); i++)
						for (int n = 0; n < clientManager.getGameBoard().getBuildSites().get(i).size(); n++)
							if (clientManager.getGameBoard().getBuildSites().get(i).get(n) == mainPanel
									.getSelectedBuildSite()) {
								// telling the server to updated the build
								// sites.
								clientManager.sendUpdatedBuildSite(n, i);
								break;
							}
				} else
					System.out.println("Failed to build City.");
			}
		} else
			System.out.println("It is not your turn.");

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
