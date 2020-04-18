package de.craftcoders.okhttpcallaws;

import com.amazonaws.util.IOUtils;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class OkRequestToAzTest {

    @Test
    public void OkRequestToAzPutTest() throws Exception {
        // Setup
        okhttp3.RequestBody okBody = RequestBody.create("{a: 123}", MediaType.parse("application/json"));
        okhttp3.Request okRequest = new okhttp3.Request.Builder()
            .addHeader("X-Test", "123456")
            .url("http://example.org/123?abc=123")
            .method("PUT", okBody)
            .build();

        // Act
        com.amazonaws.Request<Void> azRequest = OkRequestToAz.okRequestToAz(okRequest, "execute-api");

        // Validate
        Assert.assertEquals("execute-api", azRequest.getServiceName());
        Assert.assertEquals("http://example.org", azRequest.getEndpoint().toString());
        Assert.assertEquals("/123", azRequest.getResourcePath());
        Assert.assertEquals(1, azRequest.getParameters().size());
        Assert.assertEquals("123", azRequest.getParameters().get("abc").get(0));
        Assert.assertEquals("123456", azRequest.getHeaders().get("X-Test"));
        Assert.assertTrue(azRequest.getHeaders().get("Content-Type").startsWith("application/json"));
    }

    @Test
    public void OkRequestToAzDeleteWithoutBodyTest() throws Exception {
        // Setup
        okhttp3.RequestBody okBody = RequestBody.create("{a: 123}", MediaType.parse("application/json"));
        okhttp3.Request okRequest = new okhttp3.Request.Builder()
                .url("http://example.org/123")
                .method("DELETE", null)
                .build();

        // Act
        com.amazonaws.Request<Void> azRequest = OkRequestToAz.okRequestToAz(okRequest, "execute-api");

        // Validate
        Assert.assertEquals("execute-api", azRequest.getServiceName());
        Assert.assertEquals(0, azRequest.getParameters().size());
        Assert.assertEquals(null, azRequest.getHeaders().get("Content-Type"));
        Assert.assertEquals("", IOUtils.toString(azRequest.getContent()));
    }

    @Test(expected = IOException.class)
    public void OkRequestToAzDeleteWithBodyTest() throws Exception {
        // Setup
        okhttp3.RequestBody okBody = RequestBody.create("{a: 123}", MediaType.parse("application/json"));

        okhttp3.Request okRequest = new okhttp3.Request.Builder()
                .url("http://example.org/123")
                .method("DELETE", okBody)
                .build();

        // Act
        com.amazonaws.Request<Void> azRequest = OkRequestToAz.okRequestToAz(okRequest, "execute-api");
    }
}
