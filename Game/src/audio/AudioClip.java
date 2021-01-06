package audio;

import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 * Controls the playback of one clip.
 * 
 * @author Hanzen Shou
 *
 */
public class AudioClip {

	private Clip clip;

	public AudioClip(String url) {
		try {
			AudioInputStream inStream = AudioSystem
					.getAudioInputStream(new File("assets/Audio/" + url).getAbsoluteFile());
			clip = AudioSystem.getClip();

			clip.open(inStream);

			clip.loop(Clip.LOOP_CONTINUOUSLY);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void start() {
		clip.start();
	}

	public void setVolume(float vol) {
		FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		gainControl.setValue(vol);
	}
}
