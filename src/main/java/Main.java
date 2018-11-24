import client.GameClient;
import client.PingClient;
import com.sun.tools.hat.internal.parser.Reader;
import reader.UserInputHandler;
import server.GameServer;
import server.PingServer;
import shared.NodeInfo;
import shared.NodesInfoContainer;
import tournament.Tournament;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Main {
    private static NodesInfoContainer nodesInfoContainer = new NodesInfoContainer();
    private static NodeInfo selfNode;
    private static Tournament tournament;
    public static void main(String[] args) throws IOException {
        ServerSocket pingServerSocket;
        PingServer pingServer;
        PingClient pingClient;

        ServerSocket gameServerSocket;
        GameServer gameServer;
        GameClient gameClient;


        if (args.length == 3) {
            NodeInfo connectTo = new NodeInfo(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            nodesInfoContainer.setNetworkNodes(Arrays.asList(connectTo));
            pingServerSocket = createServerSocket();
            gameServerSocket = createServerSocket();

            int currentPingPort = pingServerSocket.getLocalPort();
            int currentGamePort = gameServerSocket.getLocalPort();

            selfNode = createSelfNodeInfo(currentPingPort, currentGamePort);
        } else {
            pingServerSocket = createServerSocket(3030);
            gameServerSocket = createServerSocket(3031);

            int currentPingPort = pingServerSocket.getLocalPort();
            int currentGamePort = gameServerSocket.getLocalPort();

            selfNode = createSelfNodeInfo(currentPingPort, currentGamePort);
        }

        saveSelfNodeInfo(selfNode);
        nodesInfoContainer.setSelfNode(selfNode);
        tournament = new Tournament(selfNode);

        pingServer = new PingServer(pingServerSocket, nodesInfoContainer, selfNode);
        Thread pingServerThread = new Thread(pingServer);

        pingClient = new PingClient(nodesInfoContainer, selfNode);
        Thread clientPingThread = new Thread(pingClient);

        gameServer = new GameServer(gameServerSocket, nodesInfoContainer, selfNode);
        Thread gameServerThread = new Thread(gameServer);

        gameClient = new GameClient(nodesInfoContainer, selfNode);
        Thread clientGameThread = new Thread(gameClient);

        UserInputHandler reader = new UserInputHandler(nodesInfoContainer);
        Thread readerThread = new Thread(reader);

        pingServer.setTournament(tournament);
        pingClient.setTournament(tournament);
        gameServer.setTournament(tournament);
        gameClient.setTournament(tournament);
        reader.setTournament(tournament);

        pingServerThread.start();
        clientPingThread.start();
        gameServerThread.start();
        clientGameThread.start();
        readerThread.start();
    }

    public static ServerSocket createServerSocket(int currentPort) {
        ServerSocket server = null;
        try {
            if (currentPort > 0) {
                server = new ServerSocket(currentPort);
            } else {
                server = new ServerSocket(0);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return server;
    }

    public static ServerSocket createServerSocket() {
        return createServerSocket(0);
    }

    private static void saveSelfNodeInfo(NodeInfo selfNode) {
        nodesInfoContainer.setNetworkNodes(new ArrayList<>(
                Arrays.asList(selfNode)
        ));
    }

    private static NodeInfo createSelfNodeInfo(int currentPingPort, int currentGamePort) {
        System.out.println("My ping port is " + currentPingPort + ", game port " + currentGamePort);
        return new NodeInfo("localhost", currentGamePort, currentPingPort);
    }

}
