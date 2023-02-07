package org.batela.haizeasb.http;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.batela.haizeasb.HaizeaSbApplication;
import org.batela.haizeasb.coms.DeviceConfig;
import org.batela.haizeasb.coms.RemoteManager;
import org.batela.haizeasb.coms.VaisalaData;
import org.batela.haizeasb.coms.VaisalaManager.STATUS;
import org.batela.haizeasb.db.ConfigManager;
import org.batela.haizeasb.db.SQLiteHandle;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WindcapDataService {
	private static final Logger logger = LoggerFactory.getLogger(RemoteManager.class);
	private SQLiteHandle db = new SQLiteHandle (); 
	private Connection conn = null;
	

	public List <WindcapData> getBootRequest (){
		ArrayList <WindcapData> wdl = new ArrayList <WindcapData>();
		try {
			ArrayList <WindcapValues> wcvl = new ArrayList <WindcapValues>();
			WindcapData wcd = new WindcapData();
			wcd.setHaizea_id(ConfigManager.getInstance().getLocalHaizeaId());
			wcd.setIp(ConfigManager.getInstance().getLocalIP());;
			wcd.setName(ConfigManager.getInstance().getLocalName());
//			VaisalaData vd = HaizeaSbApplication.getLastReadings();
			VaisalaData vd = new VaisalaData();
			WindcapValues vcv = new WindcapValues (vd.getDataDateStr(), vd.getWindspeed(), vd.getWinddirec());
			
			wcvl.add(vcv);
			wcd.setValues(wcvl);
			
			wdl.add(wcd);
		}
		catch (Exception e) {
			wdl = new ArrayList <WindcapData>(); 
		}
		return  wdl;
	}
	/***
	 * 
	 * @param br
	 * @return
	 */
	public Object addRemoteData(WindcapData br) {
		logger.debug("Remote values for: " + br.getName());
		List<WindcapValues> wcvl = br.getValues();
		logger.info("Recibidos datos para remoto: " + br.getName());
		for (WindcapValues wcv : wcvl) {
			this.storeRemoteData(br.getName(), br.getIp(), wcv.getWindspeed(), wcv.getWinddirec(), wcv.getDate());
		}
		return null;
	}
	/***
	 * 
	 * @param haizea_name
	 * @param ip
	 * @return
	 */
	private Integer storeRemoteDevice (String haizea_name, String ip) {
		
		Integer new_haizea_id = -1;
		try {
			new_haizea_id = this.db.insertRemoteDevice(haizea_name, ip);
			if (new_haizea_id != 1)
				logger.info("Equipo remoto nuevo insertado:" + haizea_name + 
						" id: " + new_haizea_id.toString() + 
						" ip: " + ip);
			else
				logger.error("Error insertando equipo remoto :" + haizea_name + 
						" id: " + new_haizea_id.toString() + 
						" ip: " + ip);
				
		} catch (SQLException e) {
			logger.error ("Error insertando dispositivo remoto:" + e.getMessage());
		}
		return new_haizea_id;
	}
	/***
	 * 
	 * @param haizea_name
	 * @param ip
	 */
	public void storeRemoteData (String haizea_name,String ip,Float ws, Float wd, String ddate) {
		ArrayList <DeviceConfig> dcl  =ConfigManager.getInstance().getVaisalaDevices();
		Integer haizea_id = -1;
		try {
			for (DeviceConfig dc : dcl) {
				if (dc.getName().compareTo(haizea_name) ==0) {
					haizea_id = dc.getHaizea_id();
					break;
				}
			}
			if (haizea_id == -1) {
				haizea_id = this.storeRemoteDevice(haizea_name, ip);
				ConfigManager.getInstance().reloadVaisalaDevices();
				logger.warn("Equipo remote no registrado..procesando");
			}
			else {
				this.db.insertWindData(haizea_id,ws, wd, ddate);
				logger.info("Almacenados datos remotos, haizea_name: " + haizea_name +
						"haizea_id" + haizea_id +
						" WindSpeed: " +ws +
						" WindDirec: " +wd + " Fecha: " + ddate);
				
			}	
		}
		catch (SQLException e) {
			logger.error ("Error insertando datos origen remoto:" + e.getMessage());
		}
				
		
	}
	
	
}
