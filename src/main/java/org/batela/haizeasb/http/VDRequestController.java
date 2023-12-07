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
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping (path="api/v1/vaisaladata")
public class VDRequestController {
	
	private final BootRequestService bootRequestService;
	
	@Autowired
	public VDRequestController(BootRequestService bootRequestService) {
		this.bootRequestService = bootRequestService;
	}


	@GetMapping
	public List<VaisalaDataRequest> getBootRequest (){
		return this.bootRequestService.getBootRequest();
	}

	@PostMapping
	public void newBootRequest (@RequestBody VDRequest br) {
		
	}
}
