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
import org.json.simple.parser.JSONParser;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;


public class Main {


    public final static void main(String[] args) throws Exception {

//        final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager ();
//        connectionManager.setMaxTotal ( 50 );
//
//        final CloseableHttpClient defaultHttpClient = HttpClientBuilder
//                .create ().setConnectionManager ( connectionManager )
//                .setDefaultRequestConfig ( config ).build ();

        JSONParser parser = new JSONParser();

        JSONObject getMessage = new JSONObject();

        JSONObject postMessage = new JSONObject();
        postMessage.put("led1State", "false");
        postMessage.put("led2State", "false");
        postMessage.put("robotId", "this_is_a_robot_id");
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
        HttpGet httpGet = new HttpGet("https://mobile-server-backend.herokuapp.com/robotGet");
        HttpPost httpPost = new HttpPost("https://mobile-server-backend.herokuapp.com/robotPost");

        //GPIO code from here
        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();

        // provision gpio pin #01 as an output pin and turn on
        final GpioPinDigitalOutput pin1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "MyLED1", PinState.LOW);
        final GpioPinDigitalOutput pin2 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "MyLED2", PinState.LOW);
        System.out.println("succesfully initialized GPIO");

        // set shutdown state for this pin
        pin1.setShutdownOptions(true, PinState.LOW);
        pin2.setShutdownOptions(true, PinState.LOW);

        //turn off pin 1
        pin1.high();
        pin2.high();


        //main loop, for loop because don't want to break pi
        for (int i = 0; i < 25; i++) {

            CloseableHttpClient httpclientPost = HttpClients.createDefault();
            CloseableHttpClient httpclientGet = HttpClients.createDefault();
            try {

                System.out.println("start of loop"+i);
                String responseBody = httpclientPost.execute(httpGet, responseHandler);
                getMessage = (JSONObject) parser.parse(responseBody);

                if (!(getMessage.get("led1State").equals(postMessage.get("led1State"))) || !(getMessage.get("led2State").equals(postMessage.get("led2State")))) {
                    if (getMessage.get("led1State").equals("true")) {
                        pin1.high();
                        postMessage.replace("led1State","true");
                    } else {
                        pin1.low();
                        postMessage.replace("led1State","false");
                    }

                    if (getMessage.get("led2State").equals("true")) {
                        pin2.high();
                        postMessage.replace("led2State","true");
                    } else {
                        pin2.low();
                        postMessage.replace("led2State","false");
                    }

                    System.out.println("LED1 State is " + getMessage.get("led1State"));
                    System.out.println("LED2 State is " + getMessage.get("led2State"));

                    StringEntity entity = new StringEntity(postMessage.toJSONString());
                    httpPost.setEntity(entity);
                    httpPost.setHeader("Accept", "application/json");
                    httpPost.setHeader("Content-type", "application/json");
                    CloseableHttpResponse response = httpclientPost.execute(httpPost);
                }

                Thread.sleep(2000);

                httpclientPost.close();
                httpclientGet.close();

            } finally {
                httpclientPost.close();
                httpclientGet.close();

            }
        }
        gpio.shutdown();
    }
}
