package de.craftcoders.okhttpcallaws;

import com.amazonaws.util.StringInputStream;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AzResponseToOkTest {

    @Test
    public void AzResponseToOkTestTest() throws Exception {
        // Setup
        okhttp3.Request okRequest = new okhttp3.Request.Builder().url("http://example.org").build();
        com.amazonaws.http.HttpResponse httpResponse = mock(com.amazonaws.http.HttpResponse.class);

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "text/html");
        headers.put("via", "1.1 test");

        String content = "<html><body><h1>Hello World</h1></body></html>";

        when(httpResponse.getStatusCode()).thenReturn(200);
        when(httpResponse.getStatusText()).thenReturn("OK");
        when(httpResponse.getHeaders()).thenReturn(headers);
        when(httpResponse.getContent()).thenReturn(new StringInputStream(content));
        when(httpResponse.getHeaderValues("Content-Type")).thenReturn(Arrays.asList(headers.get("Content-Type")));
        when(httpResponse.getHeaderValues("via")).thenReturn(Arrays.asList(headers.get("via")));

        // Act
        okhttp3.Response okResponse = AzResponseToOk.azHttpResponseToOkResponse(new AzHttpResponseContainer(httpResponse), okRequest);

        // Validate
        Assert.assertEquals(200, okResponse.code());
        Assert.assertEquals("text/html", okResponse.header("Content-Type"));
        Assert.assertEquals("1.1 test", okResponse.header("via"));
        Assert.assertEquals(content, okResponse.body().string());
        Assert.assertEquals("text/html", okResponse.body().contentType().toString());
    }
}
