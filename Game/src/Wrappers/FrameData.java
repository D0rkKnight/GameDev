package Wrappers;

import java.util.ArrayList;
import java.util.Collections;

import Entities.Framework.ECP;
import Entities.Framework.Entity;
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
	public static class Event {
		public ECP cb;
		public int frame;

		public Event(ECP<?> cb, int frame) {
			this.cb = cb;
			this.frame = frame;
		}
	}

	public static class FrameSegment {
		public int fLength;
		public int fStart;
		public ArrayList<ECP> cbs;

		public FrameSegment(int fLength, int fStart, ECP[] cbs) {
			this.fLength = fLength;
			this.fStart = fStart;

			this.cbs = new ArrayList<>();
			Collections.addAll(this.cbs, cbs);
		}

		public FrameSegment(int fLength, int fStart) {
			this(fLength, fStart, new ECP[] {});
		}

		public FrameSegment(int fLength, int fStart, ECP cb) {
			this(fLength, fStart, new ECP[] { cb });
		}
	}

	private ArrayList<FrameSegment> segments;
	private ArrayList<Event> events;
	private float currContFrame = 0; // Frames are discrete but time is advanced continuously because game framerate
										// is divorced from action framerate

	private float fEnd;
	private boolean looping;
	public ECP cb; // General callback for every invoke

	public ECP onEntry; // Called on first available frame, is called before everything else
	public ECP onExit; // Both this and onEntry are invoked externally.
	public ECP onEnd;
	public boolean endCBCalled = false;

	public FrameData(ArrayList<FrameSegment> segments, ArrayList<Event> events, boolean looping) {
		this.segments = segments;
		this.events = events;

		this.looping = looping;
		fEnd = totalFLength();
	}

	public FrameData(ArrayList<FrameSegment> segments, ArrayList<Event> events) {
		this(segments, events, false);
	}

	public void update(Entity caller) {
		if (cb != null)
			cb.invoke(caller);

		float fDelta = TDeltaToFrame(Time.deltaT());

		// Invoke events along the way
		for (Event e : events) {
			if (e.frame >= currContFrame && e.frame < currContFrame + fDelta) {
				e.cb.invoke(caller);

				// TODO: These are NOT invoked in order!!!
			}
		}

		// Invoke attached segments too
		for (FrameSegment s : segments) {
			if (s.cbs.size() > 0 && s.fStart <= currContFrame && s.fStart + s.fLength > currContFrame) {
				for (ECP ecb : s.cbs)
					if (ecb != null)
						ecb.invoke(caller);
			}
		}

		// Advance time
		this.currContFrame += fDelta;

		// Loop here
		if (currContFrame >= fEnd) {
			if (looping) {
				currContFrame -= fEnd;
			}

			// Used for stitching together states if one ends through full completion.
			else if (!endCBCalled && onEnd != null) {
				onEnd.invoke(caller);
				endCBCalled = true;
			}
		}

	}

	public float getCurrFrame() {
		return currContFrame;
	}

	public void fullReset() {
		currContFrame = 0;
		endCBCalled = false;
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
