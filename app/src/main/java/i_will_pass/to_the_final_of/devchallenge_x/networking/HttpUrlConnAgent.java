package i_will_pass.to_the_final_of.devchallenge_x.networking;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import i_will_pass.to_the_final_of.devchallenge_x.utils.L;

/**
 * gives ability to process network request without using libraries \
 */
public class HttpUrlConnAgent {

    private static final String CN = "HttpUrlConnAgent ` ";

    public String getStringFromWeb(String stringUrl) {
        String receivedString = null;
        HttpURLConnection urlConnection = null;
        try {
/*
            // simple check of what will happen if rotate display when this method runs \
            try {
                Thread.currentThread().sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
*/
            URL url = new URL(stringUrl);
            urlConnection = (HttpURLConnection) url.openConnection();

            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                receivedString = getStringFromInputStream(inputStream);
                // i had to transfer InputStream to String because of closing this stream in finally \
            } else L.e(CN + "response code = " + urlConnection.getResponseCode());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
            else L.a(CN + "urlConnection is null");
        }
        L.l(CN + "receivedString = " + receivedString);
        // we might send local broadcast here \
        return receivedString;
    }

    // converting InputStream to String - utility for getStringFromWeb(...) \
    private String getStringFromInputStream(InputStream inputStream) {

        BufferedReader bufferedReader = null;
        String oneLine;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while ((oneLine = bufferedReader.readLine()) != null) {
                stringBuilder.append(oneLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return stringBuilder.toString();
    }
}