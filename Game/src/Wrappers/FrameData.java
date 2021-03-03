package Wrappers;

import java.util.ArrayList;

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
		INACTABLE, DASH_CANCELLABLE, MOVE_CANCELLABLE, HITBOX_OUT
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
	}

	private ArrayList<FrameSegment> segments;
	private int currFrame = 0;

	public FrameData(ArrayList<FrameSegment> segments) {
		this.segments = segments;
	}

	public void advanceFrames(int fCount) {
		this.currFrame = fCount;
	}

	public int getCurrFrame() {
		return currFrame;
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
