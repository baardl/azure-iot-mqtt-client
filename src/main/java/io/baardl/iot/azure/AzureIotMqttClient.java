package io.baardl.iot.azure;

import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import static io.baardl.iot.azure.utils.Configuration.readProperty;
import static org.slf4j.LoggerFactory.getLogger;

public class AzureIotMqttClient implements MqttCallback {
    private static final Logger log = getLogger(AzureIotMqttClient.class);
    private static boolean isConnected = false;

    private IMqttClient subscriber = null;
    private String serverURI = null;

    public static void main(String[] args) {

        AzureIotMqttClient client = new AzureIotMqttClient();
        client.init();
        try {
            client.connect();
            int i = 0;
            while (!isConnected) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.debug("run: {}", i);
                i++;
            }
            client.subscribe("devices/#");
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("Connected? {}", isConnected);

    }

    void init() {


        try {
            String subscriberId = readProperty("deviceId");
            String hostname = readProperty("hostName");
            String sharedKey = readProperty("SharedAccessSignature");
            serverURI = "ssl://" + hostname + ":8883";
            log.info("Connect using: {}", serverURI);
            subscriber = new MqttClient(serverURI, subscriberId);
            log.info("subscriber: {}", subscriber);
        } catch (MqttException e) {

            log.info("Failed to connect to: {}", serverURI);
            e.printStackTrace();
        }
    }

    void connect() throws MqttException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        String deviceId = readProperty("deviceId");
        String hostname = readProperty("hostName");
        String password = readProperty("SharedAccessSignature");
        String username = hostname + "/" + deviceId + "/?api-version=2018-06-30";

    SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
	sslContext.init(null, null, null);
//

        MqttConnectOptions options = new MqttConnectOptions();
        options.setSocketFactory(sslContext.getSocketFactory());
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        options.setSocketFactory(sslContext.getSocketFactory());
//        HostnameVerifier hostnameVerifier = new EntraIoTHostnameVerifier();
//        options.setSSLHostnameVerifier(hostnameVerifier);
        try {
            subscriber.connect(options);
            subscriber.setCallback(this);
            isConnected = true;
        } catch (MqttException e) {
            log.info("Failed to connect to {}. Reason: {}", serverURI, e.getMessage());
            throw e;

        }
    }

    void subscribe(String topicName) {
        log.info("Subscribe from: {}", topicName);
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

    @Override
    public void connectionLost(Throwable throwable) {
        log.info("Connection lost.", throwable);
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        log.info("Message arrived: {}", s);

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        log.info("DeliveryComplete: {}", iMqttDeliveryToken);
    }
}
