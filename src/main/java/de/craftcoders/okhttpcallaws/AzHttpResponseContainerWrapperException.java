package de.craftcoders.okhttpcallaws;

import com.amazonaws.AmazonClientException;

/**
 * Exception that stores the amazon http response.
 *
 * We need this because we need to throw an exception when we hit a status code !~ 200.
 */
public class AzHttpResponseContainerWrapperException extends AmazonClientException {
    private AzHttpResponseContainer azHttpResponseContainer;

    public AzHttpResponseContainerWrapperException(AzHttpResponseContainer azHttpResponseContainer) {
        super("");
        this.azHttpResponseContainer = azHttpResponseContainer;
    }

    public AzHttpResponseContainer getAzHttpResponseContainer() {
        return azHttpResponseContainer;
    }
}
