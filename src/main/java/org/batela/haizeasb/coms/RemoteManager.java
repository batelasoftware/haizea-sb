package org.batela.haizeasb.coms;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.batela.haizeasb.HaizeaSbApplication;
import org.batela.haizeasb.db.ConfigManager;

import java.util.Queue;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;


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
	
	private String sendRemoteData (String jsonData) 
	{
		
		ArrayList <DeviceConfig> dc = ConfigManager.getInstance().getVaisalaDevices() ;
		for (DeviceConfig item : dc) {
			if (item.getRemote() == 1) {
//				Enviamos por HTTP
				 String url = "http://"+item.getIp()+":19099/api/v1/windcap";
				 logger.info("Enviando mensaje Remoto: " + url);
				 HttpClient httpclient = HttpClients.createDefault();
				 HttpPost httppost = new HttpPost(url);
				 
				 try {
					 httppost.setHeader("Content-Type", "application/json");
					 httppost.setEntity(new StringEntity(jsonData, "UTF-8"));
		            // Execute the POST request
					 HttpResponse response = httpclient.execute(httppost);

		            // Get the response code
		            int statusCode = response.getStatusLine().getStatusCode();
		            System.out.println("Response Code: " + statusCode);

		            // Read the response content
		            try (BufferedReader reader = new BufferedReader(
		                    new InputStreamReader(response.getEntity().getContent()))) {
		                String line;
		                StringBuilder responseContent = new StringBuilder();
		                while ((line = reader.readLine()) != null) {
		                    responseContent.append(line);
		                }
		                System.out.println("Response: " + responseContent.toString());
		            }
		        } catch (Exception e) {
		        	logger.error("Error sending vaisala data post message");
		            e.printStackTrace();
		        }
			}
		}
		return null;
	}

	
	@Override
	public void run() {
		while (true) {
			try {
				VaisalaData data = this.q.poll();
				
				if (data != null) {
					if (this.bufferData.size()<10) {
						this.bufferData.add(data);
						logger.info("Tamaño de la cola para remotos:" + String.valueOf (this.bufferData.size()));
					}
					else {
						
						this.bufferData.add(data);
						String jsonStr = this.createJSONMessage();
						this.bufferData.clear();
						this.sendRemoteData (jsonStr);
					}
				}
				else {
					logger.info("Remote Manager Sleep..");
					Thread.sleep(3000);
				}
					
			} catch (InterruptedException e) {
				this.bufferData.clear();
				logger.error("Error en bucle de RemoteManager");
				e.printStackTrace();
			}
		}
	}
} //Fin de la clase 

	
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

/**
{
  "data": {
    "haizea_id": "some_value",
    "name": "some_value",
    "ip": "some_value",
    "values": [
      {
        "date": "some_date",
        "windspeed": "some_speed",
        "winddirec": "some_direction"
      },
      // ... (more entries for each VaisalaData in bufferData)
    ]
  },
  "name": {
    "haizea_id": "some_value",
    "name": "some_value",
    "ip": "some_value",
    "values": [
      {
        "date": "some_date",
        "windspeed": "some_speed",
        "winddirec": "some_direction"
      },
      // ... (more entries for each VaisalaData in bufferData)
    ]
  }
}


**/
