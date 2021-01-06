package audio;

/**
 * Centralized controller for audio
 * 
 * @author Hanzen Shou
 *
 */
public class Audio {

	public static void init() {
		AudioClip aud = new AudioClip("Crystal_Desert_16.wav");
		aud.start();
		aud.setVolume(-20f);
	}

	public static void update() {

	}
}
