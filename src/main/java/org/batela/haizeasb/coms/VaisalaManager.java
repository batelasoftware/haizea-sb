package org.batela.haizeasb.coms;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.batela.haizeasb.db.SQLiteHandle;

import java.util.Queue;
import java.util.LinkedList;

import jssc.*;

public class VaisalaManager  extends Thread {

	private static SerialPort serialPort;
	private String port ;
	private Integer parity;
	private Integer baudrate;
	private Integer databits;
	private Integer stopbits;
	private ArrayList <VaisalaData> bufferData ;
	private Queue<DisplayData> displayQ ;
	private Queue<VaisalaData> remoteQ ;
	private Integer haizea_id = -1 ;
	private boolean ready = false;
	static final Logger logger = LogManager.getLogger(VaisalaManager.class);
	
	
	public VaisalaManager (String port,Integer baudrate, Integer databits, Integer stopbits, Integer parity,Queue<DisplayData> q ) {
		this.port = port;
		this.parity = parity;
		this.baudrate = baudrate;
		this.databits = databits;
		this.stopbits = stopbits;
		this.bufferData = new ArrayList <VaisalaData> ();
		this.haizea_id = 0 ;
		this.displayQ = q;
		logger.info("Puerto configurado");
	
	}
	
	public VaisalaManager (ArrayList <SerialConfig> sc,
			ArrayList<DeviceConfig> dc, 
			Queue<DisplayData> q, 
			Queue<VaisalaData> remoteQ2 ) {
		
		for (SerialConfig item : sc) { 		      
			if (item.getName().toLowerCase().equals("vaisala")) {
				this.port = item.getPort().toString();
				this.parity = item.getParity();
				this.baudrate = item.getBauds();
				this.databits = item.getDatab();
				this.stopbits = item.getStopb();
			}
	     }
		
		for (DeviceConfig item : dc) { 		      
			if (item.getRemote() == 0) {
				this.haizea_id = item.getHaizea_id();
			}
	     }
		
		this.bufferData = new ArrayList <VaisalaData> ();
		this.displayQ = q;
		this.remoteQ = remoteQ2;
		logger.info("Puerto configurado");
	}
	
	public boolean Open () throws SerialPortException {
		SerialPort serialPort = new SerialPort(this.getPort());
		boolean res = false;
		try {
	        serialPort.openPort();//Open serial port
	        serialPort.setParams(this.getBaudRate(), 
	        		this.getDataBits(),
                    this.getStopBits(),
                    this.getParity());//Set
//	        serialPort.setParams(SerialPort.BAUDRATE_9600, 
//	                             SerialPort.DATABITS_8,
//	                             SerialPort.STOPBITS_1,
//	                             SerialPort.PARITY_NONE);//Set params. Also you can set params by this string: serialPort.setParams(9600, 8, 1, 0);
////	        serialPort.writeBytes("This is a test string".getBytes());//Write data to port
//	        serialPort.closePort();//Close serial port
//	        
	        
	        int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
	        serialPort.setEventsMask(mask);//Set mask
	        serialPort.addEventListener(new SerialPortReader(this.haizea_id));//Add SerialPortEventListener
	    
	        res = true;
		}
	    catch (SerialPortException ex) {
	    	logger.error("No se ha podido abrir el puerto serie" + ex.getMessage());
	    	this.ready = false ;
	    	serialPort.closePort();
	    } catch (SQLException ex) {
	    	serialPort.closePort();
	    	logger.error("No se ha podido connectar a la base de datos: " + ex.getMessage());
	    	
		}
		return res;
	}

	@Override
	public void run() {
		
		boolean ready = false;
		int contador = 0 ;
		try {
			while (true) {
				if (this.ready == false) {
					this.ready = this.Open();
				}
				
				Thread.sleep (1000);
//				Solo para trazas
				if (contador++ == 100) {
					logger.info("Doing sleep");
					contador = 0;
				}		
			}
		}
		catch (InterruptedException e) {
			logger.error("No se ha podido ejectuar Sleep: " + e.getMessage());
		} catch (SerialPortException e) {
			logger.error("Excepcion de puerto serie: " + e.getMessage());
		}
		
	}
	
	/****
	 *  PRIVATE SECTION
	 */
	/**
	 * 
	 * @re turn
	 */
	private String getPort() {	
		switch (this.port) {
		case "1":
		case "COM1":
			return "/dev/tty0";
		case "2":
		case "COM2":
			return "/dev/tty1";
		default:
			return "/dev/tty0"; 
		}
	}
	
	private int getBaudRate() {	
		switch (this.baudrate) {
		case 9600:
			return SerialPort.BAUDRATE_9600;
		case 19200:
			return SerialPort.BAUDRATE_19200;
		case 115200:
			return SerialPort.BAUDRATE_115200;
		default:
			return SerialPort.BAUDRATE_19200 ;
		}
	}
	
	private int getDataBits() {	
		switch (this.databits) {
		case 8:
			return SerialPort.DATABITS_8;
		case 7:
			return SerialPort.DATABITS_7;
		default:
			return SerialPort.DATABITS_8 ;
		}
	}
	
	private int getParity() {	
		switch (this.parity) {
		case 0:
			return SerialPort.PARITY_NONE;
		case 1:
			return SerialPort.PARITY_ODD;
		case 2:
			return SerialPort.PARITY_EVEN;
		default:
			return SerialPort.PARITY_NONE ;
		}
	}
	
	private int getStopBits() {	
		switch (this.stopbits) {
		case 1:
			return SerialPort.STOPBITS_1;
		case 2:
			return SerialPort.STOPBITS_2;
		default:
			return SerialPort.STOPBITS_1 ;
		}
	}
	
	static class SerialPortReader implements SerialPortEventListener {

		
		private SQLiteHandle db = new SQLiteHandle ();
    	private Connection conn = db.connect();
    	private Integer haizea_id;
    	
		public SerialPortReader ( Integer haizea_id) throws SQLException {
		
			this.db = new SQLiteHandle ();
	    	this.conn = db.connect(); 
	    	this.haizea_id = haizea_id;
	    	
		}
		
	    public void serialEvent(SerialPortEvent event) {
	        if(event.isRXCHAR()){//If data is available
	            if(event.getEventValue() == 10){//Check bytes count in the input buffer
	                //Read data, if 10 bytes available 
	                try {
	                    byte buffer[] = serialPort.readBytes(10);
	                	String dt ="";
	    				Float ws = null;
	    				Float wd= null;
	    				this.db.insertWindData(conn,this.haizea_id, ws, wd, "");
	                    
	                }
	                catch (SerialPortException ex) {
	                    System.out.println(ex);
	                } catch (SQLException e) {
	                	System.out.println(e);
					}
	            }
	        }
	        else if(event.isCTS()){//If CTS line has changed state
	            if(event.getEventValue() == 1){//If line is ON
	                System.out.println("CTS - ON");
	            }
	            else {
	                System.out.println("CTS - OFF");
	            }
	        }
	        else if(event.isDSR()){///If DSR line has changed state
	            if(event.getEventValue() == 1){//If line is ON
	                System.out.println("DSR - ON");
	            }
	            else {
	                System.out.println("DSR - OFF");
	            }
	        }
	    }
	}
	
}
