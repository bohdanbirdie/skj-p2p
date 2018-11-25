package monitor;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import shared.NodeInfo;
import tournament.GameResult;
import tournament.Tournament;

public class HttpMonitor {
    static Tournament tournament = new Tournament(new NodeInfo("localhost", 0, 0));

    public static void main(String[] args) throws Exception {
        InetAddress address = InetAddress.getByName("127.0.0.1");

        HttpServer server = HttpServer.create(new InetSocketAddress(address, 8080), 0);
        server.createContext("/monitor", new MyHandler());
        server.setExecutor(null);
        server.start();
    }

    static class MyHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            try {

                ObjectInputStream connectionInputStream = new ObjectInputStream(t.getRequestBody());
                Map<NodeInfo, List<GameResult>> updatedGamesMap = null;
                try {
                    updatedGamesMap = (Map<NodeInfo, List<GameResult>>) connectionInputStream.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                System.out.println("\033[H\033[2J");
                tournament.mergeGamesMapWithNewMap(updatedGamesMap);
                tournament.printTournamentState();

            } catch (final Exception e) {
                System.out.println("404");
                t.sendResponseHeaders(404, 0);
            } finally {
                String response = "OK";
                t.sendResponseHeaders(200, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
                t.close();
            }
        }
    }

}