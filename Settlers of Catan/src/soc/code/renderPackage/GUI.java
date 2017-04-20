package soc.code.renderPackage;

import javax.swing.JFrame;

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
		//this.setResizable(false);

		mainPanel = new GamePanel(gameBoard);
		this.add(mainPanel);

		this.setVisible(true);
	}

}
