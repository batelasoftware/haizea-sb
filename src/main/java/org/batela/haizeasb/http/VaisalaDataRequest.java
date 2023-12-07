package org.batela.haizeasb.http;

import java.time.LocalDate;
import java.util.Date;

public class VaisalaDataRequest {
	private Float speed;
	private Float direction;
	private LocalDate creationDate;
	private String dataDateStr = null;
	
	public VaisalaDataRequest () {
		
	}
	
	public VaisalaDataRequest(Float a, Float b, LocalDate creationDate) {
		super();
		this.setSpeed(a);
		this.setDirection(b);
		this.setCreationDate(creationDate);
	}
	
	public VaisalaDataRequest(Float a, Float b, String creationDate) {
		super();
		this.setSpeed(a);
		this.setDirection(b);
		this.setDataDateStr(creationDate);
	}

	/**
	 * @return the name
	 */
	
	@Override
	public String toString (){
		return "BootRequest{" +
				"speed=" + speed +
				"direction=" + direction +
				"date="+ dataDateStr +	"}" ;
	}

	public Float getDirection() {
		return direction;
	}

	public void setDirection(Float direction) {
		this.direction = direction;
	}

	public Float getSpeed() {
		return speed;
	}

	public void setSpeed(Float speed) {
		this.speed = speed;
	}


	public LocalDate getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(LocalDate creationDate) {
		this.creationDate = creationDate;
	}

	public String getDataDateStr() {
		return dataDateStr;
	}

	public void setDataDateStr(String dataDateStr) {
		this.dataDateStr = dataDateStr;
	}

	
}
