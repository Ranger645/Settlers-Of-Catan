package soc.code.logicPackage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Tile {

	// The height must be the with times 2/sqrt(3)
	public static final int TILE_WIDTH = 100;
	public static final int TILE_HEIGHT = 115;

	private BuildSite[] tileBuildSites = null;
	// this number is strictly for the purposes of taking in the build site
	// objects. It will always be 6 for every tile after all buildsites have
	// been added.
	private int numberOfBuildSites = 0;

	private static BufferedImage t_WoodImage;
	private static BufferedImage t_BrickImage;
	private static BufferedImage t_OreImage;
	private static BufferedImage t_SheepImage;
	private static BufferedImage t_WheatImage;
	private static BufferedImage t_DesertImage;

	private static BufferedImage c_WoodImage;
	private static BufferedImage c_BrickImage;
	private static BufferedImage c_OreImage;
	private static BufferedImage c_SheepImage;
	private static BufferedImage c_WheatImage;
	private static BufferedImage c_CardBackImage;

	protected RESOURCE_TYPE type;
	// this is the numeber that the dice needs to be rolled to gain this
	// resource. If this tile is a desert then this number is -1
	private int resourceNumber = -1;

	public Tile(RESOURCE_TYPE Type) {
		type = Type;
	}

	/**
	 * This method adds the given build site reference to the array of build
	 * site references that is held by this tile. They are all the build sites
	 * around the tile.
	 * 
	 * @param BS
	 *            - The build site to be added.
	 */
	public void addTileBuildSite(BuildSite BS) {
		if (numberOfBuildSites == 0)
			tileBuildSites = new BuildSite[6];
		tileBuildSites[numberOfBuildSites++] = BS;
	}

	public static void initializeResourceImages() {
		// initializing the images:
		try {
			t_WoodImage = ImageIO.read(new File("resources\\images\\tile_Wood.png"));
			t_WheatImage = ImageIO.read(new File("resources\\images\\tile_Wheat.png"));
			t_SheepImage = ImageIO.read(new File("resources\\images\\tile_Sheep.png"));
			t_BrickImage = ImageIO.read(new File("resources\\images\\tile_Brick.png"));
			t_OreImage = ImageIO.read(new File("resources\\images\\tile_Ore.png"));
			t_DesertImage = ImageIO.read(new File("resources\\images\\tile_Desert.png"));

			c_WoodImage = ImageIO.read(new File("resources\\images\\card_Wood.png"));
			c_WheatImage = ImageIO.read(new File("resources\\images\\card_Wheat.png"));
			c_SheepImage = ImageIO.read(new File("resources\\images\\card_Sheep.png"));
			c_BrickImage = ImageIO.read(new File("resources\\images\\card_Brick.png"));
			c_OreImage = ImageIO.read(new File("resources\\images\\card_Ore.png"));
			c_CardBackImage = ImageIO.read(new File("resources\\images\\card_DefaultBacking.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("[ERROR] Unable to read tile image file.");
			e.printStackTrace();
		}
	}

	public RESOURCE_TYPE getType() {
		return type;
	}

	public static enum RESOURCE_TYPE {
		WOOD, WHEAT, BRICK, SHEEP, ORE, DESERT
	}

	public String toString() {
		switch (type) {
		case WOOD:
			return "wood";
		case WHEAT:
			return "wheat";
		case BRICK:
			return "brick";
		case SHEEP:
			return "sheep";
		case ORE:
			return "ore";
		case DESERT:
			return "desert";
		}
		return "null";
	}

	/**
	 * @return the integer corresponding to the type of resource tile that this
	 *         is.
	 */
	public int toTypeValue() {
		switch (type) {
		case WOOD:
			return 0;
		case WHEAT:
			return 1;
		case BRICK:
			return 2;
		case SHEEP:
			return 3;
		case ORE:
			return 4;
		case DESERT:
			return -1;
		}
		return -1;
	}
	
	public static String idToString(int id){
		switch (id) {
		case 0:
			return "Wood";
		case 1:
			return "Wheat";
		case 2:
			return "Brick";
		case 3:
			return "Sheep";
		case 4:
			return "Ore";
		}
		return "";
	}

	public static BufferedImage typeValueToCardImage(int val) {
		switch (val) {
		case 0:
			return getC_WoodImage();
		case 1:
			return getC_WheatImage();
		case 2:
			return getC_BrickImage();
		case 3:
			return getC_SheepImage();
		case 4:
			return getC_OreImage();
		}
		return null;
	}

	public static BufferedImage typeValueToTileImage(int val) {
		switch (val) {
		case 0:
			return getT_WoodImage();
		case 1:
			return getT_WheatImage();
		case 2:
			return getT_BrickImage();
		case 3:
			return getT_SheepImage();
		case 4:
			return getT_OreImage();
		default:
			return getT_DesertImage();
		}
	}

	public static BufferedImage getT_WoodImage() {
		return t_WoodImage;
	}

	public static BufferedImage getT_BrickImage() {
		return t_BrickImage;
	}

	public static BufferedImage getT_OreImage() {
		return t_OreImage;
	}

	public static BufferedImage getT_SheepImage() {
		return t_SheepImage;
	}

	public static BufferedImage getT_WheatImage() {
		return t_WheatImage;
	}

	public static BufferedImage getT_DesertImage() {
		return t_DesertImage;
	}

	public static BufferedImage getC_WoodImage() {
		return c_WoodImage;
	}

	public static BufferedImage getC_BrickImage() {
		return c_BrickImage;
	}

	public static BufferedImage getC_OreImage() {
		return c_OreImage;
	}

	public static BufferedImage getC_SheepImage() {
		return c_SheepImage;
	}

	public static BufferedImage getC_WheatImage() {
		return c_WheatImage;
	}

	public static BufferedImage getC_CardBackImage() {
		return c_CardBackImage;
	}

	public static int getTileWidth() {
		return TILE_WIDTH;
	}

	public static int getTileHeight() {
		return TILE_HEIGHT;
	}

	public BuildSite[] getTileBuildSites() {
		return tileBuildSites;
	}

	public int getNumberOfBuildSites() {
		return numberOfBuildSites;
	}

	public void setResourceNumber(int resourceNumber) {
		this.resourceNumber = resourceNumber;
	}

	public int getResourceNumber() {
		return resourceNumber;
	}

}
