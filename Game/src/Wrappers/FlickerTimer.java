package Wrappers;

import Entities.Entity;
import Rendering.SpriteRenderer;

public class FlickerTimer extends Timer {

	public Color peakColor;
	public Color troughColor;
	public boolean isPeak;
	public Entity owner;
	
	public FlickerTimer(long loopLength, long flickerLength, Color peakColor, Color troughColor, Entity owner, TimerCallback masterCb) {
		super(loopLength, masterCb); //master CB still needed to free the loop.
		
		this.owner = owner;
		SpriteRenderer sprRen = (SpriteRenderer) owner.renderer;
		
		TimerCallback flickerCb = new TimerCallback() {
			@Override
			public void invoke(Timer timer) {
				Color newCol = null;
				if (isPeak) newCol = troughColor;
				if (!isPeak) newCol = peakColor;
				sprRen.updateColors(newCol);
				
				isPeak = !isPeak;
			}
		};
		
		TimerCallback resetCb = new TimerCallback() {

			@Override
			public void invoke(Timer timer) {
				// TODO Auto-generated method stub
				sprRen.updateColors(new Color());
			}
			
		};
		cbs.add(resetCb);
		
		subTimers.add(new Timer(flickerLength, flickerCb));
	}

}
