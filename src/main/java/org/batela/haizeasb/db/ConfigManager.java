package org.batela.haizeasb.db;

import java.sql.SQLException;
import java.util.ArrayList;

import org.batela.haizeasb.coms.DeviceConfig;
import org.batela.haizeasb.coms.SerialConfig;

public final class ConfigManager {

    private static ConfigManager INSTANCE;
    private ArrayList<DeviceConfig> devicesConf = null;
    private ArrayList<SerialConfig> serialConf = null;
    private Integer haizea_id = -1;
    private String name ="";
    private ConfigManager() {        
    }
    
    public static ConfigManager getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new ConfigManager();
        }
        
        return INSTANCE;
    }
    /***
     * 
     * @return
     */
    
    public Integer getLocalHaizeaId () {
    	if (this.devicesConf == null) {
    		this.getVaisalaDevices();
    	}
    	if (this.haizea_id == -1) {
    		this.setLocalConfiguration();
    	}
    	return this.haizea_id;
    	
    }
    
    public Integer getLocalName () {
    	if (this.devicesConf == null) {
    		this.getVaisalaDevices();
    	}
    	if (this.haizea_id == -1) {
    		this.setLocalConfiguration();
    	}
    	return this.haizea_id;
    	
    }
    
    private void setLocalConfiguration () {
    	for (DeviceConfig item : this.devicesConf) { 		      
			if (item.getRemote() == 0) {
				this.haizea_id = item.getHaizea_id();
				this.name = item.getName();
			}
	     }	
    }
    
    public ArrayList <DeviceConfig>  getVaisalaDevices () {
    	
    	try {
	    	if (this.devicesConf == null) {
	    		SQLiteHandle db = new SQLiteHandle ();    	
	    		
				this.devicesConf = db.readDeviceConfig();
		    	if (this.devicesConf.size() == 0) {
		    		this.devicesConf = null;
		    	}
	    	}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return this.devicesConf ;   	
    }
    /***
     * 
     * @return
     */
    public ArrayList <SerialConfig>  getSerialDevices () {
    	
		try {
			if (this.serialConf == null) {
				SQLiteHandle db = new SQLiteHandle ();
		    	
				this.serialConf = db.readSerialConfig();
		    	if (this.serialConf.size() == 0) {
		    		this.serialConf = null;
		    	}
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return this.serialConf ;   	
    }
    
    // getters and setters
}
