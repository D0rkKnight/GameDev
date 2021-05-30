package Wrappers;

import java.util.ArrayList;

import Entities.PlayerPackage.Player;
import Entities.PlayerPackage.PlayerCB;
import GameController.GameManager;
import GameController.Time;

/**
 * Outlines a set of frame segments that can be used for animation and game
 * logic
 * 
 * Frame segments may overlap
 * 
 * @author Hanzen Shou
 *
 */
public class FrameData {
	public static enum FrameTag {
		INACTABLE, DASH_CANCELLABLE, MOVE_CANCELLABLE, MOVEABLE
	}

	public static class Event {
		public PlayerCB cb;
		public int frame;

		public Event(PlayerCB cb, int frame) {
			this.cb = cb;
			this.frame = frame;
		}
	}

	public static class FrameSegment {
		public int fLength;
		public int fStart;
		public FrameTag[] tags;

		public PlayerCB cb;

		public FrameSegment(int fLength, int fStart, FrameTag[] tags) {
			this.fLength = fLength;
			this.fStart = fStart;
			this.tags = tags;
		}

		public FrameSegment(int fLength, int fStart) {
			this(fLength, fStart, new FrameTag[] {});
		}

		public FrameSegment(int fLength, int fStart, FrameTag tag) {
			this(fLength, fStart, new FrameTag[] { tag });
		}
	}

	private ArrayList<FrameSegment> segments;
	private ArrayList<Event> events;
	private float currContFrame = 0; // Frames are discrete but time is advanced continuously because game framerate
										// is divorced from action framerate

	private float fEnd;
	private boolean looping;
	public PlayerCB cb; // General callback for every invoke

	public PlayerCB onEntry; // Called on first available frame, is called before everything else
	public boolean entryInvoked = false;

	public FrameData(ArrayList<FrameSegment> segments, ArrayList<Event> events, boolean looping) {
		this.segments = segments;
		this.events = events;

		this.looping = looping;
		fEnd = totalFLength();
	}

	public FrameData(ArrayList<FrameSegment> segments, ArrayList<Event> events) {
		this(segments, events, false);
	}

	public void update(Player player) {
		if (!entryInvoked && onEntry != null) {
			onEntry.invoke(player);
			entryInvoked = true;
		}

		if (cb != null)
			cb.invoke(player);

		float fDelta = TDeltaToFrame(Time.deltaT());

		// Invoke events along the way
		for (Event e : events) {
			if (e.frame >= currContFrame && e.frame < currContFrame + fDelta) {
				e.cb.invoke(player);

				// TODO: These are NOT invoked in order!!!
			}
		}

		// Invoke attached segments too
		for (FrameSegment s : segments) {
			if (s.cb != null && s.fStart <= currContFrame && s.fStart + s.fLength > currContFrame) {
				s.cb.invoke(player);
			}
		}

		this.currContFrame += fDelta;

		// Loop here
		if (looping) {
			if (currContFrame >= fEnd) {
				currContFrame -= fEnd;
			}
		}
	}

	public float getCurrFrame() {
		return currContFrame;
	}

	public void fullReset() {
		entryInvoked = false;
		currContFrame = 0;
	}

	/**
	 * Retrieve all tags at the current frame the object is at
	 * 
	 * @return
	 */
	public boolean[] getCurrTags() {
		// Boolean mask for tags, use ordinal value of enum to access
		boolean[] o = new boolean[FrameTag.values().length];

		for (FrameSegment seg : segments) {

			// If in this tag's timeframe
			if (currContFrame >= seg.fStart && currContFrame < seg.fStart + seg.fLength) {
				for (FrameTag tag : seg.tags) {
					o[tag.ordinal()] = true;
				}
			}
		}

		return o;
	}

	/**
	 * Retrieve the distance from f0 to the end of the last segment.
	 * 
	 * @return
	 */
	public int totalFLength() {
		int max = 0;
		for (FrameSegment seg : segments) {
			max = Math.max(seg.fStart + seg.fLength, max);
		}

		return max;
	}

	public static long frameToTDelta(float f) {
		return (long) (f / GameManager.COMBAT_FPS * 1000);
	}

	public static float TDeltaToFrame(long td) {
		return (float) td * GameManager.COMBAT_FPS / 1000f;
	}
}
