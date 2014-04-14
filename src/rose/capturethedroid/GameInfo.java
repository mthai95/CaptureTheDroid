package rose.capturethedroid;


/**
 * Holds info from the server.
 * @author millerlj.
 *         Created Apr 12, 2014.
 */
public class GameInfo {
	boolean flagholder;
	Coordinate[] flagpos;
	int win;//-1:game running, 0:team 0 wins,1:team 1 wins
	
	
	public GameInfo(boolean flagholder,Coordinate[] flagpos,int win) {
		this.flagholder=flagholder;
		this.flagpos=flagpos;
		this.win=win;
	}
	
	@Override
	public String toString() {
		return "" + this.flagholder + ", " + this.flagpos.toString() + ", " + this.win;
	}
}
