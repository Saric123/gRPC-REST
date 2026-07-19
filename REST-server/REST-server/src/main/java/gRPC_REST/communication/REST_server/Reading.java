package gRPC_REST.communication.REST_server;

import com.fasterxml.jackson.annotation.JsonInclude;

import gRPC.on.REST.server.side.Message;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Reading {
	
	private String temperature, pressure, humidity, co, no2, so2;	
	private Integer id;
	
	public Reading() {
		
	}
	
	public Reading(Reading reading1, Reading reading2) {
		temperature = String.valueOf((Double.parseDouble(reading1.getTemperature())
									+ Double.parseDouble(reading2.getTemperature()))/2);
		temperature = Double.parseDouble(temperature) == 0 ? null : temperature;
		
		pressure = String.valueOf((Double.parseDouble(reading1.getPressure())
				+ Double.parseDouble(reading2.getPressure()))/2);
		pressure = Double.parseDouble(pressure) == 0 ? null : pressure;
		
		humidity = String.valueOf((Double.parseDouble(reading1.getHumidity())
				+ Double.parseDouble(reading2.getHumidity()))/2);
		humidity = Double.parseDouble(humidity) == 0 ? null : humidity;
		
		co = String.valueOf((Double.parseDouble(reading1.getCo())
				+ Double.parseDouble(reading2.getCo()))/2);
		co = Double.parseDouble(co) == 0 ? null : co;
		
		no2 = String.valueOf((Double.parseDouble(reading1.getNo2())
				+ Double.parseDouble(reading2.getNo2()))/2);
		no2 = Double.parseDouble(no2) == 0 ? null : no2;
		
		so2 = String.valueOf((Double.parseDouble(reading1.getSo2())
				+ Double.parseDouble(reading2.getSo2()))/2);
		so2 = Double.parseDouble(so2) == 0 ? null : so2;
		
		
	}
	
	public Reading(Message message) {
		temperature = message.getTemperature();
		pressure = message.getPressure();
		humidity = message.getHumidity();
		co = message.getCo();
		no2 = message.getNo2();
		so2 = message.getSo2();
		
	}
	public Reading(String reading, Integer id) {
		this.setId(id);
		String[] measures = reading.split(",", -1);
		temperature = measures[0] != "" ? measures[0] : "0";
		pressure = measures[1] != "" ? measures[1] : "0";
		humidity = measures[2] != "" ? measures[2] : "0";
		co = measures[3] != "" ? measures[3] : "0";
		no2 = measures[4] != "" ? measures[4] : "0";
		so2 = measures[5] != "" ? measures[5] : "0";
	}
	
	public String getTemperature() {
		return temperature;
	}
	public String getPressure() {
		return pressure;
	}
	public String getHumidity() {
		return humidity;
	}
	public String getCo() {
		return co;
	}
	public String getNo2() {
		return no2;
	}
	public String getSo2() {
		return so2;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	@Override 
	public String toString() {
		return temperature +  ", " + pressure + ", " + humidity + ", " + co + ", " + no2 + ", " + so2;
	}
	
}