package Graphics.Elements;

public class SpriteSheetSection {
	public int[] topleft;
	public int[] topright;
	public int[] botleft;
	public int[] botright;
	public SpriteSheetSection(int[] topleft, int[] topright, int[] botleft, int[] botright) {
		this.topleft = topleft;
		this.topright = topright;
		this.botleft = botleft;
		this.botright = botright;
	}
}
