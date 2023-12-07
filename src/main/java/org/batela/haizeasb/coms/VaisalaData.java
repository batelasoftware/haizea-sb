package org.batela.haizeasb.coms;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jssc.SerialPortException;

public class VaisalaData {
		
	private static final Logger logger = LoggerFactory.getLogger(VaisalaData.class);
	
	private char [] data = new char [250];
	private int data_len  = 0;
	private LocalDateTime dataDate = null;
	
	private Float windspeed;
	private Float winddirec;

    private DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private String dataDateStr = null;
    
	public VaisalaData () {
		this.data_len = 0 ;
	}
	public VaisalaData (String dt, Float ws, Float wd ) {
//		this.dt = dt;
		this.windspeed = ws;
		this.winddirec = wd;
		this.dataDateStr = dt;
	}
	public boolean parse () {
		boolean res = false;
		try {
		
			String vdata = new String (this.data);
			String [] vdata_split = vdata.split(",");
			
			if (vdata_split[0].compareTo("0R1")!= 0) {
				logger.error("Cabecera de mensaje erroneo: " + new String(this.data,0,this.data_len));
				this.reset();
				return false;
			}
				
			if (vdata_split.length != 7) {
				logger.error("Longitud de mensaje erroneo: " + new String(this.data));
				this.reset();
				return false ;
			}
				
			String [] wdirl = vdata_split[2].split("=");
			String [] wvell = vdata_split[5].split("=");
			
			if ((wdirl[1].charAt(wdirl[1].length()-1) != 'D') || (wvell[1].charAt(wvell[1].length()-1) != 'K')) {
				logger.error("Mensaje mal formado: " + new String(this.data));
				this.reset();
				return false ;
			}
			
			String wdir = wdirl[1].substring(0, wdirl[1].length()-1);
			String wvel = wvell[1].substring(0, wvell[1].length()-1);
			
			this.winddirec = Float.valueOf(wdir);
			this.windspeed = Float.valueOf(wvel);
			
			this.dataDate = LocalDateTime.now();
			this.setDataDateStr(this.dataDate.format(myFormatObj));
			
			res = true;
		}
		catch (Exception e) {
			logger.error("Excepcion parseando Mensaje: " + new String(this.data));
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
	public String toString() {
		String str = "WindSpeed: " + Float.toString(this.windspeed) + " km/h;" +
				"WindDir: " + Float.toString(this.winddirec) + " º;" +
				"Fecha: " + this.dataDateStr +" ; " ;
		return str;
	}
}
