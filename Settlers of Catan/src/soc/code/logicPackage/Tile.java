package soc.code.logicPackage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Tile {
	
	//The height must be the with times 2/sqrt(3)
	public static final int TILE_WIDTH = 100;
	public static final int TILE_HEIGHT = 115; 
	
	private static BufferedImage t_WoodImage;
	private static BufferedImage t_BrickImage;
	private static BufferedImage t_OreImage;
	private static BufferedImage t_SheepImage;
	private static BufferedImage t_WheatImage;
	private static BufferedImage t_DesertImage;
	
	protected RESOURCE_TYPE type;

	public Tile(RESOURCE_TYPE Type) {
		type = Type;
		
//		//initializing the images:
//		try {
//			t_WoodImage = ImageIO.read(new File("resources\\images\\tile_Wood.png"));
//			t_WheatImage = ImageIO.read(new File("resources\\images\\tile_Wheat.png"));
//			t_SheepImage = ImageIO.read(new File("resources\\images\\tile_Sheep.png"));
//			t_BrickImage = ImageIO.read(new File("resources\\images\\tile_Brick.png"));
//			t_OreImage = ImageIO.read(new File("resources\\images\\tile_Ore.png"));
//			t_DesertImage = ImageIO.read(new File("resources\\images\\tile_Desert.png"));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public RESOURCE_TYPE getType() {
		return type;
	}

	public static enum RESOURCE_TYPE {
		WOOD, WHEAT, BRICK, SHEEP, ORE, DESERT
	}
	
	public String toString(){
		switch (type){
		case WOOD:
			return "Wood";
		case WHEAT:
			return "Wheat";
		case BRICK:
			return "Brick";
		case SHEEP:
			return "Sheep";
		case ORE:
			return "Ore";
		case DESERT:
			return "Desert";
		}
		return "null";
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

}
