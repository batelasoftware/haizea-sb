package org.batela.haizeasb.db;
import java.sql.Connection;  
import java.sql.DriverManager;  
import java.sql.PreparedStatement;  
import java.sql.SQLException;

import java.sql.ResultSet;    
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.batela.haizeasb.HaizeaSbApplication;
import org.batela.haizeasb.coms.DeviceConfig;
import org.batela.haizeasb.coms.SerialConfig;  

//REPLACE INTO tvalues (id,haizea_id,windspeed,winddir,date) VALUES(4,4,18304.0,12.0,'2015-10-17 11:30:30');
public class SQLiteHandle {

	private static final Logger logger = LogManager.getLogger( SQLiteHandle .class);
	
	
	
	public Connection connect() throws SQLException {  
        String url = "jdbc:sqlite:/home/batela/Prj/Batela/haizea-ws-deploy/dbs/haizea.db";  
        Connection conn = null;  
        conn = DriverManager.getConnection(url);  
        return conn;  
    }  

	public void insertWindData(Integer haizea_id, Float windspeed, Float winddir, String date) throws SQLException {  
        String sql = "REPLACE INTO tvalues (id,haizea_id,windspeed,winddir,date) VALUES(?,?,?,?,?)";
        Connection conn = this.connect();  
        try{  
            PreparedStatement pstmt = conn.prepareStatement(sql);  
            pstmt.setInt	(1, haizea_id);  
            pstmt.setInt	(2, haizea_id);
            pstmt.setFloat	(3, windspeed);
            pstmt.setFloat	(4, winddir);
            pstmt.setString	(5, date);
            
            pstmt.executeUpdate();  
            conn.close();
        } catch (SQLException e) {  
        	logger.error(e.getMessage());
        	conn.close();
        }  
    }
	
	
	public void insertWindData(Connection conn,Integer haizea_id, Float windspeed, Float winddir, String date) throws SQLException {  
        String sql = "REPLACE INTO tvalues (id,haizea_id,windspeed,winddir,date) VALUES(?,?,?,?,?)";
        try{  
            PreparedStatement pstmt = conn.prepareStatement(sql);  
            pstmt.setInt	(1, haizea_id);  
            pstmt.setInt	(2, haizea_id);
            pstmt.setFloat	(3, windspeed);
            pstmt.setFloat	(4, winddir);
            pstmt.setString	(5, date);
            
            pstmt.executeUpdate();  
            conn.close();
        } catch (SQLException e) {  
        	logger.error(e.getMessage());
        	
        }  
    }
	/**
	 * 
	 * @return
	 * @throws SQLException 
	 */
	public ArrayList <DeviceConfig> readDeviceConfig() throws SQLException {  
		
		String sql = "SELECT haizea_id,remote,name,ip FROM tdevices WHERE remote = 0";  
		ArrayList <DeviceConfig> vslc_list = new ArrayList <DeviceConfig>();
		Connection conn = null;
        try {
    		conn = this.connect();  

        	Statement stmt  = conn.createStatement();  
            ResultSet rs    = stmt.executeQuery(sql);  
                
            // loop through the result set
            while (rs.next()) {  
                DeviceConfig vslc = new DeviceConfig();
            	vslc.setHaizea_id(rs.getInt("haizea_id"));
                vslc.setName(rs.getString("name"));
                vslc.setIp(rs.getString("ip"));
                vslc.setRemote(rs.getInt("remote"));
                
                vslc_list.add(vslc);    
            } 
        } catch (SQLException e) {  
        	logger.error(e.getMessage());
        	
        }
        if (conn != null)
        	conn.close();
        
        return vslc_list;
    }
	
	
	public ArrayList <SerialConfig> readSerialConfig() throws SQLException {  
		
		String sql = "SELECT port,bauds,datab,stopb,parit,name FROM tserial";  
		ArrayList <SerialConfig> vslc_list = new ArrayList <SerialConfig>();
		Connection conn = this.connect();
        try {
              
            Statement stmt  = conn.createStatement();  
            ResultSet rs    = stmt.executeQuery(sql);  
                
            while (rs.next()) {  
                SerialConfig srlc = new SerialConfig();
                srlc.setPort(rs.getInt("port"));
                srlc.setBauds(rs.getInt("bauds"));
                srlc.setDatab(rs.getInt("datab"));
                srlc.setStopb(rs.getInt("stopb"));
                srlc.setParity(rs.getInt("parit"));
                srlc.setName(rs.getString("name"));
                
                vslc_list.add(srlc);
               
            } 
            conn.close();
        } catch (SQLException e) {  
        	logger.error(e.getMessage());
        	conn.close();
        }
        return vslc_list;
    }
}
