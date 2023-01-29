package org.batela.haizeasb.http;

import java.time.LocalDate;
import java.util.Date;

public class BootRequest {
	private Integer id;
	private String name;
	private LocalDate creationDate;
	
	public BootRequest () {
		
	}
	
	public BootRequest(Integer id, String name, LocalDate creationDate) {
		super();
		this.id = id;
		this.name = name;
		this.creationDate = creationDate;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public LocalDate getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(LocalDate creationDate) {
		this.creationDate = creationDate;
	}
	@Override
	public String toString (){
		return "BootRequest{" +
				"id=" + id +
				"name=" + name +
				"date="+ creationDate.toString() +	"}" ;
	}

}
