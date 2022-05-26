package com.snap.workday;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HtmlParser {

    public Map<String,Map<String,String>> parseHtmlFromUri(String uri) throws IOException {

        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(uri);
        String data = client.execute(httpGet, new BasicResponseHandler());
        client.close();


        Document parse = Jsoup.parse(data);
        Elements serviceNames = parse.select("h4");
        Elements aHrefs = parse.select("a");


        Map<String,Map<String,String>> serviceSwagger = new HashMap<>(serviceNames.size());

        for (Element name : serviceNames) {
            String serviceNameWithVersion = name.text();
            String[] nameAndVersion = serviceNameWithVersion.split(" ");
            String serviceName = nameAndVersion[1].trim();
            String version = nameAndVersion[nameAndVersion.length - 1].trim();
            String link = "";

            for (Element aHref : aHrefs) {
                String href = aHref
                    .getElementsByAttributeValueEnding("href", ".json")
                    .attr("href");
                if (href.contains(serviceName) && href.contains(version)) {
                    link = href;
                    break;
                }
            }

            if (!link.isEmpty()) {
                Map<String, String> versionLink = new HashMap<>(1);
                if (serviceSwagger.containsKey(serviceName)){
                    versionLink = serviceSwagger.get(serviceName);
                }
                versionLink.put(version, link);
                serviceSwagger.put(serviceName, versionLink);
            }
        }
        return serviceSwagger;
    }

}
