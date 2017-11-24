package com.mobileserver.mobileserverrobot;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;


public class Main {



    public final static void main(String[] args) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();

        JSONObject message = new JSONObject();
        message.put("led1State","false");
        message.put("led2State","false");

        try {
            HttpGet httpget = new HttpGet("https://mobile-server-backend.herokuapp.com/robotGet");
            HttpPost httppost = new HttpPost("https://mobile-server-backend.herokuapp.com/robotPost");

            System.out.println("Executing request " + httpget.getRequestLine());

            // Create a custom response handler
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                @Override
                public String handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };

            StringEntity entity = new StringEntity(message.toString());
            System.out.println("The JSON message that we're posting is "+message.toString());
            httppost.setEntity(entity);
            CloseableHttpResponse response = httpclient.execute(httppost);

            String responseBody = httpclient.execute(httpget, responseHandler);
            System.out.println("----------------------------------------");
            System.out.println("The JSON message that we're getting is "+responseBody);
            System.out.println("----------------------------------------");
            System.out.println(response);
        } finally {
            httpclient.close();
        }
    }
}
