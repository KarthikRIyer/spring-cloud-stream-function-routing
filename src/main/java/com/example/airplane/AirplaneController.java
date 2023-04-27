package com.example.airplane;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class AirplaneController {
    @Autowired
    StreamBridge streamBridge;

    @GetMapping("publishPlane")
    public void publishPlane() {
        PlaneEvent planeEvent = new PlaneEvent(UUID.randomUUID().toString(), "CITY");
        Message<PlaneEvent> message = MessageBuilder.withPayload(planeEvent).setHeader("Type", PlaneEvent.class.getSimpleName())
                .build();
        streamBridge.send("planeEventProducer", message);
    }
}
