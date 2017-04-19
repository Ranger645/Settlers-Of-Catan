package soc.code.renderPackage;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import soc.code.logicPackage.Board;
import soc.code.logicPackage.Tile;

/**
 * This class will be responsible for rendering the game itself based on the
 * board object and the Player objects. It will also pass down the mouse events
 * from the GUI JFrame to the UI that is stored inside of this panel. At the end
 * of this class' paint method will be a call to the UI's paint method. The
 * class will also be responsible for initializing and controlling the UI
 * Object.
 * 
 * @author Greg
 */
public class GamePanel extends JPanel {

	// the reference to the board used in this game.
	Board gameBoard = null;

	// constructor requires reference to board that will be generated.
	public GamePanel(Board GB) {
		gameBoard = GB;

		// initializing the images:
		Tile.initializeResourceImages();
	}

	public void paint(Graphics g) {

		// drawing the background:
		g.setColor(Color.BLUE);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());

		// The extremes:
		final int STARTING_X = Tile.TILE_WIDTH / 2;
		final int STARTING_Y = Tile.TILE_HEIGHT / 2;
		int currentXRow = STARTING_X;

		// This is an example for loop for navigating the board array.
		for (int i = -2; i < gameBoard.numberOfRows() - 2; i++) {
			currentXRow = STARTING_X + Math.abs(i) * Tile.TILE_WIDTH / 2 - Tile.TILE_WIDTH;
			for (int n = 0; n < gameBoard.numberOfColumns(i + 2); n++) {
				System.out.println((i + 2) + ", " + n + "  " + gameBoard.getTileAt(i + 2, n).toString());
				switch (gameBoard.getTileAt(i + 2, n).getType()) {
				case WOOD:
					g.setColor(Color.GREEN);
					break;
				case WHEAT:
					g.setColor(Color.YELLOW);
					break;
				case BRICK:
					g.setColor(Color.RED);
					break;
				case ORE:
					g.setColor(Color.DARK_GRAY);
					break;
				case SHEEP:
					g.setColor(Color.WHITE);
					break;
				case DESERT:
					g.setColor(Color.LIGHT_GRAY);
					break;
				}
				drawHexagon((Graphics2D) g, Tile.TILE_HEIGHT / 2, currentXRow += Tile.TILE_WIDTH,
						STARTING_Y + (i + 2) * 3 * Tile.TILE_HEIGHT / 4);
			}
		}
	}

	/**
	 * This method draws a regular hexagon with the given center x,y point and
	 * the given side length on the specified graphics object. The hexagon it
	 * draws has one of its points facing down.
	 * 
	 * @param g2
	 *            the graphics that the hexagon will be drawn on.
	 * @param sideLength
	 *            the side length of the regular hexagon
	 * @param centerY
	 *            the y value of the center of the hexagon
	 * @param centerX
	 *            the x value of the center of the hexagon.
	 */
	private void drawHexagon(Graphics2D g2, int sideLength, int centerY, int centerX) {
		int[] xValues = { centerX - sideLength, centerX - sideLength / 2, centerX + sideLength / 2,
				centerX + sideLength, centerX + sideLength / 2, centerX - sideLength / 2 };
		double yOffset = sideLength * Math.sqrt(3) / 2;
		int[] yValues = { centerY, (int) (centerY - yOffset), (int) (centerY - yOffset), centerY,
				(int) (centerY + yOffset), (int) (centerY + yOffset) };
		g2.fillPolygon(yValues, xValues, 6);
		g2.setColor(Color.BLACK);
		g2.setStroke(new BasicStroke(3));
		g2.drawPolygon(yValues, xValues, 6);
		g2.setStroke(new BasicStroke(1));
	}

}
