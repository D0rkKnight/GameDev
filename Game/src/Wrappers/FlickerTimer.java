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
				
				if (isPeak) sprRen.col = troughColor;
				if (!isPeak) sprRen.col = peakColor;
				
				isPeak = !isPeak;
			}
		};
		
		TimerCallback resetCb = new TimerCallback() {

			@Override
			public void invoke(Timer timer) {
				// TODO Auto-generated method stub
				sprRen.col = new Color();
			}
			
		};
		cbs.add(resetCb);
		
		subTimers.add(new Timer(flickerLength, flickerCb));
	}

}
