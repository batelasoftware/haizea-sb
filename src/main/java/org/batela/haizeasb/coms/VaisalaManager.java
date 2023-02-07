package org.batela.haizeasb.coms;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import org.batela.haizeasb.HaizeaSbApplication;
import org.batela.haizeasb.db.SQLiteHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.LinkedList;
import java.util.List;

import jssc.*;

public class VaisalaManager  extends Thread {

	private SerialPort serialPort;
	private String port ;
	private Integer parity;
	private Integer baudrate;
	private Integer databits;
	private Integer stopbits;
	private ArrayList <VaisalaData> bufferData ;
	private Queue<DisplayData> displayQ ;
	private Queue<VaisalaData> remoteQ ;
	private Integer haizea_id = -1 ;
	private Integer storage_count = 10;
	private VaisalaData vaisalaData = new VaisalaData();
	
	private static final Logger logger = LoggerFactory.getLogger(VaisalaManager.class);
	
	private SQLiteHandle db = null; 
	private Connection conn = null;
	public enum STATUS{COM_INIT, DB_INIT, READ_DATA, ERROR}
	private STATUS status = STATUS.COM_INIT;

	public VaisalaManager (String port,Integer baudrate, Integer databits, Integer stopbits, Integer parity,Queue<DisplayData> q ) {
		this.port = port;
		this.parity = parity;
		this.baudrate = baudrate;
		this.databits = databits;
		this.stopbits = stopbits;
		this.bufferData = new ArrayList <VaisalaData> ();
		this.haizea_id = 0 ; 
		this.displayQ = q;
		this.db = new SQLiteHandle ();
		this.status = STATUS.COM_INIT;
		
		logger.info("Puerto configurado");
	
	}
	
	public VaisalaManager (ArrayList <SerialConfig> sc,
			ArrayList<DeviceConfig> dc, 
			Queue<DisplayData> q, 
			Queue<VaisalaData> remoteQ2 ) {
		this.status = STATUS.COM_INIT;
		
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
		this.db = new SQLiteHandle ();
		
		logger.info("Puerto configurado: " + this.getPort() +":"+ this.getBaudRate()+":"+this.getDataBits());
	
	}
	/***
	 * 
	 */
	private void closePort () {
		try {
			this.serialPort.closePort();
		} catch (SerialPortException e) {
			logger.error("Error cerrando puerto:" + this.getPort());	
		}
	}
	/***
	 * 
	 * @return
	 * @throws SerialPortException
	 */
	public boolean Open () throws SerialPortException {
		serialPort = new SerialPort(this.getPort());
		boolean res = false;
		try {
	        serialPort.openPort();//Open serial port
	        serialPort.setParams(this.getBaudRate(), 
	        		this.getDataBits(),
                    this.getStopBits(),
                    this.getParity());//Set
	        
	        int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
	        serialPort.setEventsMask(mask);//Set mask
//	        serialPort.addEventListener(new SerialPortReader(this.haizea_id));//Add SerialPortEventListener
	        
	        logger.info("EventListener configurado: " + this.getPort());
	        
	        res = true;
		}
	    catch (SerialPortException ex) {
	    	logger.error("No se ha podido abrir el puerto serie" + ex.getMessage());
	    	res = false ;
	    	serialPort.closePort();
	    }
		return res;
	}

	private void purgePort () {
		try {
			while (true) {
				serialPort.readBytes(1,100);
			}
		} 
		catch (SerialPortException | SerialPortTimeoutException e) {
			logger.info("El puerto ha sido purgado");	
		}
	}
	
