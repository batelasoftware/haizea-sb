package org.batela.haizeasb.coms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisplayData {
///	TODO
	private String Message;
	private Float windSpeed;
	private Float windDirec;
	private String date;

	private static final Logger logger = LoggerFactory.getLogger(DisplayData.class);
	DisplayData (String data){
		this.Message = data;
	}

	public String getMessage() {
		return Message;
	}

	public void setMessage(String message) {
		Message = message;
	}
	

	public Integer getWindSpeed() {
		return new Integer((int) (windSpeed * 10));
	}

	public void setWindSpeed(Float windSpeed) {
		this.windSpeed = windSpeed;
	}

	public Integer getWindDirec() {
		return new Integer((int) (windDirec*1));
		//return windDirec;
	}

	public void setWindDirec(Float windDirec) {
		this.windDirec = windDirec;
	}
	
	public boolean parseMessage () {
		
		boolean res = true;
		try {
			this.logger.info("Message to be parsed:->" + this.Message);
			
			String [] vdata_split = this.Message.split(";");
			//this.logger.info("Parsed WindSpeed: " + vdata_split[0]);
			
			String [] aux = vdata_split[0].split(" ");
			
			//this.logger.info("Parsed WindSpeed: " + aux[1]);
			this.windSpeed = new Float(aux[1]);
			
			aux = vdata_split[1].split(" ");
			//this.logger.info("Parsed WindDirection: " + aux[1]);
			this.windDirec = new Float(aux[1]);
		}
		catch (Exception e) {
			e.printStackTrace();
			res = false;
		}
		return res;
		
		
		
		
		
		
		
	}	
}
