package de.craftcoders.okhttpcallaws;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.http.AmazonHttpClient;
import com.amazonaws.internal.auth.DefaultSignerProvider;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import okhttp3.*;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CallAwsInterceptorTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

    @Test
    public void CallAwsInterceptorWithoutCredentialsTest() throws Exception {
        // Setup
        OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(CallAwsInterceptor.defaultUnauthenticatedCallAwsInterceptor("execute-api"))
            .build();

        stubFor(get(urlEqualTo("/abc"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "text/plain")
                .withBody("Hello world!")
            )
        );

        Request request = new Request.Builder()
            .url(wireMockRule.url("/abc"))
            .build();

        // Act
        Response response = client.newCall(request).execute();

        // Validate
        Assert.assertEquals( 200, response.code());
        Assert.assertEquals("Hello world!", response.body().string());

        verify(getRequestedFor(urlEqualTo("/abc")));
    }

    @Test
    public void CallAwsInterceptorWithoutCredentials403Test() throws Exception {
        // Setup
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(CallAwsInterceptor.defaultUnauthenticatedCallAwsInterceptor("execute-api"))
                .build();

        stubFor(get(urlEqualTo("/abc"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "text/plain")
                .withBody("Forbidden")
                .withStatus(403)
            )
        );

        Request request = new Request.Builder()
            .url(wireMockRule.url("/abc"))
            .build();

        // Act
        Response response = client.newCall(request).execute();

        // Validate
        Assert.assertEquals( 403, response.code());
        Assert.assertEquals("Forbidden", response.body().string());

        verify(getRequestedFor(urlEqualTo("/abc")));
    }

    @Test
    public void CallAwsInterceptorWithCredentialsTest() throws Exception {
        // Setup
        ClientConfiguration clientConfiguration = new ClientConfiguration().withSocketTimeout(1 * 1000).withMaxErrorRetry(0);

        AWS4Signer aws4Signer = new AWS4Signer();
        aws4Signer.setServiceName("execute-api");

        AWSCredentialsProvider awsCredentialsProvider = mock(AWSCredentialsProvider.class);
        AWSCredentials awsCredentials = mock(AWSCredentials.class);
        when(awsCredentialsProvider.getCredentials()).thenReturn(awsCredentials);
        when(awsCredentials.getAWSAccessKeyId()).thenReturn("AKIAIOSFODNN7EXAMPLE");
        when(awsCredentials.getAWSSecretKey()).thenReturn("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");

        CallAwsInterceptor callAwsInterceptor = new CallAwsInterceptor(
                new AmazonHttpClient(clientConfiguration),
                new DefaultSignerProvider(null, aws4Signer),
                awsCredentialsProvider,
                "execute-api"
        );

        OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(callAwsInterceptor)
            .build();

        stubFor(put(urlEqualTo("/abc"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "text/plain")
                .withBody("Put")
            )
        );

        Request request = new Request.Builder()
            .url(wireMockRule.url("/abc"))
                .method("PUT", RequestBody.create("{\"abc\": 123}", MediaType.parse("application/json")))
            .build();

        // Act
        Response response = client.newCall(request).execute();

        // Validate
        Assert.assertEquals( 200, response.code());
        Assert.assertEquals("Put", response.body().string());

        verify(putRequestedFor(urlEqualTo("/abc"))
            .withRequestBody(equalToJson("{\"abc\": 123}"))
            .withHeader("Authorization", matching("AWS4-HMAC-SHA256 Credential=AKIAIOSFODNN7EXAMPLE/[0-9]{8}/us-east-1/execute-api/aws4_request, SignedHeaders=amz-sdk-invocation-id;amz-sdk-request;amz-sdk-retry;content-type;host;user-agent;x-amz-date, Signature=.*"))
            .withHeader("Content-Type", matching("application/json.*"))
            .withHeader("x-amz-date", matching("[0-9]*T[0-9]*Z"))
        );
    }
}
