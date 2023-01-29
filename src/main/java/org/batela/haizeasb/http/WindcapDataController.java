package org.batela.haizeasb.http;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping (path="api/v1/windcap")
public class WindcapDataController {
	
	private final WindcapDataService wcdService;
	
	@Autowired
	public WindcapDataController(WindcapDataService wcdService) {
		this.wcdService = wcdService;
	}


	@GetMapping
	public List <WindcapData> getBootRequest (){
		return this.wcdService.getBootRequest();
	}

	@PostMapping
	public void newBootRequest (@RequestBody WindcapData br) {
		
	}
}
