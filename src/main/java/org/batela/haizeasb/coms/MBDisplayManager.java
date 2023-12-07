package org.batela.haizeasb.coms;


import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Queue;

import org.batela.haizeasb.HaizeaSbApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intelligt.modbus.jlibmodbus.Modbus;
import com.intelligt.modbus.jlibmodbus.master.ModbusMaster;
import com.intelligt.modbus.jlibmodbus.master.ModbusMasterFactory;
import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException;
import com.intelligt.modbus.jlibmodbus.serial.SerialParameters;
import com.intelligt.modbus.jlibmodbus.serial.SerialPort;
import com.intelligt.modbus.jlibmodbus.serial.SerialPort.Parity;
import com.intelligt.modbus.jlibmodbus.serial.*;

import jssc.SerialPortList;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;


public class MBDisplayManager  extends Thread {

	private SerialPort serialPort;
	private String port ;
	private Integer parity;
	private Integer baudrate;
	private Integer databits;
	private Integer stopbits;
	private ArrayList <DisplayData> bufferData ;
	private Queue<DisplayData> q;

	private   SerialParameters sp = new SerialParameters();
	private ModbusMaster master = null;
	private static final Logger logger = LoggerFactory.getLogger(DisplayManager.class);
	public enum STATUS{COM_INIT, WRITE_DATA, ERROR}
	private STATUS status = STATUS.COM_INIT;
	private LocalDateTime lastRemoteR = LocalDateTime.now();
	
	public MBDisplayManager (String port,Queue<DisplayData> q) {
		this.port = port;
	
		this.bufferData = new ArrayList <DisplayData> ();
		this.q = q;
		logger.info("Puerto configurado: " + this.getPort());
	
	}

	public MBDisplayManager(ArrayList <SerialConfig> sc,Queue<DisplayData> q ) {
		
		for (SerialConfig item : sc) { 		      
			if (item.getName().toLowerCase().equals("display")) {
				this.port = item.getPort().toString();
				this.parity = item.getParity();
				this.baudrate = item.getBauds();
				this.databits = item.getDatab();
				this.stopbits = item.getStopb();
			}
	     }
		this.bufferData = new ArrayList <DisplayData> ();
		this.q = q;
		
		logger.info("Puerto configurado");
	
	}
	
	public boolean Open () throws Exception {
		boolean res = true;
		try {
			this.sp = new SerialParameters();
			Thread.sleep(2000);
			this.sp.setDevice(this.getPort());
			this.sp.setBaudRate(SerialPort.BaudRate.BAUD_RATE_19200);
			this.sp.setDataBits(8);
			this.sp.setParity(SerialPort.Parity.NONE);
			this.sp.setStopBits(1);
            // these parameters are set by default
			//this.sp.setBaudRate(this.getBaudRate());
			//this.sp.setDataBits(this.databits);
			//this.sp.setParity(SerialPort.Parity.NONE);
			//this.sp.setStopBits(this.stopbits);
            
			logger.info("Creando Modbus master en: " + this.getPort() + " : "+ this.sp.getDevice());
			//SerialUtils.setSerialPortFactory(new SerialPortFactoryLoopback(false));
			
			SerialUtils.setSerialPortFactory(new SerialPortFactoryJSSC());
            this.master = ModbusMasterFactory.createModbusMasterRTU(this.sp);
            //this.master.setResponseTimeout(1000);
            this.master.connect();

            logger.info("EventListener configurado: " + this.getPort());
	    }
	    catch (Exception ex) {
	    	ex.printStackTrace();
	    	logger.error("No se ha podido abrir el puerto serie:" + ex.getMessage());
	    	res = false ;
	    }
		return res;
	}

//	private void purgePort () {
//		try {
//			while (true) {
//				serialPort.readBytes(1,100);
//			}
//		} 
//		catch (SerialPortException | SerialPortTimeoutException e) {
//			this.logger.info("El puerto ha sido purgado");	
//		}
//	}
	
