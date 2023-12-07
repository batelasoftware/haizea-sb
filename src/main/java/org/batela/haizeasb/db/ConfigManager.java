package org.batela.haizeasb.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import org.batela.haizeasb.coms.DeviceConfig;
import org.batela.haizeasb.coms.SerialConfig;

public final class ConfigManager {

    private static ConfigManager INSTANCE;
    private ArrayList<DeviceConfig> devicesConf = null;
    private ArrayList<SerialConfig> serialConf = null;
    private  Properties properties = null; 
    private Integer haizea_id = -1;
    private String name ="";
    private String ip ="";
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
    
    public String getLocalName () {
    	if (this.devicesConf == null) {
    		this.getVaisalaDevices();
    	}
    	if (this.name.compareTo("")==0) {
    		this.setLocalConfiguration();
    	}
    	return this.name;
    	
    }
    

    public String getLocalIP () {
    	if (this.devicesConf == null) {
    		this.getVaisalaDevices();
    	}
    	if (this.name.compareTo("")==0) {
    		this.setLocalConfiguration();
    	}
    	return this.ip;
    	
    }
    
    private void setLocalConfiguration () {
    	for (DeviceConfig item : this.devicesConf) { 		      
			if (item.getRemote() == 0) {
				this.haizea_id = item.getHaizea_id();
				this.setName(item.getName());
				this.ip = item.getIp();
			}
	     }	
    }
    
    /***
     * 
     * @return
     */
    public void  reloadVaisalaDevices (){
    	this.devicesConf = null;
    	this.getVaisalaDevices();
    }
    
    /***
     * 
     * @return
     */
    public Properties  getProperties () {
    	
    	try {
	    	if (this.properties == null) {
	    		this.properties = new Properties(); 	
	    		this.properties = this.loadProperties("application.properties");
	    	}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.properties = null;
		}
    	return this.properties ;   	
    }
    /***
     * 
     * @return
     */
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
			this.devicesConf = null;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}    
	
	private  Properties loadProperties(String resourceFileName) throws IOException {
		Properties configuration = new Properties(); 
        InputStream inputStream = ConfigManager.class
          .getClassLoader()
          .getResourceAsStream(resourceFileName);
        configuration.load(inputStream);
        inputStream.close();
        return configuration;
    }
    // getters and setters
}
