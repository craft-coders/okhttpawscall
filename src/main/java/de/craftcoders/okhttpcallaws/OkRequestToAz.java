package de.craftcoders.okhttpcallaws;

import com.amazonaws.http.HttpMethodName;
import okhttp3.HttpUrl;
import okio.Buffer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Convert okhttp request to amazon requests.
 */
public class OkRequestToAz {

    private static URI getBaseUrl(URI uri) throws IOException {
        try {
            return new URI(uri.getScheme(),
                    uri.getAuthority(),
                    null,
                    null, // Ignore the query part of the input url
                    uri.getFragment());
        } catch (URISyntaxException e) {
            throw  new IOException("malformed uri", e);
        }
    }

    private static Map<String, List<String>> getParametersFromUrl(HttpUrl url) throws IOException {
        Map<String, List<String>> parameters = new HashMap<String, List<String>>();

        for (String name: url.queryParameterNames()) {
            parameters.put(name, url.queryParameterValues(name));
        }

        return parameters;
    }

    private static Map<String, String> okHeadersToAz(okhttp3.Headers okHeaders) {
        Map<String, String> azHeaders = new HashMap<String, String>(){};

        for(int i = 0; i < okHeaders.size(); i++) {
            String name = okHeaders.name(i).toLowerCase(Locale.US);

            azHeaders.put(name, okHeaders.value(i));
        }

        return azHeaders;
    }

    private static byte[] extractBody(okhttp3.Request okRequest) throws IOException {
        Buffer buffer = new Buffer();

        if (okRequest.body() != null) {
            okRequest.body().writeTo(buffer);
        }

        return buffer.readByteArray();
    }


    public static com.amazonaws.Request<Void> okRequestToAz(okhttp3.Request okRequest, String serviceName) throws IOException {
        com.amazonaws.Request<Void> azRequest = new com.amazonaws.DefaultRequest<Void>(serviceName);

        HttpMethodName httpMethodName = com.amazonaws.http.HttpMethodName.fromValue(okRequest.method());
        byte[] body = extractBody(okRequest);

        // Unfortunately the AmazonHttpClient does not support requests with body for DELETE, HEAD and OPTIONS requests.
        // See com.amazonaws.http.apache.request.impl::createApacheRequest()
        boolean bodyNotSupported = (httpMethodName == HttpMethodName.DELETE || httpMethodName == HttpMethodName.HEAD || httpMethodName == HttpMethodName.OPTIONS);
        if (body.length > 0 && bodyNotSupported) {
            throw new IOException("Body in requests is not supported for DELETE, HEAD and OPTIONS requests.");
        }

        azRequest.setHttpMethod(httpMethodName);
        azRequest.setContent(new ByteArrayInputStream(body));
        azRequest.setEndpoint(getBaseUrl(okRequest.url().uri()));
        azRequest.setResourcePath(okRequest.url().uri().getPath());
        azRequest.setParameters(getParametersFromUrl(okRequest.url()));
        azRequest.setHeaders(okHeadersToAz(okRequest.headers()));

        if (okRequest.body() != null && okRequest.body().contentType() != null) {
            azRequest.getHeaders().put("Content-Type", okRequest.body().contentType().toString());
        }

        return azRequest;
    }
}
