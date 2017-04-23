package soc.code.renderPackage;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import soc.code.logicPackage.Board;
import soc.code.logicPackage.Tile;

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
public class GUI extends JFrame {

	private GamePanel mainPanel = null;

	public GUI(Board gameBoard) {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(Tile.TILE_WIDTH * 6, Tile.TILE_WIDTH * 6);
		this.setTitle("Stettlers of Catan");
		// this.setResizable(false);

		mainPanel = new GamePanel(gameBoard);
		this.add(mainPanel);

		// JMenuBar menuBar = new JMenuBar();
		// menuBar.setPreferredSize(new Dimension(this.getWidth(), 25));
		// JMenu commandMenu = new JMenu("Commands");
		// JMenuItem buildSettlement = new JMenuItem("Build Settlement");
		// buildSettlement.addActionListener(new ActionListener() {
		// @Override
		// public void actionPerformed(ActionEvent e) {
		// if (mainPanel.getSelectedBuildSite().buildSettlement(1))
		// System.out.println("Building settlement.");
		// else
		// System.out.println("Failed to build settlement.");
		// }
		// });
		//
		// commandMenu.add(buildSettlement);
		// menuBar.add(commandMenu);
		// this.setJMenuBar(menuBar);

		this.setVisible(true);
	}

}
