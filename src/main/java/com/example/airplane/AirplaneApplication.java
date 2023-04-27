package com.example.airplane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.function.Consumer;
import java.util.function.Function;

@SpringBootApplication
public class AirplaneApplication {

	public static void main(String[] args) {
		SpringApplication.run(AirplaneApplication.class, args);
	}

	Logger logger = LoggerFactory.getLogger(AirplaneApplication.class);

	@Autowired
	BindingServiceProperties bindingServiceProperties;

	@Bean("ArrivalEvent")
	public Consumer<Message<ArrivalEvent>> arrivalEventConsumer() {
		return msg -> {
			logger.info("ArrivalEvent consumed: {}", msg.getPayload());
		};
	}

	@Bean("LandEvent")
	public Function<Message<LandEvent>, Message<ArrivalEvent>> landEventProcessor() {
		return landEventMessage -> {
			ArrivalEvent arrivalEvent = new ArrivalEvent(landEventMessage.getPayload().getFlightId()+"-flight", landEventMessage.getPayload().getCurrentAirport());
			logger.info("Publishing ArrivalEvent: {}", arrivalEvent);
			return MessageBuilder.withPayload(arrivalEvent)
					.setHeader("Type", ArrivalEvent.class.getSimpleName())
					.setHeader("spring.cloud.stream.sendto.destination", "arrivalEventProducer")
					.build();
		};
	}

	@Bean("FlightEvent")
	public Function<Message<FlightEvent>, Message<LandEvent>> flightEventProcessor() {
		return flightEventMessage -> {
			LandEvent landEvent = new LandEvent(flightEventMessage.getPayload().getFlightId()+"-flight", flightEventMessage.getPayload().getCurrentAirport());
			logger.info("Publishing LandEvent: {}", landEvent);
			return MessageBuilder.withPayload(landEvent)
					.setHeader("Type", LandEvent.class.getSimpleName())
					.setHeader("spring.cloud.stream.sendto.destination", "landEventProducer")
					.build();
		};
	}

	@Bean("PlaneEvent")
	public Function<Message<PlaneEvent>, Message<FlightEvent>> planeEventProcessor() {
		return planeEventMessage -> {
			FlightEvent flightEvent = new FlightEvent(planeEventMessage.getPayload().getPlaneId()+"-flight", planeEventMessage.getPayload().getCurrentAirport());
			logger.info("Publishing Flight: {}", flightEvent);
			return MessageBuilder.withPayload(flightEvent)
					.setHeader("Type", FlightEvent.class.getSimpleName())
					.setHeader("spring.cloud.stream.sendto.destination", "flightEventProducer")
					.build();
		};
	}

}
