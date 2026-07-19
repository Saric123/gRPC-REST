package gRPC_REST.communication.REST_client;

import gRPC.on.REST.client.side.Message;
import gRPC.on.REST.client.side.ReadingGrpc;
import io.grpc.stub.StreamObserver;


public class Service extends ReadingGrpc.ReadingImplBase{
	
	private Reading currentSensorReading;
	public void requestReading(Message request, StreamObserver<Message> responseObserver) {
	
		// Create response
	    Message response = Message.newBuilder().setTemperature(currentSensorReading.getTemperature())
	    									   .setPressure(currentSensorReading.getPressure())
	    									   .setHumidity(currentSensorReading.getHumidity())
	    									   .setCo(currentSensorReading.getCo())
	    									   .setNo2(currentSensorReading.getNo2())
	    									   .setSo2(currentSensorReading.getSo2()).build();
	    // Send response
	    responseObserver.onNext(
	        response
	    );

	    
	    // Send a notification of successful stream completion.
	    responseObserver.onCompleted();
	  
		
	}
	public Reading getCurrentSensorReading() {
		return currentSensorReading;
	}
	public void setCurrentSensorReading(Reading currentSensorReading) {
		this.currentSensorReading = currentSensorReading;
	}
}