	private int readPortData (VaisalaData vaisala_data) {
		
		int data_len = 0;
    	String result = "";
		try {
			while (true) {
				if (vaisala_data.getData_len() >=250) {
					vaisala_data.setData_len(0);
				}	
				byte data = serialPort.readBytes(1,100)[0];
				vaisala_data.addByte((char)data);	
			}
		} 
		catch (SerialPortException | SerialPortTimeoutException e) {
			this.logger.debug("Serial Port : data_len: " + Integer.toString(data_len));
				
		} 
		return vaisala_data.getData_len();
	}
	/**
	 * 
	 * @param contador_errores
	 * @param ready
	 * @return
	 * @throws SQLException 
	 */
	private boolean resetConnections (int contador_errores,boolean ready) throws SQLException {
		boolean is_ready = ready;
		if (contador_errores >= 10) {
			this.closePort();
			this.conn.close();
			is_ready = false;
		}
		return is_ready;
	}
	/***
	 * 
	 * @param vd
	 * @return
	 */
	private boolean insertIntoRemoteQ (VaisalaData vd) {
		boolean res = false;
		try {
			VaisalaData vdr = new VaisalaData (vd.getDataDateStr(),vd.getWinddirec(),vd.getWindspeed());
			this.remoteQ.add(vdr);
			logger.debug("Insertado en cola Remota:" + vdr.toString());
			res = true;
		}
		catch (Exception e) {
			logger.error("Error al insertar en la cola para remotos");
		}
		return res;
	}
	/***
	 * 
	 * @return
	 * @throws SQLException
	 */
	private Integer evaluateReadData () throws SQLException {
   
		Float speed_avg = (float) 0;
		Float direc_avg	= (float) 0;
		String ddate_avg = "";
		Integer new_range = 10;
		for (VaisalaData vd : this.bufferData) { 	
		   speed_avg += vd.getWindspeed(); 	
		   direc_avg += vd.getWinddirec(); 	
		   ddate_avg = vd.getDataDateStr();   
		}
		
		VaisalaData vdi = new VaisalaData(ddate_avg, speed_avg/this.bufferData.size(), direc_avg/this.bufferData.size());
		this.db.insertWindData(conn,this.haizea_id, 
					vdi.getWindspeed(),
					vdi.getWinddirec(), 
					vdi.getDataDateStr());
		logger.debug("Insertando en base de datos: " + vdi.toString());	
		this.insertIntoRemoteQ(vdi);
		
		if (vdi.getWindspeed()<=10) new_range = 60*5; //5 minutos
		else if (vdi.getWindspeed()>10 && vdi.getWindspeed()<=25) new_range = (int) (60*2.5); //2.5 minutos
		else if (vdi.getWindspeed()>25 && vdi.getWindspeed()<=50) new_range = (int) (60*1.5); //1.5 minutos
		else if (vdi.getWindspeed()>50 && vdi.getWindspeed()<=70) new_range = 60;
		else if (vdi.getWindspeed()>70) new_range = 30;
		logger.info("Nuevo rango de almacenamiento calculado: " + new_range.toString());
		
//		new_range = 1;
		this.bufferData.clear();
		return new_range;
				
	}
	/***
	 * 
	 */
	@Override
	public void run() {
		boolean ready = false;
		int contador_errores = 0 ;
		DecimalFormat df = new DecimalFormat("0.00");
		
		while (true) {
			switch (this.status) {
				case COM_INIT:
					try {
						ready = this.Open();
						if ( ready ) {
							this.purgePort();
							this.status = STATUS.DB_INIT;
							logger.info("Puerto serie abierto: " + this.serialPort.getPortName());
						}
						else
							this.status = STATUS.ERROR;
					} catch (SerialPortException e) {
						this.status = STATUS.ERROR;
					}
					
				break;
				case DB_INIT:
					try {
						this.conn = this.db.connect();
						this.status = STATUS.READ_DATA;
						logger.info("Base de datos abierta");
					} catch (SQLException e) {
						this.status = STATUS.ERROR;
					}
				break;
				case READ_DATA:
					try {
						int longi = this.readPortData(vaisalaData);
						if (longi > 1) {
							logger.info("Mensaje de Vaisala: " + new String(vaisalaData.getData(),0,longi) + " longitud: " + longi);
							if (vaisalaData.parse() == true) {
								this.displayQ.add(new DisplayData (vaisalaData.toString()));
								
								if (this.bufferData.size()<this.storage_count) {
									this.bufferData.add(vaisalaData);
									logger.debug("Tamaño de la cola para almacenamiento:" + String.valueOf (this.bufferData.size()));
								}
								else {
									this.bufferData.add(vaisalaData);
									this.storage_count = this.evaluateReadData();
								}
								contador_errores = 0;
							}
							else {
								if (contador_errores++ >= 10)
									this.status = STATUS.ERROR;
									logger.warn ("Timeout, no se reciben datos.") ;
							}
						}
					} catch (SQLException e) {
						logger.warn("Se ha producido un error en READ_DATA" + e.getMessage());
						this.status = STATUS.ERROR;
					}
				break;
				case ERROR:
					try {
						this.bufferData.clear();
						logger.error("Se ha producido un Error!!");
						this.closePort();
						this.conn.close();
						contador_errores = 0;
					} catch (SQLException e) {
						this.status = STATUS.ERROR;
					}		
				break;
			}
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
			return "/dev/ttyS0";
		case "2":
		case "COM2":
			return "/dev/ttyS1";
		default:
			return "/dev/ttyS0"; 
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
	
	public VaisalaData getVaisalaData() {
		return vaisalaData;
	}

	public void setVaisalaData(VaisalaData vaisalaData) {
		this.vaisalaData = vaisalaData;
	}

}	
//	static class SerialPortReader implements SerialPortEventListener {
//
//		
//		private SQLiteHandle db = new SQLiteHandle ();
//    	private Connection conn = db.connect();
//    	private Integer haizea_id;
//    	static final Logger logger = LogManager.getLogger(SerialPortReader.class);
//    	private byte [] data = new byte [250];
//    	private int data_len = 0;
//    	
//		public SerialPortReader ( Integer haizea_id) throws SQLException {
//		
//			this.db = new SQLiteHandle ();
//	    	this.conn = db.connect(); 
//	    	this.haizea_id = haizea_id;
//	    	this.logger.info("Serial Port Reader configurado");
//		}
//		
//		public void serialEvent(SerialPortEvent event) {
//			
//			try {
////				this.logger.info("Recibido byte, longitud:" + this.data_len);
//				this.data[data_len++] = serialPort.readBytes(1,100)[0];
////				this.logger.info("Recibido: " + (char)this.data[data_len-1]);
//				if (this.data_len >=250) {
//					this.data_len =0;
//					this.data[data_len] = 0;
//				}
//	
//			} 
//			catch (SerialPortException | SerialPortTimeoutException e) {
//				this.logger.error("Serial Port Exception*******************");
//				
//			}
//		
//	    }
		
//		public void serialEvent(SerialPortEvent event) {
//	        if(event.isRXCHAR()){//If data is available
//	            if(event.getEventValue() == 10){//Check bytes count in the input buffer
//	                //Read data, if 10 bytes available 
//	                try {
//	                    byte buffer[] = serialPort.readBytes(10);
//	                	String dt ="";
//	    				Float ws = null;
//	    				Float wd= null;
//	    				this.db.insertWindData(conn,this.haizea_id, ws, wd, "");
//	                    
//	                }
//	                catch (SerialPortException ex) {
//	                    System.out.println(ex);
//	                } catch (SQLException e) {
//	                	System.out.println(e);
//					}
//	            }
//	        }
//	        else if(event.isCTS()){//If CTS line has changed state
//	            if(event.getEventValue() == 1){//If line is ON
//	                System.out.println("CTS - ON");
//	            }
//	            else {
//	                System.out.println("CTS - OFF");
//	            }
//	        }
//	        else if(event.isDSR()){///If DSR line has changed state
//	            if(event.getEventValue() == 1){//If line is ON
//	                System.out.println("DSR - ON");
//	            }
//	            else {
//	                System.out.println("DSR - OFF");
//	            }
//	        }
//	    }
//	}
	
//}
