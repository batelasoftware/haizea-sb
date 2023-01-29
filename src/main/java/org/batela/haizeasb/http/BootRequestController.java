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
@RequestMapping (path="api/v1/bootrequest")
public class BootRequestController {
	
	private final BootRequestService bootRequestService;
	
	@Autowired
	public BootRequestController(BootRequestService bootRequestService) {
		this.bootRequestService = bootRequestService;
	}


	@GetMapping
	public List <BootRequest> getBootRequest (){
		return this.bootRequestService.getBootRequest();
	}

	@PostMapping
	public void newBootRequest (@RequestBody BootRequest br) {
		
	}
}
