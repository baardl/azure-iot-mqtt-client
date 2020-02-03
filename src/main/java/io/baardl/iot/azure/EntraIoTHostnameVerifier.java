package io.baardl.iot.azure;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class EntraIoTHostnameVerifier implements HostnameVerifier {
    @Override
    public boolean verify(String hostname, SSLSession session) {
        return true;
    }
}
