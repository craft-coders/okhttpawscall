package de.craftcoders.okhttpcallaws;

import com.amazonaws.http.HttpResponse;
import com.amazonaws.util.IOUtils;

import java.io.IOException;

/**
 * Container for amazon http responses that stores the body as byte array along the HTTPResponse object.
 *
 * This is necessary because by the time we try to read the input stream of the HTTPResponse object,
 * it might already be closed.
 */
public class AzHttpResponseContainer {
    private HttpResponse httpResponse;
    private byte[]       body;

    public AzHttpResponseContainer(HttpResponse httpResponse) throws IOException  {
        this.httpResponse = httpResponse;
        if (httpResponse.getContent() != null) {
            this.body = IOUtils.toByteArray(httpResponse.getContent());
        } else {
            this.body = new byte[]{};
        }
    }

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }

    public byte[] getBody() {
        return body;
    }
}
