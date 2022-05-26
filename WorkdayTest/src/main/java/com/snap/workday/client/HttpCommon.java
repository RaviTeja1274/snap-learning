package com.snap.workday.client;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

public class HttpCommon {

    /*
       1. %s = service
       2. %s = version
       3. %s = end point resource
     */
    private static final String url = "https://wd2-impl-services1.workday.com/api/%s/%s/snaplogic_pt2/%s";
    private final CloseableHttpClient aDefault;
    private final String token;
    private final String uriToCall;

    private final Header header;

    public HttpCommon(String token, String service, String version, String endPointResource) {
        this.token = token;
        uriToCall = String.format(url, service, version, endPointResource);
        header = makeHeader();
        aDefault = HttpClients.createDefault();
    }

    private Header makeHeader() {
       return new BasicHeader("Authorization",
            String.format("Bearer %s", this.token));
    }

    public Map<Object, Object> makeGetCall(Map<String,String> queryParams)
        throws IOException {

        HttpGet httpGet = new HttpGet(uriToCall);
        httpGet.addHeader(header);
        httpGet.setHeader("Accept", "application/json");

        String content = aDefault.execute(httpGet, new BasicResponseHandler());

        System.out.println(content);
        aDefault.close();
        return new HashMap<>();
    }

    public Map<Object,Object> makePostCall(String entity) throws IOException {

        HttpPost httpPost = new HttpPost(uriToCall);
        httpPost.addHeader(header);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Accept", "application/json");


        httpPost.setEntity(new StringEntity(entity));

        String content = aDefault.execute(httpPost, new BasicResponseHandler());

        System.out.println(content);
        aDefault.close();
        return new HashMap<>();
    }
}
