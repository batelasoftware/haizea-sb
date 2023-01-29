package org.batela.haizeasb.http;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class WindcapDataService {

	public List <WindcapData> getBootRequest (){
		return  List.of (new WindcapData());
	}

	
}
