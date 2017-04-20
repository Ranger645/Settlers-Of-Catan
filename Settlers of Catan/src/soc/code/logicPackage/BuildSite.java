package soc.code.logicPackage;

public class BuildSite {

	// These are the x and y coordinates of the build sites that will be used
	// for determining if they were clicked on or where the building that is on
	// top of them goes.
	private int x, y = 0;
	
	public BuildSite(int xPos, int yPos){
		x = xPos;
		y = yPos;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
}
