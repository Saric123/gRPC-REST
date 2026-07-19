package gRPC_REST.communication.REST_server;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class SensorService {
	private final SensorRepository sensorRepository;
	private List<Reading> readings = new ArrayList<>();
	
	public SensorService(SensorRepository sensorRepository) {
		this.sensorRepository = sensorRepository;
	}
	public SensorData register(SensorData sensor) {
		if(sensor.getServerPort() >= 3000 
		   && sensor.getLattitude() > 0.0 
		   && sensor.getLongitude() > 0.0) {
			SensorData s = sensorRepository.save(sensor);
			return s;
		}
		return null;
	}
	
	public List<SensorData> getSensors() {
		return sensorRepository.findAll();
	}
	
	public List<Reading> getReadings() {
		return readings;
	}
	
	public List<Reading> getReadingsOf(int id) {
		return readings.stream().filter(r -> r.getId()==id).toList();
	}
	
	public SensorData getSensorForId(Integer id) {
		return sensorRepository.findById(id).get();
	}
	
	
	public boolean addReading(Reading reading) {
		Optional<SensorData> optional = sensorRepository.findById(reading.getId());
		if(optional.isEmpty()) {
			return false;
		}
		readings.add(reading);
		return true;
	}
	
	public List<Integer> getIds() {
		List<Integer> ids = new ArrayList<>();
		for(var s : sensorRepository.findAll()) {
			ids.add(s.getId());
		}
		return ids;
	}
	
	
	public SensorData getNearestNeighborOf(int id) {
		if(sensorRepository.findAll().size() == 1) {
			return null;
		}
		SensorData mySensor = sensorRepository.findById(id).get();
		
		SensorData nearestSensor = null;
		double minDistance = 10000000;
		double distance;
		for(SensorData otherSensor : sensorRepository.findAll()) {
			if(otherSensor.getServerPort() != mySensor.getServerPort()) {
				distance = calculateDistance(mySensor, otherSensor);
				if(distance < minDistance) {
					minDistance = distance;
					nearestSensor = otherSensor;
				}
			}
		}
		return nearestSensor;
		
	}
	
	private double calculateDistance(SensorData mySensor, SensorData otherSensor) {		
		double R = 6371.0;
		double dlon = otherSensor.getLongitude() - mySensor.getLongitude();
		double dlat = otherSensor.getLattitude() - mySensor.getLattitude();
		double a = Math.pow(Math.sin(dlat/2), 2) 
				 + Math.cos(mySensor.getLattitude())
				 * Math.cos(otherSensor.getLattitude()) 
				 * Math.pow(Math.sin(dlon/2), 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double d = R * c;
		return d;
	}
}
