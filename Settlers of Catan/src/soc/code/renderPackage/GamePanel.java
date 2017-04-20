package soc.code.renderPackage;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JPanel;

import soc.code.logicPackage.Board;
import soc.code.logicPackage.BuildSite;
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
public class GamePanel extends JPanel implements MouseListener {

	// the reference to the board used in this game.
	private Board gameBoard = null;
	// this coordinate is the top left x,y coordinate of the actual tiles. There
	// are no tiles connected to it becuase it is a hexagon but it is the lowest
	// x and lowest y that any of the tiles are drawn in.
	private Point tileZeroPoint = null;

	private final Color BACKGROUND_COLOR = new Color(68, 199, 255);

	// constructor requires reference to board that will be generated.
	public GamePanel(Board GB) {
		gameBoard = GB;

		this.addMouseListener(this);

		// setting the size and border spacing:
		this.setSize(Tile.TILE_WIDTH * 6, Tile.TILE_WIDTH * 6);
		tileZeroPoint = new Point(this.getWidth() / 2 - Tile.TILE_WIDTH * 5 / 2 - 1,
				this.getHeight() / 2 - Tile.TILE_WIDTH * 5 / 2 - 1);

		// setting the positions of each of the buildsites:
		setBuildSitePostitions(gameBoard.getBuildSites());

		// initializing the images:
		Tile.initializeResourceImages();
	}

	/**
	 * This method will be called when the board is initialized and will be in
	 * charge of setting the x and y positions of each of the build sites in the
	 * array.
	 * 
	 * @param sites
	 *            the array lists that need to have their contents' positions
	 *            set.
	 */
	private void setBuildSitePostitions(ArrayList<ArrayList<BuildSite>> sites) {
		
	}

	public void paint(Graphics g) {
		// drawing the background:
		g.setColor(BACKGROUND_COLOR);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());

		// The extremes:
		final int STARTING_X = Tile.TILE_WIDTH / 2;
		final int STARTING_Y = Tile.TILE_HEIGHT / 2;
		int currentXRow = STARTING_X;

		// This is an example for loop for navigating the board array.
		for (int i = -2; i < gameBoard.numberOfRows() - 2; i++) {
			currentXRow = STARTING_X + Math.abs(i) * Tile.TILE_WIDTH / 2 - Tile.TILE_WIDTH;
			for (int n = 0; n < gameBoard.numberOfColumns(i + 2); n++) {
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
				drawHexagon((Graphics2D) g, Tile.TILE_HEIGHT / 2,
						(int) ((currentXRow += Tile.TILE_WIDTH) + tileZeroPoint.getX()),
						(int) (STARTING_Y + (i + 2) * 3 * Tile.TILE_HEIGHT / 4 + tileZeroPoint.getY()));
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
		g2.setStroke(new BasicStroke(6));
		g2.drawPolygon(yValues, xValues, 6);
		g2.setStroke(new BasicStroke(1));
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		System.out.println(e.getX() + "  " + e.getY());
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

}
