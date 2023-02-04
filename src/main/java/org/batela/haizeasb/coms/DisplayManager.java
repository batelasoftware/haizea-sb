package org.batela.haizeasb.coms;

import java.util.ArrayList;
import java.util.Queue;

import org.batela.haizeasb.HaizeaSbApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jssc.*;

public class DisplayManager  extends Thread {

	private static SerialPort serialPort;
	private String port ;
	private Integer parity;
	private Integer baudrate;
	private Integer databits;
	private Integer stopbits;
	private ArrayList <DisplayData> bufferData ;
	private Queue<DisplayData> q;
	
	private static final Logger logger = LoggerFactory.getLogger(DisplayManager.class);
	
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
	
	public boolean Open () {
		SerialPort serialPort = new SerialPort(this.getPort());
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
	        serialPort.addEventListener(new SerialPortReader());//Add SerialPortEventListener
	        logger.info("EventListener configurado: " + this.getPort());
	    }
	    catch (SerialPortException ex) {
	    	logger.error(ex.getMessage());
//	    	System.out.println(ex);
	    }
		return false;
	}

	@Override
	public void run() {
		
		this.Open();
		while (true) {
			try {
				Thread.sleep(1000);
				logger.info("Doing sleep");
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public static void getData() {
		
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
	
	static class SerialPortReader implements SerialPortEventListener {

	    public void serialEvent(SerialPortEvent event) {
	        if(event.isRXCHAR()){//If data is available
	            if(event.getEventValue() == 10){//Check bytes count in the input buffer
	                //Read data, if 10 bytes available 
	                try {
	                    byte buffer[] = serialPort.readBytes(10);
	                }
	                catch (SerialPortException ex) {
	                    System.out.println(ex);
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
