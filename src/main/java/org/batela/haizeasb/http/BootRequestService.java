package org.batela.haizeasb.http;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.batela.haizeasb.HaizeaSbApplication;
import org.springframework.stereotype.Service;

@Service
public class BootRequestService {

	public List<VaisalaDataRequest> getBootRequest (){
		HaizeaSbApplication.setLastRemoteRequest();
		return  List.of (new VaisalaDataRequest(HaizeaSbApplication.getLastReadings().getWindspeed(), 
				HaizeaSbApplication.getLastReadings().getWinddirec(), HaizeaSbApplication.getLastReadings().getDataDateStr()));
		//return  List.of (new BootRequest(1, "2", LocalDate.of(2022, Month.NOVEMBER, 17)));
		
	}

	
}
