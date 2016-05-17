package yazabara.log.events.client;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import yazabara.springxd.utils.HttpUtils;

import java.io.IOException;

/**
 * @author Yaroslav Zabara
 */
public class CorsHttpClient {

    private String url;

    public CorsHttpClient(String url) {
        this.url = url;
    }

    public void sendEventMessage(String json) {
        try {
            CloseableHttpClient httpclient = HttpUtils.buildHttpClient(null/*live connection seconds*/);
            HttpPost httppost = HttpUtils.buildHttpPost(json, url, null);
            httpclient.execute(httppost);
        } catch (IOException e) {
            throw new RuntimeException("Unable to send event to spring xd", e);
        }
    }

}