	@Override
	public void run() {
		boolean ready = false;
		int contador_errores = 0 ;
		DecimalFormat df = new DecimalFormat("0.00");
		VaisalaData vaisalaData = new VaisalaData();
		int[] ipItems = new int[4];
		while (true) {
			switch (this.status) {
				case COM_INIT:
					try {
						ready = this.Open();
						if ( ready ) {
//							this.purgePort();
							this.status = STATUS.WRITE_DATA;
							logger.info("Puerto modbus abierto: " + this.getPort());
							
							//Enviando IP local y hostname
							if (this.getLocalIpAddress (ipItems) == -1) {
								this.master.writeSingleRegister(1,3500,0);
							}
							else {
								this.master.writeSingleRegister(1,3500,this.getLocalIpAddress (ipItems));
							}
							
							logger.info("Haizea IP:> " + ipItems[0] +"."+ipItems[1]+"."+ipItems[2]+"."+ipItems[3]);
							for (int i = 0 ;i<4;i++)
								this.master.writeSingleRegister(1,3501+i,ipItems[i]);

						}
						else
							this.status = STATUS.ERROR;
					} catch (Exception e) {
						logger.error("No es posible escribir la IP");
						e.printStackTrace();
						this.status = STATUS.ERROR;
					}
				break;
				case WRITE_DATA:
					try {
						DisplayData data = this.q.poll();
						
						int comparisonResult = this.lastRemoteR.compareTo(HaizeaSbApplication.getLastRemoteRequest());
						if (comparisonResult < 0 ) {
							this.lastRemoteR = HaizeaSbApplication.getLastRemoteRequest();
							this.master.writeSingleRegister(1,3512,1);
						}
						else {
							this.master.writeSingleRegister(1,3512,0);
						} 
						
						logger.info("Usable Space: " + (int)this.getDiskSpace());  
						this.master.writeSingleRegister(1,3511,(int)this.getDiskSpace());
						
						logger.info("Extraemos data de cola para Display");
						if (data != null) {
							if (data.parseMessage ()) {
								LocalDateTime now = LocalDateTime.now();  
								//Enviando Fecha de recepción	
								this.master.writeSingleRegister(1,3505, now.getYear());
								this.master.writeSingleRegister(1,3506, now.getMonthValue());
								this.master.writeSingleRegister(1,3507, now.getDayOfMonth());
								
								this.master.writeSingleRegister(1,3508, now.getHour());
								this.master.writeSingleRegister(1,3509, now.getMinute());
								this.master.writeSingleRegister(1,3510, now.getSecond());
								//Enviando Valores de viento
								logger.info ("Enviado al display: Velocidad: " +  data.getWindSpeed() + " Dirección: " +data.getWindDirec());
								this.master.writeSingleRegister(1,3514,data.getWindSpeed());
								this.master.writeSingleRegister(1,3515,data.getWindDirec());
								// Status veleta ok
								this.master.writeSingleRegister(1,3513,1);
								contador_errores = 0;
							} 
							else {
								logger.error("Error parsing display data: " + data.getMessage());
							}
						}
						else {
							if (++contador_errores>=20) {
								this.master.writeSingleRegister(1,3513,0);
								contador_errores = 0;
							}
							Thread.sleep(500);
							logger.info("Display Manager Doing sleep");
						}
					} catch (Exception e) {
						this.bufferData.clear();
						
						logger.error("Error en bucle de RemoteManager: " + e.getMessage());
						this.status = STATUS.ERROR;
					} 
				break;
				case ERROR:
					this.bufferData.clear();
					logger.error("Se ha producido un Error!!");
					this.closePort();
					contador_errores = 0;	
					this.status = STATUS.COM_INIT;
				break;
			}
		}		
	}
	
	
	private int getDiskSpace() 
	{
		int res = -1;
		NumberFormat nf = NumberFormat.getNumberInstance();
		for (Path root : FileSystems.getDefault().getRootDirectories()) {

		    System.out.print(root + ": ");
		    try {
		        FileStore store = Files.getFileStore(root);
		        float a = store.getUsableSpace();
		        float b = store.getTotalSpace();
		        res = (int) ((a/b) * 100);
		    } catch (IOException e) {
		    	logger.error("Error getting disk space: " + e.getMessage());
		    }
		}
		return res;
	}

	private int getLocalIpAddress(int[] ipItems)  {
		NetworkInterface ni;
		try {
			ni = NetworkInterface.getByName("enp1s0");
		
			Enumeration<InetAddress> inetAddresses =  ni.getInetAddresses();
	        while(inetAddresses.hasMoreElements()) {
	            InetAddress ia = inetAddresses.nextElement();
	            if(!ia.isLinkLocalAddress()) {
	            	String[] vdata_split = ia.getHostAddress().split("\\.");
	            	ipItems[0]= new Integer(vdata_split[0]);
	            	ipItems[1]= new Integer(vdata_split[1]);
	            	ipItems[2]= new Integer(vdata_split[2]);
	            	ipItems[3]= new Integer(vdata_split[3]);
	            	String hn = ia.getLocalHost().getHostName();
	            	vdata_split = hn.split("-");
	            	System.out.print("Hostname is: ->>" + vdata_split[1]);
	            	return (new Integer(vdata_split[1]));
	            	
	            }
	        }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error reading IP and Hostname");
			ipItems[0]= ipItems[1]= ipItems[2]= ipItems[3]= 0;
		}
		return -1;
	}

	/***,
	 * 
	 */
	private void closePort () {
		try {
			this.master.disconnect();
		} catch (Exception e) {
			logger.error("Error cerrando puerto:" + this.getPort());	
		}
	}
	
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
	
	private SerialPort.BaudRate  getBaudRate() {	
		switch (this.baudrate) {
		case 9600:
			return SerialPort.BaudRate.BAUD_RATE_9600;
		case 19200:
			return SerialPort.BaudRate.BAUD_RATE_19200;
		case 115200:
			return SerialPort.BaudRate.BAUD_RATE_115200;
		default:
			return SerialPort.BaudRate.BAUD_RATE_19200 ;
		}
	}
	
	
	private Parity getParity() {	
		switch (this.parity) {
		case 0:
			return SerialPort.Parity.NONE;
		case 1:
			return SerialPort.Parity.ODD;
		case 2:
			return SerialPort.Parity.EVEN;
		default:
			return SerialPort.Parity.NONE ;
		}
	}
		
	
}