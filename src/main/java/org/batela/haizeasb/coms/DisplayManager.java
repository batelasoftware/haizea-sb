package org.batela.haizeasb.coms;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Queue;

import org.batela.haizeasb.HaizeaSbApplication;
import org.batela.haizeasb.coms.VaisalaManager.STATUS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jssc.*;

public class DisplayManager  extends Thread {

	private SerialPort serialPort;
	private String port ;
	private Integer parity;
	private Integer baudrate;
	private Integer databits;
	private Integer stopbits;
	private ArrayList <DisplayData> bufferData ;
	private Queue<DisplayData> q;
	
	private static final Logger logger = LoggerFactory.getLogger(DisplayManager.class);
	public enum STATUS{COM_INIT, WRITE_DATA, ERROR}
	private STATUS status = STATUS.COM_INIT;
	
	public DisplayManager (String port,Integer baudrate, Integer databits, Integer stopbits, Integer parity,Queue<DisplayData> q) {
		this.port = port;
		this.parity = parity;
		this.baudrate = baudrate;
		this.databits = databits;
		this.stopbits = stopbits;
		this.bufferData = new ArrayList <DisplayData> ();
		this.q = q;
		logger.info("Puerto configurado: " + this.getPort());
	
	}

	public DisplayManager (ArrayList <SerialConfig> sc,Queue<DisplayData> q ) {
		
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
	
	public boolean Open () throws SerialPortException {
		boolean res = true;
		serialPort = new SerialPort(this.getPort());
	    try {
	        serialPort.openPort();//Open serial port
	        serialPort.setParams(this.getBaudRate(), 
	        		this.getDataBits(),
                    this.getStopBits(),
                    this.getParity());//Set
	        int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
	        serialPort.setEventsMask(mask);//Set mask
	        logger.info("EventListener configurado: " + this.getPort());
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
			this.logger.info("El puerto ha sido purgado");	
		}
	}
	@Override
	public void run() {
		boolean ready = false;
		int contador_errores = 0 ;
		DecimalFormat df = new DecimalFormat("0.00");
		VaisalaData vaisalaData = new VaisalaData();
		while (true) {
			switch (this.status) {
				case COM_INIT:
					try {
						ready = this.Open();
						if ( ready ) {
							this.purgePort();
							this.status = STATUS.WRITE_DATA;
							logger.info("Puerto serie abierto: " + this.serialPort.getPortName());
						}
						else
							this.status = STATUS.ERROR;
					} catch (SerialPortException e) {
						this.status = STATUS.ERROR;
					}
				break;
				case WRITE_DATA:
					try {
						DisplayData data = this.q.poll();
						
						logger.info("Extraemos data de cola para remotos");
						if (data != null) {
							logger.debug ("Enviado al display: " + data.getMessage());
							this.serialPort.writeString(data.getMessage());
						}
						else {
							Thread.sleep(500);
							logger.info("Display Manager Doing sleep");
						}
					} catch (InterruptedException | SerialPortException e) {
						this.bufferData.clear();
						logger.error("Error en bucle de RemoteManager: " + e.getMessage());
					} 
				break;
				case ERROR:
					this.bufferData.clear();
						logger.error("Se ha producido un Error!!");
						this.closePort();
						contador_errores = 0;	
				break;
			}
		}		
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
}