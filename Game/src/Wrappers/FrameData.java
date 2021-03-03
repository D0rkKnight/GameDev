package Wrappers;

import java.util.ArrayList;

import Utility.Callback;

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
		INACTABLE, DASH_CANCELLABLE, MOVE_CANCELLABLE
	}

	public static class Event {
		public Callback cb;
		public int frame;

		public Event(Callback cb, int frame) {
			this.cb = cb;
			this.frame = frame;
		}
	}

	public static class FrameSegment {
		public int fLength;
		public int fStart;
		public FrameTag[] tags;

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
	private int currFrame = 0;

	public FrameData(ArrayList<FrameSegment> segments, ArrayList<Event> events) {
		this.segments = segments;
		this.events = events;
	}

	public void advanceFrames(int fCount) {
		// Invoke events along the way
		for (Event e : events) {
			if (e.frame >= currFrame && e.frame < currFrame + fCount) {
				e.cb.invoke();

				// TODO: These are NOT invoked in order!!!
			}
		}

		this.currFrame += fCount;
	}

	public int getCurrFrame() {
		return currFrame;
	}

	public void reset() {
		currFrame = 0;
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
			if (currFrame >= seg.fStart && currFrame < seg.fStart + seg.fLength) {
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
}
