package org.batela.haizeasb.coms;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jssc.SerialPortException;

public class VaisalaData {
		
	static final Logger logger = LogManager.getLogger(VaisalaData.class); 
	private char [] data = new char [250];
	private int data_len  = 0;
	
	private Float windspeed;
	private Float winddirec;
	private Instant dt; 

	private LocalDateTime dataDate = null;
    
    private DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private String dataDateStr = null;
    
	public VaisalaData () {
		this.data_len = 0 ;
	}
	public VaisalaData (String dt, Float ws, Float wd ) {
//		this.dt = dt;
		this.windspeed = ws;
		this.winddirec = wd;
	}
	public boolean parse () {
		boolean res = false;
		try {
		
			String vdata = new String (this.data);
			String [] vdata_split = vdata.split(",");
			
			if (vdata_split[0].compareTo("0R1")!= 0) {
				this.logger.error("Cabecera de mensaje erroneo: " + new String(this.data));
				this.reset();
				return false;
			}
				
			if (vdata_split.length != 7) {
				this.logger.error("Longitud de mensaje erroneo: " + new String(this.data));
				this.reset();
				return false ;
			}
				
			String wdir = vdata_split[2].split("=")[1];
			String wvel = vdata_split[5].split("=")[1];
			wdir = wdir.substring(0, wdir.length()-1);
			wvel = wvel.substring(0, wvel.length()-1);
			
			this.winddirec = Float.valueOf(wdir);
			this.windspeed = Float.valueOf(wvel);
			
			this.dataDate = LocalDateTime.now();
			this.setDataDateStr(this.dataDate.format(myFormatObj));
			
			res = true;
		}
		catch (Exception e) {
			this.logger.error("Excepcion parseando Mensaje: " + new String(this.data));
			res = false;
		}
		
		this.reset();
		return res;	
	}
	private void reset () {
		this.data_len = 0 ;
	}
	public void addByte (char data) {
		this.data[this.data_len++] = (char) data;
		this.data[this.data_len] = 0x00;
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
	public char [] getData() {
		return data;
	}
	public void setData(char [] data) {
		this.data = data;
	}
	public int getData_len() {
		return data_len;
	}
	public void setData_len(int data_len) {
		this.data_len = data_len;
	}
	public String getDataDateStr() {
		return dataDateStr;
	}
	public void setDataDateStr(String dataDateStr) {
		this.dataDateStr = dataDateStr;
	}
}
