/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 * See LICENSE in the project root for license information.
 */
package com.microsoft.graph.connect;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.http.IHttpRequest;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;

public class GraphServiceClientManagerUnitTests implements IAuthenticationProvider {
    private String accessToken;
    private String clientId = "3d0d8ee3-e097-4039-82f3-8c11b64c8412";
    private String username = "zrinkam@mod182601.onmicrosoft.com";
    private String password = "pass@word1";

    private final String CONTENT_TYPE = "application/x-www-form-urlencoded";
    private final String GRANT_TYPE = "password";
    private final String TOKEN_ENDPOINT = "https://login.microsoftonline.com/common/oauth2/token";
    private final String RESOURCE = "https%3A%2F%2Fgraph.microsoft.com";
    private final String REQUEST_METHOD = "POST";

    private final String SUBJECT = "Email sent from unit test in android";
    private final String BODY = "<html><body>The body of the test email</body></html>";

    @Before
    public void getAccessTokenUsingPasswordGrant() throws IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException, JSONException {
        URL url = new URL(TOKEN_ENDPOINT);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        String urlParameters = String.format(
                "grant_type=%1$s&resource=%2$s&client_id=%3$s&username=%4$s&password=%5$s",
                GRANT_TYPE,
                RESOURCE,
                clientId,
                username,
                password
        );

        connection.setRequestMethod(REQUEST_METHOD);
        connection.setRequestProperty("Content-Type", CONTENT_TYPE);
        connection.setRequestProperty("Content-Length", String.valueOf(urlParameters.getBytes("UTF-8").length));

        connection.setDoOutput(true);
        DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
        dataOutputStream.writeBytes(urlParameters);
        dataOutputStream.flush();
        dataOutputStream.close();

        connection.getResponseCode();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        JsonParser jsonParser = new JsonParser();
        JsonObject grantResponse = (JsonObject)jsonParser.parse(response.toString());
        accessToken = grantResponse.get("access_token").getAsString();
    }

    @Test
    public void sendMail_messageSent() {
        GraphServiceController graphServiceController = new GraphServiceController(this);

        try {
            graphServiceController.sendMail(
                    username,
                    SUBJECT,
                    BODY,
                    new ICallback<Void>() {
                        @Override
                        public void success(Void aVoid) {
                            System.out.println();
                        }

                        @Override
                        public void failure(ClientException ex) {
                            throw ex;
                        }
                    }
            );
        } catch (ClientException ex) {
            throw ex;
        }
        while(true){}
    }

    @Override
    public void authenticateRequest(IHttpRequest request) {
        request.addHeader("Authorization", "Bearer " + accessToken);
    }
}
