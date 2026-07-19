package gRPC_REST.communication.REST_client;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class SensorData {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	private double lattitude, longitude;
	private String ip;
	private int serverPort;
	public SensorData() {
		
	}
	public SensorData(double longitude, double lattitude, int serverPort, String ip) {
		this.longitude = longitude;
		this.lattitude = lattitude;
		this.serverPort = serverPort;
		this.ip = ip;
	}
	public double getLongitude() {
		return longitude;
	}
	
	public double getLattitude() {
		return lattitude;
	}
	
	public int getServerPort() {
		return serverPort;
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getIp() {
		return ip;
	}
	
	@Override
	public String toString() {
		return "SERVER PORT: " + serverPort + ", ID: " + id;
	}	
}
