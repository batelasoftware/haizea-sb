package org.batela.haizeasb;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.yaml.snakeyaml.util.ArrayUtils;

public class MyTests {

	public static void main(String[] args) {
		
		String vdata = "0R1,Dn=128D,Dm=128D,Dx=128D,Sn=0.1K,Sm=0.1K,Sx=0.1K";
		String [] vdata_split = vdata.split (",");
		String wdir = vdata_split[2].split("=")[1];
		String wvel = vdata_split[5].split("=")[1];
		wdir = wdir.substring(0, wdir.length()-1);
		wvel = wvel.substring(0, wvel.length()-1);
		
		String json = "{\"data\":{\"ip\":\"\",\"values\":[{\"date\":\"2023-02-04 07:45:41\",\"windspeed\":128,\"winddirec\":0.4}," +
                "{\"date\":\"2023-02-04 07:45:42\",\"windspeed\":128,\"winddirec\":0.4},"+
                "{\"date\":\"2023-02-04 07:45:43\",\"windspeed\":128,\"winddirec\":0.3},"+
                "{\"date\":\"2023-02-04 07:45:44\",\"windspeed\":128,\"winddirec\":0.3},"+
                "{\"date\":\"2023-02-04 07:45:45\",\"windspeed\":128,\"winddirec\":0.3},"+
                "{\"date\":\"2023-02-04 07:45:46\",\"windspeed\":128,\"winddirec\":0.3},"+
                "{\"date\":\"2023-02-04 07:45:47\",\"windspeed\":128,\"winddirec\":0.3},"+
                "{\"date\":\"2023-02-04 07:45:48\",\"windspeed\":128,\"winddirec\":0.2},"+
                "{\"date\":\"2023-02-04 07:45:49\",\"windspeed\":128,\"winddirec\":0.1},"+
                "{\"date\":\"2023-02-04 07:45:50\",\"windspeed\":128,\"winddirec\":0.2},"+
                "{\"date\":\"2023-02-04 07:45:51\",\"windspeed\":128,\"winddirec\":0.1}],\"name\":\"\",\"haizea_id\":0}}";
		
		


		byte [] data = new byte [250];
    	data[0]= '1';
    	data[1]= 0;
    	String tt = new String (data);
    	System.out.print(tt.length());
    	
    	String result = "";
		// TODO Auto-generated method stub
		
		
//		ArrayList<Byte> arrays = new ArrayLis<Byte>();
//	
//		arrays.add((byte) 65);
//		arrays.add((byte) 66);
//		Byte[] soundBytes = arrays.toArray(new Byte[arrays.size()]);
//		
//		String s = Base64.getEncoder().encodeToString( ArrayUtils.toPrimitive(soundBytes));
//		System.out.print(soundBytes.toString());
//		System.out.print(soundBytes.toString());
	}

}
