import client.PingClient;
import server.PingServer;
import shared.NodeInfo;
import shared.NodesInfoContainer;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    private static NodesInfoContainer nodesInfoContainer = new NodesInfoContainer();
    private static NodeInfo selfNode;
    public static void main(String[] args) throws IOException {
        PingServer pingServer;

        if (args.length == 3) {
            NodeInfo connectTo = new NodeInfo(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            nodesInfoContainer.setNetworkNodes(Arrays.asList(connectTo));
            pingServer = new PingServer(createServerSocket(), nodesInfoContainer, selfNode);
        } else {
            pingServer = new PingServer(createServerSocket(3030), nodesInfoContainer, selfNode);
        }

        Thread pingServerThread = new Thread(pingServer);
        pingServerThread.start();

        PingClient pingClient = new PingClient(nodesInfoContainer, selfNode);
        Thread clientPingThread = new Thread(pingClient);
        clientPingThread.start();
    }

    public static ServerSocket createServerSocket(int currentPort) {
        ServerSocket server = null;
        try {
            if (currentPort > 0) {
                server = new ServerSocket(currentPort);
            } else {
                server = new ServerSocket(0);
                currentPort = server.getLocalPort();
            }

            selfNode = createSelfNodeInfo(currentPort);
            saveSelfNodeInfo(selfNode);
            System.out.println("My port: " + currentPort);
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

    private static NodeInfo createSelfNodeInfo(int currentPort) {
        return new NodeInfo("localhost", 3031, currentPort);
    }

}
