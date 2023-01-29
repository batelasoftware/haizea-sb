package org.batela.haizeasb.http;

import java.time.LocalDate;
import java.util.List;

class WindcapValues {
	private LocalDate date;
	private Float value;
	
	public WindcapValues(LocalDate date, Float value) {
		super();
		this.date = date;
		this.value = value;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public Float getValue() {
		return value;
	}

	public void setValue(Float value) {
		this.value = value;
	}
	
}

public class WindcapData {
	private Integer haizea_id;
	private String name;
	private String ip;
	private List <WindcapValues> values ;
	
	public WindcapData() {
		super();
		this.haizea_id = null;
		this.name 	= null;
		this.ip 	= null;
		this.values = null;
	}
	
	public WindcapData(Integer haizea_id, String name, String ip, List<WindcapValues> values) {
		super();
		this.haizea_id = haizea_id;
		this.name = name;
		this.ip = ip;
		this.values = values;
	}

	public Integer getHaizea_id() {
		return haizea_id;
	}

	public void setHaizea_id(Integer haizea_id) {
		this.haizea_id = haizea_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public List<WindcapValues> getValues() {
		return values;
	}

	public void setValues(List<WindcapValues> values) {
		this.values = values;
	}
	
	
	
	

}
