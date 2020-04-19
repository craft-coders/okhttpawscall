package de.craftcoders.okhttpcallaws;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Convert aws http responses to okhttp responses.
 */
public class AzResponseToOk {

    private static okhttp3.Headers azHeadersToOk(Map<String, String> azHeaders) {
        okhttp3.Headers.Builder okHeadersBuilder = new okhttp3.Headers.Builder();

        for (Map.Entry<String, String> set : azHeaders.entrySet()) {
            okHeadersBuilder.add(set.getKey(), set.getValue());
        }

        return okHeadersBuilder.build();
    }

    private static okhttp3.MediaType extractMediaType(okhttp3.Headers okHeaders) {
        okhttp3.MediaType okMediaType = null;

        if (okHeaders.get("content-type") != null) {
            okMediaType = okhttp3.MediaType.parse(okHeaders.get("content-type"));
        }

        return okMediaType;
    }

    public static okhttp3.Response azHttpResponseToOkResponse(AzHttpResponseContainer responseContainer, okhttp3.Request okRequest) throws IOException {
        com.amazonaws.http.HttpResponse azHttpResponse = responseContainer.getHttpResponse();
        byte[] body = responseContainer.getBody();

        okhttp3.Headers okHeaders = azHeadersToOk(azHttpResponse.getHeaders());

        okhttp3.Response okResponse = new okhttp3.Response.Builder()
            .code(azHttpResponse.getStatusCode())
            .headers(okHeaders)
            .protocol(okhttp3.Protocol.HTTP_1_1)
            .body(okhttp3.ResponseBody.create(extractMediaType(okHeaders), body))
            .message(azHttpResponse.getStatusText())
            .request(okRequest)
            .build();

        return okResponse;
    }
}
