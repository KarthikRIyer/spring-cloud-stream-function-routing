import org.springframework.cloud.stream.binder.test.InputDestination
import org.springframework.cloud.stream.binder.test.OutputDestination
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.messaging.SubscribableChannel
import org.springframework.util.ReflectionUtils

import java.lang.reflect.Field
import java.util.stream.Collectors

class TestUtil {
    static Map<String, SubscribableChannel> getInputChannels(InputDestination inputDestination) {
        Field chField = ReflectionUtils.findField(InputDestination.class, "channels")
        chField.setAccessible(true);
        List<SubscribableChannel> channels = chField.get(inputDestination) as List<SubscribableChannel>
        return channels.stream().collect(Collectors.toMap(x -> getChannelName(x).replace(".destination", ""), x -> x))
    }

    static Map<String, SubscribableChannel> getOutputChannels(OutputDestination outputDestination) {
        Field chField = ReflectionUtils.findField(OutputDestination.class, "channels")
        chField.setAccessible(true);
        List<SubscribableChannel> channels = chField.get(outputDestination) as List<SubscribableChannel>
        return channels.stream().collect(Collectors.toMap(x -> getChannelName(x).replace(".destination", ""), x -> x))
    }

    static String getChannelName(SubscribableChannel ch) {
        Field chNameField = ReflectionUtils.findField(PublishSubscribeChannel.class, "fullChannelName")
        chNameField.setAccessible(true)
        try {
            return chNameField.get(ch) as String
        } catch (IllegalAccessException e) {
            System.err.println(e)
            return ""
        }
    }
}