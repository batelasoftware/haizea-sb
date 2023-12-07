package org.batela.haizeasb.db;
import java.sql.Connection;  
import java.sql.DriverManager;  
import java.sql.PreparedStatement;  
import java.sql.SQLException;

import java.sql.ResultSet;    
import java.sql.Statement;
import java.util.ArrayList;



import org.batela.haizeasb.coms.DeviceConfig;
import org.batela.haizeasb.coms.SerialConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLiteHandle {
	
	private static final Logger logger = LoggerFactory.getLogger(SQLiteHandle.class);
	
	
	public Connection connect() throws SQLException {  
	
		
        //String url = "jdbc:sqlite:/home/batela/Prj/Batela/haizea-sb-deploy/dbs/haizea.db";  
        //String url = "jdbc:sqlite:/home/batela/Haizea/Db/haizea.db";
        String url = "jdbc:sqlite:/home/batela/batela-shared/haizea.db";
        Connection conn = null;  
        conn = DriverManager.getConnection(url);  
        this.logger.info("Conexion con base de datos abierta: " + url);
        return conn;  
    }
	/**
	 * 
	 * @param haizea_id
	 * @param windspeed
	 * @param winddir
	 * @param date
	 * @throws SQLException
	 */
	public int insertRemoteDevice(String haizea_name, String ip ) throws SQLException {  
        String sql = "SELECT  max(haizea_id) as haizea_id FROM tdevices";
        Connection conn = this.connect();
        Integer max_haizea_id = -1;
        try{ 
        	if (conn == null) return -1;
        	
        	Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery(sql);
            while (rs.next()) {
            	max_haizea_id = rs.getInt("haizea_id");
            }
            rs.close();
            stmt.close();
            sql = "INSERT INTO tdevices (haizea_id,name,ip,remote,send_remote) VALUES (?,?,?,?,?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);  
            pstmt.setInt	(1, max_haizea_id+1);  
            pstmt.setString	(2, haizea_name);
            pstmt.setString	(3, ip);
            pstmt.setInt	(4, 1);
            pstmt.setInt	(5, 0);
            
            pstmt.executeUpdate();  
            pstmt.close();
            max_haizea_id++;
            
        } catch (SQLException e) {  
        	logger.error(e.getMessage());
        	conn.close();
        }  
        return max_haizea_id;
	}
	/***
	 * 
	 * @param haizea_id
	 * @param windspeed
	 * @param winddir
	 * @param date
	 * @throws SQLException
	 */
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
            logger.info("Valores en SQLITE actualizados" );
        } catch (SQLException e) {  
        	logger.error(e.getMessage());
        	conn.close();
        }  
    }
	
	public void insertWindData(Connection conn,Integer haizea_id, Float windspeed, Float winddir, String date) throws SQLException {  
        String sql = "REPLACE INTO tvalues (id,haizea_id,windspeed,winddir,date) VALUES(?,?,?,?,?)";
        PreparedStatement pstmt = null;
        try{  
            pstmt = conn.prepareStatement(sql);  
            pstmt.setInt	(1, haizea_id);  
            pstmt.setInt	(2, haizea_id);
            pstmt.setFloat	(3, windspeed);
            pstmt.setFloat	(4, winddir);
            pstmt.setString	(5, date);
            pstmt.executeUpdate();  
            
            logger.info("Valores en SQLITE actualizados" );
            
        } catch (SQLException e) {  
        	logger.error("Error al insertar valures: " + pstmt.toString());
        	logger.error(e.getMessage());
        	
        }  
    }
	/**
	 * 
	 * @return
	 * @throws SQLException 
	 */
	public ArrayList <DeviceConfig> readDeviceConfig() throws SQLException {  
		
		String sql = "SELECT haizea_id,remote,name,ip,web_remote FROM tdevices";  
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
                vslc.setWeb_remote(rs.getInt("web_remote"));
                
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
