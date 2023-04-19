import com.example.airplane.AirplaneApplication
import com.example.airplane.PlaneEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.binder.test.InputDestination
import org.springframework.cloud.stream.binder.test.OutputDestination
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.messaging.Message
import org.springframework.messaging.SubscribableChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Title

import java.util.concurrent.ConcurrentLinkedQueue

@SpringBootTest(classes = [AirplaneApplication.class])
@ActiveProfiles("test")
@Stepwise
@Title("Sample Test")
class SampleTest extends Specification {

    @Autowired
    StreamBridge streamBridge

    @Autowired
    OutputDestination outputDestination

    @Autowired
    InputDestination inputDestination

    class TestState {
        Map<String, SubscribableChannel> inputChannels
        Map<String, SubscribableChannel> outputChannels

        Map<String, Queue<Object>> inputMessages
        Map<String, Queue<Object>> outputMessages
    }

    TestState testState = new TestState()

    def initTestState(InputDestination inDest, OutputDestination outDest) {
        def inputChannels = TestUtil.getInputChannels(inDest)
        def outputChannels = TestUtil.getOutputChannels(outDest)
        getTestState().setInputChannels(inputChannels)
        getTestState().setOutputChannels(outputChannels)
        getTestState().setInputMessages(new HashMap<String, Queue<Object>>())
        getTestState().setOutputMessages(new HashMap<String, Queue<Object>>())

        getTestState().getInputChannels().keySet()
                .forEach(channelName -> getTestState().getInputMessages().put(channelName, new ConcurrentLinkedQueue<String>()))
        getTestState().getInputChannels().entrySet().stream()
                .forEach(inputChannelEntry -> {
                    def subscribableChannel = inputChannelEntry.getValue()
                    def channelName = inputChannelEntry.getKey()
                    subscribableChannel.subscribe(m -> {
                        getTestState().getInputMessages().get(channelName).add(m)
                    })
                })

        getTestState().getOutputChannels().keySet()
                .forEach(channelName -> getTestState().getOutputMessages().put(channelName, new ConcurrentLinkedQueue<String>()))
        getTestState().getOutputChannels().entrySet().stream()
                .forEach(outputChannelEntry -> {
                    def subscribableChannel = outputChannelEntry.getValue()
                    def channelName = outputChannelEntry.getKey()
                    subscribableChannel.subscribe(m -> {
                        getTestState().getOutputMessages().get(channelName).add(m)
                    })
                })
    }

    def "Sample Test"() {
        given: "A plane event is created"
        initTestState(inputDestination, outputDestination)

        when: "Plane Event is processed"
        Message<PlaneEvent> message = MessageBuilder.withPayload(new PlaneEvent(UUID.randomUUID().toString(), "CITY"))
                .setHeader("Type", PlaneEvent.class.getSimpleName())
        .build()
        streamBridge.send("functionRouter-in-0", message)

        then: "Flight event is raised"
//        def flightEventPayload = getTestState().getOutputMessages().get("flightEventProcessor")
        System.out.println()
    }
}