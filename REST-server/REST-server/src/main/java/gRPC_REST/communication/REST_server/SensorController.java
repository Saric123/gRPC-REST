package gRPC_REST.communication.REST_server;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class SensorController {
	private SensorService sensorService;
	
	public SensorController(SensorService sensorService) {
		this.sensorService = sensorService;
	}
	@PostMapping("/sensors")
	public ResponseEntity<SensorData> register(@RequestBody SensorData sensor) {
		SensorData savedSensor = sensorService.register(sensor);
		
		if(savedSensor != null) {
			URI location = ServletUriComponentsBuilder
		            .fromCurrentRequest()
		            .path("/{id}")
		            .buildAndExpand(savedSensor.getId())
		            .toUri();
			
			return ResponseEntity.created(location).body(savedSensor);
		}
		return ResponseEntity.status(401).build();
	}
	
	@PostMapping("/readings")
	public ResponseEntity<Reading> postReading(@RequestBody Reading reading) {
		if(sensorService.addReading(reading)) {
			URI location = ServletUriComponentsBuilder
		            .fromCurrentRequest()
		            .path("/{id}")
		            .buildAndExpand(reading.getId())
		            .toUri();
			return ResponseEntity.created(location).build();
		}
		return ResponseEntity.status(204).build();
	}
	
	@GetMapping("/sensors")
	public List<SensorData> getSensors() {
		return sensorService.getSensors();
	}
	
	@GetMapping("/readings")
	public List<Reading> getReadings() {
		return sensorService.getReadings();
	}
	
	@GetMapping("/readings/{id}")
	public List<Reading> getReadingsOf(@PathVariable("id") int id) {
		return sensorService.getReadingsOf(id);
	}
	
	@GetMapping("/nearestNeighborOf/{id}")
	public ResponseEntity<SensorData> searchNeighborsServerPort(@PathVariable("id") int id) {
		SensorData nearestNeighbor = sensorService.getNearestNeighborOf(id);
		if(nearestNeighbor == null) {
			return ResponseEntity.noContent().build();
		}
		return 	ResponseEntity.status(200).body(nearestNeighbor);
	}

	@GetMapping("/sensors/ids")
    public List<Integer> getSensorIds() {
		
        return sensorService.getIds(); 
    }
}
