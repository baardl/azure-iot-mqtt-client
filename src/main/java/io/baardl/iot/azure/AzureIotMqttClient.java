package io.baardl.iot.azure;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;

import static io.baardl.iot.azure.utils.Configuration.readProperty;
import static org.slf4j.LoggerFactory.getLogger;

public class AzureIotMqttClient {
    private static final Logger log = getLogger(AzureIotMqttClient.class);
    private static boolean isConnected = false;

    private IMqttClient subscriber = null;
    private String serverURI = null;

    public static void main(String[] args) {

        AzureIotMqttClient client = new AzureIotMqttClient();
        client.init();
        client.connect();
        client.subscribe("devices/#");
        log.info("Connected? {}", isConnected);

    }

    void init() {


        try {
            String subscriberId = readProperty("deviceId");
            String hostname = readProperty("hostName");
            String sharedKey = readProperty("SharedAccessSignature");
            serverURI = "tcp://" + hostname + ":8883";
            subscriber = new MqttClient(serverURI, subscriberId);
            log.info("subscriber: {}", subscriber);
        } catch (MqttException e) {

            log.info("Failed to connect to: {}", serverURI);
            e.printStackTrace();
        }
    }

    void connect() {
        String deviceId = readProperty("deviceId");
        String hostname = readProperty("hostName");
        String password = readProperty("SharedAccessSignature");
        String username = hostname + "/" + deviceId + "/?api-version=2018-06-30";
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        try {
            subscriber.connect(options);
            isConnected = true;
        } catch (MqttException e) {
            log.info("Failed to connect to {}. Reason: {}", serverURI, e.getMessage());
            e.printStackTrace();
        }
    }

    void subscribe(String topicName) {
        try {
            subscriber.subscribe(topicName, (topic, msg) -> {
                log.trace("From: {}, received: {}", topic, msg.getPayload());
                //            byte[] payload = msg.getPayload();
                // ... payload handling omitted
            });
        } catch (MqttException e) {
            log.info("Failed to receive from {}. Reason: {}", topicName, e.getMessage());
        }
    }
}
