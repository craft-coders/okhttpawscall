package de.craftcoders.okhttpcallaws;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.AmazonHttpClient;
import com.amazonaws.http.ExecutionContext;
import com.amazonaws.internal.auth.DefaultSignerProvider;
import com.amazonaws.internal.auth.SignerProvider;

import java.io.IOException;

/**
 * Make a request to aws.
 */
public class CallAwsInterceptor implements okhttp3.Interceptor {

    public static CallAwsInterceptor defaultAuthenticatedCallAwsInterceptor(String serviceName) {
        ClientConfiguration clientConfiguration = new ClientConfiguration().withSocketTimeout(1 * 1000).withMaxErrorRetry(0);
        AWS4Signer aws4Signer = new AWS4Signer();
        aws4Signer.setServiceName(serviceName);

        return new CallAwsInterceptor(
                new AmazonHttpClient(clientConfiguration),
                new DefaultSignerProvider(null, aws4Signer),
                new DefaultAWSCredentialsProviderChain(),
                serviceName
        );
    }

    public static CallAwsInterceptor defaultUnauthenticatedCallAwsInterceptor(String serviceName) {
        ClientConfiguration clientConfiguration = new ClientConfiguration().withSocketTimeout(1 * 1000).withMaxErrorRetry(0);

        return new CallAwsInterceptor(
                new AmazonHttpClient(clientConfiguration),
                null,
                null,
                serviceName
        );
    }

    private com.amazonaws.http.HttpResponseHandler<AzHttpResponseContainerWrapperException> httpErrorHandler = new com.amazonaws.http.HttpResponseHandler<AzHttpResponseContainerWrapperException>() {
        @Override
        public AzHttpResponseContainerWrapperException handle(com.amazonaws.http.HttpResponse httpResponse) throws IOException {
            return new AzHttpResponseContainerWrapperException(new AzHttpResponseContainer(httpResponse));
        }

        @Override
        public boolean needsConnectionLeftOpen() {
            return false;
        }
    };

    private com.amazonaws.http.HttpResponseHandler<AzHttpResponseContainer> httpHandler = new com.amazonaws.http.HttpResponseHandler<AzHttpResponseContainer>() {
        @Override
        public AzHttpResponseContainer handle(com.amazonaws.http.HttpResponse httpResponse) throws Exception {
            return new AzHttpResponseContainer(httpResponse);
        }

        @Override
        public boolean needsConnectionLeftOpen() {
            return false;
        }
    };

    private com.amazonaws.http.AmazonHttpClient amazonHttpClient;
    private SignerProvider signerProvider;
    AWSCredentialsProvider awsCredentialsProvider;
    private String serviceName;

    public CallAwsInterceptor(
            AmazonHttpClient amazonHttpClient,
            SignerProvider signerProvider,
            AWSCredentialsProvider awsCredentialsProvider,
            String serviceName
    ) {
        this.amazonHttpClient = amazonHttpClient;
        this.signerProvider = signerProvider;
        this.awsCredentialsProvider = awsCredentialsProvider;
        this.serviceName = serviceName;
    }

    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {
        okhttp3.Request okRequest = chain.request();

        com.amazonaws.Request<Void> azRequest = OkRequestToAz.okRequestToAz(okRequest, serviceName);

        ExecutionContext.Builder executionContextBuilder = ExecutionContext.builder();

        if (signerProvider != null) {
            executionContextBuilder.withSignerProvider(signerProvider);
        }

        ExecutionContext executionContext = executionContextBuilder.build();

        if (awsCredentialsProvider != null) {
            executionContext.setCredentialsProvider(awsCredentialsProvider);
        }

        AmazonHttpClient.RequestExecutionBuilder requestExecutionBuilder = amazonHttpClient.requestExecutionBuilder()
                .request(azRequest)
                .executionContext(executionContext)
                .errorResponseHandler(httpErrorHandler);

        try {
            com.amazonaws.Response<AzHttpResponseContainer> azResponse = requestExecutionBuilder.execute(httpHandler);
            return AzResponseToOk.azHttpResponseToOkResponse(azResponse.getAwsResponse(), okRequest);
        } catch (AzHttpResponseContainerWrapperException e) {
            return AzResponseToOk.azHttpResponseToOkResponse(e.getAzHttpResponseContainer(), okRequest);
        }
    }
}
