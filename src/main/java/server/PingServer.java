package server;

import shared.NodeInfo;
import shared.NodesInfoContainer;
import tournament.GameResult;
import tournament.Tournament;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class PingServer implements Runnable {

    private ServerSocket server;
    private NodesInfoContainer nodesInfoContainer;
    private NodeInfo selfNode;
    private Tournament tournament;

    public PingServer(ServerSocket server, NodesInfoContainer nodesInfoContainer, NodeInfo selfNode) {
        this.server = server;
        this.nodesInfoContainer = nodesInfoContainer;
        this.selfNode = selfNode;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public void run() {
        System.out.println("Ping server is waiting for new connections");
        try {
            while (true) {
                Socket socket = server.accept();
                ObjectOutputStream connectionOutputStream = new ObjectOutputStream((socket.getOutputStream()));
                ObjectInputStream connectionInputStream = new ObjectInputStream(socket.getInputStream());

                // Receive nodes list from the client
                List<NodeInfo> receivedNetworkNodesList = (List<NodeInfo>) connectionInputStream.readObject();
                // Merge nodes list with current list
                nodesInfoContainer.setNetworkNodes(receivedNetworkNodesList);
                List<NodeInfo> mergedNetworkNodesList = nodesInfoContainer.getNetworkNodes();

                // Return updated information to the client
                connectionOutputStream.writeObject(mergedNetworkNodesList);

                // Get tournament state from the client
                Map<NodeInfo, List<GameResult>> newGamesMap = (Map<NodeInfo, List<GameResult>>) connectionInputStream.readObject();
                // Merge client and self tournaments
                this.tournament.mergeGamesMapWithNewMap(newGamesMap);
                this.tournament.removeInactivePlayersFromTournament(this.nodesInfoContainer);
                // Return updated tournament to the client
                connectionOutputStream.writeObject(this.tournament.getGamesMap());

                socket.close();
            }
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

}
