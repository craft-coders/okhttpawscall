# OKHttpCallAWS interceptor

This project provides an okhttp3 interceptor that uses the AmazonHttpClient to make signed requests to AWS services.
Signing okhttp3 requests properly so that they are accepted by AWS turned out to be quite a hassle
[see here](https://github.com/esiqveland/okhttp-awssigner/issues/3).

Since we wanted to stick with retrofit we decided to create this adapter that uses the client from the aws java sdk.

## Getting the library

This library isn't published on maven central (yet).
However you can get the library with [JitPack](https://jitpack.io/).

1. Add the jitpack repo to your maven pom:

```
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

2. Add the dependency:

```
<dependency>
    <groupId>com.github.craft-coders</groupId>
    <artifactId>okhttpawscall</artifactId>
    <version>0.2.1</version>
</dependency>
```

## How to use it

```
OkHttpClient client = new OkHttpClient.Builder()
    .addInterceptor(CallAwsInterceptor.defaultAuthenticatedCallAwsInterceptor("execute-api"))
    .build();
```

Make sure that the CallAWSInterceptor is the last interceptor in your chain.
Because no interceptors after it (including all network interceptors) will get called.

## Limitations

This implementation breaks quite a bit of ok http functionality.
Everything related to networking will be useless and all network interceptors will not get called.
