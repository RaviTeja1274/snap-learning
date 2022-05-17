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
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws IOException, URISyntaxException {
        SwaggerParser swaggerParser = new SwaggerParser();
        swaggerParser.parse();
    }
}