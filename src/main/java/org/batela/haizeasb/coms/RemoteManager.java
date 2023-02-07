package org.batela.haizeasb.coms;

import java.net.http.HttpClient;
import java.util.ArrayList;

import org.batela.haizeasb.HaizeaSbApplication;
import org.batela.haizeasb.db.ConfigManager;

import java.util.Queue;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;


public class RemoteManager  extends Thread {
	private final RestTemplate restTemplate;
	private ArrayList <VaisalaData> bufferData ;
	private Queue<VaisalaData> q ; 
	
	private static final Logger logger = LoggerFactory.getLogger(RemoteManager.class);
	
	public RemoteManager (Queue<VaisalaData> q ) {
		this.bufferData = new ArrayList <VaisalaData> ();
		this.q = q;
		this.restTemplate = new RestTemplate();
		logger.info("RESTHander  configurado");
	
	}

	private String createJSONMessage () {
		String jsonStr = "";
		try {
			logger.info("Creando mensaje JSON");
			
			JSONObject root=new JSONObject();
			JSONObject local=new JSONObject();
			local.put("haizea_id",ConfigManager.getInstance().getLocalHaizeaId());    
			local.put("name",ConfigManager.getInstance().getLocalName());    
			local.put("ip",ConfigManager.getInstance().getLocalIP());    
			
			JSONArray values = new JSONArray();  
			
			for (VaisalaData vd : this.bufferData) {
				JSONObject val=new JSONObject();
				val.put("date",vd.getDataDateStr() );
				val.put ("windspeed",vd.getWindspeed());
				val.put ("winddirec",vd.getWinddirec());
				values.put(val);
			}
			local.put("values", values);
			root.put("data", local);
			root.put("name", local);
			jsonStr = root.toString();
			logger.info("Creado mensaje JSON:" + local.toString());
		}
		catch (Exception e) {
			logger.error("Error al crear mensaje JSON:" + e.getMessage());
			jsonStr= "";
		}
		
		return jsonStr;
	}
	
	private String sendRemoteData () {
		
		ArrayList <DeviceConfig> dc = ConfigManager.getInstance().getVaisalaDevices() ;
		for (DeviceConfig item : dc) {
			if (item.getRemote() == 1) {
//				Enviamos por HTTP
				 String url = "https://jsonplaceholder.typicode.com/posts";
				 return this.restTemplate.getForObject(url, String.class);
//				HttpClient httpclient = HttpClients.createDefault();
//				HttpPost httppost = new HttpPost("http://www.a-domain.com/foo/");
			}
		}
		return null;
	}

	
	@Override
	public void run() {
		while (true) {
			try {
				VaisalaData data = this.q.poll();
				logger.info("Extraemos data de cola para remotos");
				
				if (data != null) {
					if (this.bufferData.size()<1) {
						this.bufferData.add(data);
						logger.debug("Tamaño de la cola para remotos:" + String.valueOf (this.bufferData.size()));
					}
					else {
						this.bufferData.add(data);
						String jsonStr = this.createJSONMessage();
						this.bufferData.clear();
	//					this.sendRemoteData ();
					}
				}
				else 
					Thread.sleep(3000);
			} catch (InterruptedException e) {
				this.bufferData.clear();
				logger.error("Error en bucle de RemoteManager");
				e.printStackTrace();
			}
		}
	}

	
//	@Override
//	public void run() {
//		while (true) {
//			try {
//				VaisalaData data = this.q.poll();
//				logger.info("Extraemos data de cola para remotos");
//				
//				if (data != null) {
//					this.bufferData.add(data);
//					String jsonStr = this.createJSONMessage();
//					this.bufferData.clear();
////					this.sendRemoteData ();
//				}
//				else {
//					Thread.sleep(3000);
//					logger.info("Remote Manager Doing sleep");
//				}
//			} catch (InterruptedException e) {
//				this.bufferData.clear();
//				logger.error("Error en bucle de RemoteManager");
//				e.printStackTrace();
//			}
//		}
//	}
	
} //Fin de la clase 
