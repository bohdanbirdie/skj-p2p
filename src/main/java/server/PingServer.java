package server;

import shared.NodeInfo;
import shared.NodesInfoContainer;
import shared.Utils;
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

        System.out.println("waiting for new connections");

        try {
            while (true) {
                // Блокируется до возникновения нового соединения:
                Socket socket = server.accept();


                ObjectOutputStream oout = new ObjectOutputStream((socket.getOutputStream()));
                ObjectInputStream ooi = new ObjectInputStream(socket.getInputStream());

//                System.out.println("Read first");
                List<NodeInfo> receivedNetworkNodesList = (List<NodeInfo>) ooi.readObject();
//                List<NodeInfo> merged = Utils.mergerUniqueLatest(receivedNetworkNodesList, nodesInfoContainer.getNetworkNodes());
                nodesInfoContainer.setNetworkNodes(receivedNetworkNodesList);
                List<NodeInfo> merged = nodesInfoContainer.getNetworkNodes();
//                detectMessageType(merged);

                oout.writeObject(merged);
                Map<NodeInfo, List<GameResult>> newGamesMap = (Map<NodeInfo, List<GameResult>>) ooi.readObject();
//                System.out.println(this.tournament.getGamesMap());
                this.tournament.mergeGamesMapWithNewMap(newGamesMap);
                oout.writeObject(this.tournament.getGamesMap());

                socket.close();
            }
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    public void detectMessageType(Object object) {
        if (object instanceof String) {
            System.out.println(object);
        }

        if (object instanceof List) {
            if (((List) object).size() > 0 && ((List) object).get(0) instanceof NodeInfo) {
                System.out.println(object);
            }
        }
    }
}
