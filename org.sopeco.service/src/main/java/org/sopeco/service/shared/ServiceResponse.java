package org.sopeco.service.shared;

public class ServiceResponse<T> {

	private T object;
	
	public ServiceResponse() {
		
	}
	
	public ServiceResponse(T object) {
		this.object = object;
	}
	
	public T getObject() {
		return object;
	}
	
	public void setObject(T object) {
		this.object = object;
	}
	
}
