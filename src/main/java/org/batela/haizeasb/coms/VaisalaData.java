package org.batela.haizeasb.coms;

import java.time.Instant;

public class VaisalaData {
	
	private Float windspeed;
	private Float winddirec;
	private Instant dt; 

	public VaisalaData (String dt, Float ws, Float wd ) {
//		this.dt = dt;
		this.windspeed = ws;
		this.winddirec = wd;
	}
	public Float getWindspeed() {
		return windspeed;
	}
	public void setWindspeed(Float windspeed) {
		this.windspeed = windspeed;
	}
	public Float getWinddirec() {
		return winddirec;
	}
	public void setWinddirec(Float winddirec) {
		this.winddirec = winddirec;
	}
	public Instant getDt() {
		return dt;
	}
	public void setDt(Instant dt) {
		this.dt = dt;
	}
}
