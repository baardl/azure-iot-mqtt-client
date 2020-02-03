package io.baardl.iot.azure;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Device;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.slf4j.LoggerFactory.getLogger;

public class DeviceTwinClient {
    private static final Logger log = getLogger(DeviceTwinClient.class);

    private DeviceClient client;
    private Device dataCollector;

    public DeviceTwinClient(String iotHubConnectionString) throws URISyntaxException {
        IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;
        client = new DeviceClient(iotHubConnectionString, protocol);
        dataCollector = new Device() {
            // Print details when a property value changes
            @Override
            public void PropertyCall(String propertyKey, Object propertyValue, Object context) {
                System.out.println(propertyKey + " changed to " + propertyValue);
            }
        };

    }


    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        String iotHubConnectionString = "HostName=xx.azure-devices.net;DeviceId=yyy;SharedAccessKey=zz";
        iotHubConnectionString = args[0];
        DeviceTwinClient deviceTwinClient = new DeviceTwinClient(iotHubConnectionString);
        deviceTwinClient.updateProperty("IP", "127.0.0.1");
        Thread.sleep(2000);
        log.info("Shutting down.");
        deviceTwinClient.disconnect();
    }

    private void disconnect() throws IOException {
        if (client != null) {
            client.closeNow();
        }
    }

    void updateProperty(String key, String value) throws IOException {
        try {
            // Open the DeviceClient and start the device twin services.
            client.open();
            client.startDeviceTwin(new DeviceTwinStatusCallBack(), null, dataCollector, null);

            // Create a reported property and send it to your IoT hub.
            dataCollector.setReportedProp(new Property(key, value));
            client.sendReportedProperties(dataCollector.getReportedProp());
        }
        catch (Exception e) {
            System.out.println("On exception, shutting down \n" + " Cause: " + e.getCause() + " \n" + e.getMessage());
            dataCollector.clean();
            client.closeNow();
            System.out.println("Shutting down...");
        }
    }

    protected static class DeviceTwinStatusCallBack implements IotHubEventCallback {
        @Override
        public void execute(IotHubStatusCode status, Object context) {
            System.out.println("IoT Hub responded to device twin operation with status " + status.name());
        }
    }
}
