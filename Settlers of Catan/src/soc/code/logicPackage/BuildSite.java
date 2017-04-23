package soc.code.logicPackage;

public class BuildSite {

	// These are the x and y coordinates of the build sites that will be used
	// for determining if they were clicked on or where the building that is on
	// top of them goes.
	private int x, y = 0;
	// This is the variable that determines what type of building is located on
	// this point. A 0 is no building, a 1 is a settlment, and a 2 is a city.
	private int buildingType = 0;
	// This is the variable that stores the ID of the player that this buildsite
	// belongs to. A -1 means that it does not belong to any player because
	// nothing has beeen built on it yet.
	private int playerID = -1;

	public BuildSite(int xPos, int yPos) {
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

	public int getBuildingType() {
		return buildingType;
	}

	public int getPlayerID() {
		return playerID;
	}

	public boolean buildSettlement(int playerID) {
		if (this.playerID == -1) {
			this.playerID = playerID;
			this.buildingType++;
			return true;
		} else
			return false;
	}

	public boolean buildCity() {
		if (this.buildingType == 1) {
			this.buildingType++;
			return true;
		} else
			return false;
	}

}
