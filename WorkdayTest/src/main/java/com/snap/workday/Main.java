package com.snap.workday;

import com.snap.workday.client.RecruitingV3;
import com.snap.workday.client.SwaggerParser;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException, URISyntaxException {

        String base = "https://community.workday.com/sites/default/files/file-hosting/restapi/";
        HtmlParser htmlParser = new HtmlParser();
        Map<String, Map<String, String>> stringMapMap = htmlParser.parseHtmlFromUri(base);

        Scanner scanner = new Scanner(System.in);
        System.out.println("select service: " +stringMapMap.keySet());
        String service = scanner.nextLine();

        String version = "";
        Map<String, String> versionLink = stringMapMap.get(service);
        if (versionLink.size() > 1) {
            System.out.println("select version: " + versionLink.keySet());
            version = scanner.nextLine();
        } else {
            Collection<String> versions = versionLink.keySet();
            System.out.println("only one version " + versions + " available, using it by default");
            version = versions.stream().findFirst().get();
        }

        SwaggerParser swaggerParser = new SwaggerParser();
        swaggerParser.setService(service);
        swaggerParser.setVersion(version);
        swaggerParser.setToken("");
        swaggerParser.parse(base + versionLink.get(version));
    }
}