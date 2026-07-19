package gRPC_REST.communication.REST_server;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorRepository extends JpaRepository<SensorData, Integer>{
}
