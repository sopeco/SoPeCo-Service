package org.sopeco.service.persistence.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class MECEntity implements Serializable {

	private static final long serialVersionUID = 3093838427924558713L;

	@Id
	@Column(name = "url")
	private String url;

	@Column(name = "timeAdded")
	private long timeAdded;

	public String getUrl() {
		return url;
	}

	public void setUrl(String newUrl) {
		this.url = newUrl;
	}

	public long getAdded() {
		return timeAdded;
	}

	public void setAdded(long time) {
		this.timeAdded = time;
	}
	
}