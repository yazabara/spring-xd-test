package yazabara.springxd.utils;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * @author Yaroslav Zabara
 */
public class HttpUtils {

    public static RequestConfig buildConfig(final int socketTimeout) {
        RequestConfig defaultRequestConfig = RequestConfig.custom().build();
        return RequestConfig.copy(defaultRequestConfig)
                .setSocketTimeout(socketTimeout)
                .setConnectTimeout(socketTimeout)
                .setConnectionRequestTimeout(socketTimeout).build();
    }

    public static HttpPost buildHttpPost(String requestJson, String url, final Integer socketTimeout) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("charset", StandardCharsets.UTF_8.name());
        if (socketTimeout != null) {
            httpPost.setConfig(buildConfig(socketTimeout));
        }
        httpPost.setEntity(new StringEntity(requestJson, ContentType.APPLICATION_JSON));
        return httpPost;
    }

    public static CloseableHttpClient buildHttpClient(final Integer connectionLiveSeconds) {
        if (connectionLiveSeconds != null) {
            return HttpClientBuilder.create().setSSLHostnameVerifier(new NoopHostnameVerifier()).setConnectionTimeToLive(connectionLiveSeconds, TimeUnit.SECONDS).build();
        }
        return HttpClientBuilder.create().setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
    }

}
