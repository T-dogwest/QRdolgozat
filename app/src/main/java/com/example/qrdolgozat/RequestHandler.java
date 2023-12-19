package com.example.qrdolgozat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RequestHandler {
    private RequestHandler() {}

    // Backend csatlakozásért felelős metódus
    private static HttpURLConnection setUpConnection(String url) throws IOException {
        URL linkObject = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) linkObject.openConnection();
        connection.setRequestProperty("Accept", "application/json");
        connection.setReadTimeout(1000);
        connection.setConnectTimeout(1000);
        return connection;
    }

    // response elérése
    private static com.example.qrdolgozat.Response getResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        InputStream inputStream;
        if (responseCode < 400) {
            inputStream = connection.getInputStream();
        } else {
            inputStream = connection.getErrorStream();
        }
        StringBuilder builder = new StringBuilder();
        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(inputStream));
        String sor = bufferReader.readLine();
        while (sor != null) {
            builder.append(sor);
            sor = bufferReader.readLine();
        }
        bufferReader.close();
        inputStream.close();
        return new com.example.qrdolgozat.Response(responseCode, builder.toString());
    }

    // Request body hozzáadása
    private static void addRequestBody(HttpURLConnection connection, String data) throws IOException {
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        OutputStream outputStream = connection.getOutputStream();
        BufferedWriter Writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        Writer.write(data);
        Writer.flush();
        Writer.close();
        outputStream.close();
    }

    // GET request
    public static com.example.qrdolgozat.Response get(String url) throws IOException {
        HttpURLConnection connection = setUpConnection(url);
       // connection.setRequestMethod("GET");
        return getResponse(connection);
    }
    // PUT request
    public static com.example.qrdolgozat.Response put(String url, String data) throws IOException {
        HttpURLConnection connection = setUpConnection(url);
        connection.setRequestMethod("PUT");
        addRequestBody(connection, data);
        return getResponse(connection);
    }
}
