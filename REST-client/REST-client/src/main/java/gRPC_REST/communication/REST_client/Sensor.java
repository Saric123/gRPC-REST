package gRPC_REST.communication.REST_client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import gRPC.on.REST.client.side.Message;
import gRPC.on.REST.client.side.ReadingGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.StatusRuntimeException;

public class Sensor {
	
	private static final Logger logger = Logger.getLogger(Sensor.class.getName());
	private ManagedChannel channel;
	private ReadingGrpc.ReadingBlockingStub readingBlockingStub;
	
	private Server server;
	private final Service service;
	private final int serverPort;
	
	
	
	private final String host = "http://localhost:8080";
	private final String grpchost = "127.0.0.1";
	private SensorData data;
	
	private long startTime;
	private String readingsFilePath = "readings.csv";
	private int nearestNeighborsId;
	
	
	//Constructor
		public Sensor(Service service, int serverPort, long startTime) {
			this.service = service;
			this.serverPort = serverPort;
			double randomLongitude = ThreadLocalRandom.current().nextDouble(15.87, 16);
			double randomLattitude = ThreadLocalRandom.current().nextDouble(45.75, 45.85);
			data = new SensorData(randomLongitude, randomLattitude, serverPort, host);
			this.startTime = startTime;
			
		}
		
		public int getServerPort() {
			return serverPort;
		}
		
		
		//READING A READING
		public Reading readReading() {
			long elapsedTime = System.currentTimeMillis() - startTime;
			int timeSeconds = (int)Math.round(elapsedTime/1000.0);
			Reading r;
			try {
				List<String> lines = Files.readAllLines(Paths.get(readingsFilePath));
				int row = (timeSeconds % 100) + 1;
				r = new Reading(lines.get(row), data.getId());
				return r;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}
		
		
		//STARTING GRPC SERVER
		public void start() throws IOException, InterruptedException {
			server = ServerBuilder.forPort(serverPort)
					.addService(service)
					.build()
					.start();
			logger.info("Server started on " + serverPort);
			
		//  Clean shutdown of server in case of JVM shutdown
		    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
		      System.err.println("Shutting down gRPC server since JVM is shutting down");
		      try {
		        Sensor.this.stop();
		      } catch (InterruptedException e) {
		        e.printStackTrace(System.err);
		      }
		      System.err.println("Server shut down");
		    }));
		}
		
		public void stop() throws InterruptedException {
		    if (server != null) {
		      server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
		    }
		  }
		
		public void blockUntilShutdown() throws InterruptedException {
		    if (server != null) {
		      server.awaitTermination();
		    }
		  }
		
		/**
		   * Request reading.
		   */
		public Reading requestReading() {
			
			Message request = Message.newBuilder()
		    		.build();

		    logger.info(data.getId() + " SEND REQUEST FOR A READING OF " + nearestNeighborsId);
		    try {
		      Message response = readingBlockingStub.requestReading(request);
		      logger.info(data.getId() + " RECEIVES RESPONSE FROM " + nearestNeighborsId);
		      return new Reading(response);
		    } catch (StatusRuntimeException e) {
		      logger.info("RPC failed: " + e.getMessage());
		    }
		    return null;
		  }
		
		//SENSOR REGISTRATION
		public void registerSensor() {
			WebClient webClient = WebClient.create(host);

			ResponseEntity<SensorData> response = webClient.post()
														.uri("/sensors")
														.bodyValue(data)
														.exchangeToMono(r -> r.toEntity(SensorData.class))
														.block();
			logger.info("Registration " + response.getStatusCode()+ " "
										+ response.getHeaders().getLocation());
			data.setId(response.getBody().getId());
			}
		
		
		//FINDING NEAREST NEIGHBOR
		public int findNearestNeighbor() {
			WebClient webClient = WebClient.create(host);
			
			ResponseEntity<SensorData> response = webClient.get()
										   .uri("/nearestNeighborOf/{id}", data.getId())
										   .retrieve()
										   .toEntity(SensorData.class)
										   .block();
					
			if(response.getBody() == null) {
				return 0;
			}
			return response.getBody().getServerPort();
		}
		
		
		//SEND READING TO WEB SERVER
		public void sendReading(Reading reading) {
			WebClient webClient = WebClient.create(host);
			
			ResponseEntity<Reading> response = webClient.post()
										   .uri("/readings")
										   .bodyValue(reading)
										   .exchangeToMono(r -> r.toEntity(Reading.class))
										   .block();
			logger.info("READING CREATED: " + response.getStatusCode()+ " " 
											+ response.getHeaders().getLocation());
			}
		
		//READING CALIBRATION
		public Reading readingCalibration(Reading myReading) {
			Reading neighborReading = requestReading();
			Reading finalReading;
			if(neighborReading != null) {
				logger.info("Sensor " + myReading.getId() + " READING: " + myReading.toString());
				logger.info("NEIGHBOR READING OF " + myReading.getId() + ": " + neighborReading.toString());
				finalReading = new Reading(myReading, neighborReading);
				finalReading.setId(myReading.getId());
			} else {
				finalReading = myReading;
			
			}
			
			return finalReading;
			
			
		}
		
		//STARTING A GRPC CLIENT
		public void startClient(int port) {
			channel = ManagedChannelBuilder.forAddress(grpchost, port).usePlaintext().build();
			readingBlockingStub = ReadingGrpc.newBlockingStub(channel);
		}
	
	
		
		public static void main(String[] args) {
			Sensor sensor = new Sensor(new Service(), 3000, System.currentTimeMillis());
			sensor.registerSensor();
			try {
				sensor.start();
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			new Thread(() -> {
				boolean hasNeighbor = false;
				int neighborServerPort = 0;
	            while (true) {
	            	Reading myReading = sensor.readReading();
	            	if(myReading == null) {
	            		return;
	            	}
	            	sensor.service.setCurrentSensorReading(myReading);
	            	if(!hasNeighbor) {
	            		neighborServerPort = sensor.findNearestNeighbor();
	            		if(neighborServerPort != 0) {
	            			hasNeighbor = true;
	            			sensor.nearestNeighborsId = neighborServerPort - 3000+1;
	            		}
	            	}
	            	
	            	if(hasNeighbor) {
	            		if(sensor.channel==null) {
	            			sensor.startClient(neighborServerPort);
	            		}
	            		myReading = sensor.readingCalibration(myReading);
	            		
	            	}
	            	
	            	sensor.sendReading(myReading);
	            	try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                
	            }
	        }).start();
			
			try {
				sensor.blockUntilShutdown();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

}

