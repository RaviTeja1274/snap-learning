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
import java.util.Optional;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {

        String base = "https://community.workday.com/sites/default/files/file-hosting/restapi/";


        HtmlParser htmlParser = new HtmlParser();
        Map<String, Map<String, String>> serviceToVersionSwaggers = htmlParser.parseHtmlFromUri(base);

        Scanner scanner = new Scanner(System.in);
        System.out.println("select service: " +serviceToVersionSwaggers.keySet());
        String service = scanner.nextLine();

        String version = "";

        if (!serviceToVersionSwaggers.containsKey(service)){
            throw new Exception(String.format("No service with name %s available", service));
        }

        Map<String, String> versionToSwaggerLink = serviceToVersionSwaggers.get(service);
        if (versionToSwaggerLink.size() > 1) {
            System.out.println("select version: " + versionToSwaggerLink.keySet());
            version = scanner.nextLine();
        } else {
            Collection<String> versions = versionToSwaggerLink.keySet();
            System.out.println("only one version " + versions + " available, using it by default");
            Optional<String> firstVersion = versions.stream().findFirst();
            if (firstVersion.isEmpty()){
                throw new Exception(String.format("No version available for service %s", service));
            }
            version = firstVersion.get();
        }

        if (!versionToSwaggerLink.containsKey(version)){
            throw new Exception(String.format("No version %s available for service %s ", version, service));
        }

        SwaggerParser swaggerParser = new SwaggerParser();
        swaggerParser.setService(service);
        swaggerParser.setVersion(version);
        swaggerParser.setToken("");
        swaggerParser.parse(base + versionToSwaggerLink.get(version));
//6dcb8106e8b74b5aabb1fc3ab8ef2b92
        scanner.close();
    }
}