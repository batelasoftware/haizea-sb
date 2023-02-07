package org.batela.haizeasb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


import jssc.SerialPortException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.batela.haizeasb.coms.DisplayData;
import org.batela.haizeasb.coms.DisplayManager;
import org.batela.haizeasb.coms.RemoteManager;
import org.batela.haizeasb.coms.VaisalaManager;
import org.batela.haizeasb.coms.DeviceConfig;
import org.batela.haizeasb.coms.SerialConfig;
import org.batela.haizeasb.coms.VaisalaData;
import org.batela.haizeasb.db.ConfigManager;

//@SpringBootApplication(exclude =  {DataSourceAutoConfiguration.class })
@SpringBootApplication
public class HaizeaSbApplication {
//	private static final Logger logger = LogManager.getLogger( HaizeaSbApplication.class);
	private static final Logger logger = LoggerFactory.getLogger(HaizeaSbApplication.class);
	private static VaisalaManager vaisala = null;
	
	private static boolean checkConfiguration () {
		
		boolean res = true ;
		
		ArrayList <SerialConfig> sc = ConfigManager.getInstance().getSerialDevices() ;
		ArrayList <DeviceConfig> dc = ConfigManager.getInstance().getVaisalaDevices() ;
		/*
		 * 1.- Verificar que hay un solo local
		 * 2.- Verificar que hay dos conexiones de puertos
		 */
		if (dc == null && sc == null) {
			res = false;
		}
		return res ;
	}

	public static VaisalaData getLastReadings () {
		return vaisala.getVaisalaData();
	}
	
	public static void main(String[] args) throws SerialPortException {
		
		
		Queue<DisplayData> serialQ= new LinkedList<>();
		Queue<VaisalaData> remoteQ= new LinkedList<>();
		
		if ( checkConfiguration () == false ) {
			logger.error("Not valid configuration found");
			System.exit(-1);
		}

		ArrayList <SerialConfig> sc = ConfigManager.getInstance().getSerialDevices() ;
		ArrayList <DeviceConfig> dc = ConfigManager.getInstance().getVaisalaDevices() ;

	    vaisala = new VaisalaManager(sc,dc,serialQ,remoteQ);
	    DisplayManager display = new DisplayManager(sc,serialQ);
	    RemoteManager remote = new RemoteManager(remoteQ);			

//	    Thread vaisala_th =new Thread(vaisala);   // Using the constructor (Runnable r)  
//		vaisala_th.start(); 
//		
//		Thread display_th =new Thread(display);   // Using the constructor (Runnable r)  
//		display_th.start();  
//		
		Thread remote_th =new Thread(remote);   // Using the constructor (Runnable r)  
		remote_th.start();  
	
		SpringApplication.run(HaizeaSbApplication.class, args);
	}

}
