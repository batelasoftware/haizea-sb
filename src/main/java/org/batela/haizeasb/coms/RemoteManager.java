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
		
		JSONObject root=new JSONObject();
		JSONObject local=new JSONObject();
		local.put("haizea_id",new Integer(0));    
		local.put("name","");    
		local.put("ip","");    
		
		
		JSONArray values = new JSONArray();  
		
		for (VaisalaData vd : this.bufferData) {
			JSONObject val=new JSONObject();
			val.put("date",vd.getDt() );
			val.put ("windspeed",vd.getWindspeed());
//	http://marketplace.eclipse.org/marketplace-client-intro?mpc_install=1944539		val.put ("winddir",vd.getWinddirec());
			values.put(val);
		}
		local.put("values", values);
		root.put("data", local);
		return root.toString();
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
				if (data != null) {
					if (this.bufferData.size()<60) {
						this.bufferData.add(data);
					}
					else {
						this.bufferData.add(data);
						this.sendRemoteData ();
					}
				}
				else {
					Thread.sleep(3000);
					logger.info("Doing sleep");

				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	
}
