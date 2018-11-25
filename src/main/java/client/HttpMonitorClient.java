package client;

import shared.ControlledLogger;
import shared.Utils;
import tournament.Tournament;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpMonitorClient implements Runnable {
    Tournament tournament;

    public HttpMonitorClient(Tournament tournament) {
        this.tournament = tournament;
    }

    private void sendPost() throws Exception {
        String url = "http://localhost:8080/monitor";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("POST");
        con.setDoOutput(true);

        ObjectOutputStream connectionOutputStream = new ObjectOutputStream(con.getOutputStream());
        connectionOutputStream.writeObject(tournament.getGamesMap());
        connectionOutputStream.close();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(Utils.getRandomNumberInRange(900, 1300));
                sendPost();
            } catch (InterruptedException e) {
            } catch (Exception e) {
                ControlledLogger.log("Unable to connect to monitor");
            }
        }
    }
}
