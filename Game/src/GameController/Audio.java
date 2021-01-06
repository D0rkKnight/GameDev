package GameController;

import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 * Centralized controller for audio
 * 
 * @author Hanzen Shou
 *
 */
public class Audio {

	static Clip clip;

	public static void init() {
		playClip("Crystal_Desert_16.wav");

		FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		gainControl.setValue(-20f);
	}

	public static void playClip(String url) {
		try {
			AudioInputStream inStream = AudioSystem
					.getAudioInputStream(new File("assets/Audio/" + url).getAbsoluteFile());
			clip = AudioSystem.getClip();

			clip.open(inStream);

			clip.loop(Clip.LOOP_CONTINUOUSLY);
			clip.start();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
