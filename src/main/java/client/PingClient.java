package client;

import shared.ControlledLogger;
import shared.NodeInfo;
import shared.NodesInfoContainer;
import shared.Utils;
import tournament.GameResult;
import tournament.Tournament;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PingClient implements Runnable {
    private NodesInfoContainer nodesInfoContainer;
    private NodeInfo selfNode;
    private Tournament tournament;
    private List<NodeInfo> whoIRemoved = new ArrayList<>();

    public PingClient(NodesInfoContainer nodesInfoContainer, NodeInfo selfNode) {
        this.nodesInfoContainer = nodesInfoContainer;
        this.selfNode = selfNode;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    private void printWhoIRemoved() {
        ControlledLogger.log("I Removed: ");
        this.whoIRemoved.forEach(nodeInfo -> ControlledLogger.log(String.valueOf(nodeInfo.getPingPort())));
    }

    private void notifyNnodes(int n, NodeInfo selfNode) {
        for (int i = 0; i <= n; i++) {
            List<NodeInfo> availableNodes = nodesInfoContainer.getAvailableNodes(selfNode);
            if (i <= availableNodes.size()) {
                try {
                    NodeInfo nodeToCheck = availableNodes.get(i);
                    Socket nodeToCheckSocket = new Socket(nodeToCheck.getIpAddess(), nodeToCheck.getPingPort());
                    nodesInfoContainer.updateNodeInfo(nodeToCheck);

                    ObjectOutputStream oout = new ObjectOutputStream((nodeToCheckSocket.getOutputStream()));
                    ObjectInputStream ooin = new ObjectInputStream((nodeToCheckSocket.getInputStream()));

                    oout.writeObject(nodesInfoContainer.getNetworkNodes());

                    List<NodeInfo> updatedNetworkNodes = (List<NodeInfo>) ooin.readObject();
                    updatedNetworkNodes.sort(Comparator.comparing(NodeInfo::getWasAliveTimestamp));
//                    System.out.println("Updated node");

//                    ControlledLogger.log("I know about: ");
//                    updatedNetworkNodes.forEach(node -> ControlledLogger.log(node.getPingPort() + "  " + (node.isDead() ? "dead" : "alive")));

                    nodesInfoContainer.setNetworkNodes(updatedNetworkNodes);

                    exchangeTournamentState(oout, ooin, nodeToCheck);
                    nodeToCheckSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public void exchangeTournamentState(ObjectOutputStream oout, ObjectInputStream ooin, NodeInfo nodeToCheck) {
        try {
            oout.writeObject(this.tournament.getGamesMap());
            Map<NodeInfo, List<GameResult>> updatedGamesMap = (Map<NodeInfo, List<GameResult>>) ooin.readObject();
            this.tournament.mergeGamesMapWithNewMap(updatedGamesMap);
        } catch (IOException e) {
            nodesInfoContainer.setNodeToDead(nodeToCheck);
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void proceedCommunation(Socket nodeToCheckSocket, NodeInfo nodeToCheck) throws IOException, ClassNotFoundException {

        nodesInfoContainer.updateNodeInfo(nodeToCheck);

        ObjectOutputStream oout = new ObjectOutputStream((nodeToCheckSocket.getOutputStream()));
        ObjectInputStream ooin = new ObjectInputStream((nodeToCheckSocket.getInputStream()));

        oout.writeObject(nodesInfoContainer.getNetworkNodes());

        List<NodeInfo> updatedNetworkNodes = (List<NodeInfo>) ooin.readObject();

        exchangeTournamentState(oout, ooin, nodeToCheck);
        ControlledLogger.log("I know about: ");
        updatedNetworkNodes.forEach(node ->
                ControlledLogger.log(node.getPingPort() + "  " +
                        (node.isDead() ? "dead - " : "alive - ") +
                        (node.isActivePlayer() ? "active - " : "n/active - ") +
                        " plated w/: " + tournament.checkIfIPlayedWith(node))
            );

        nodesInfoContainer.setNetworkNodes(updatedNetworkNodes);
    }


    public void run() {
        while (true) {
            List<NodeInfo> availableNodes = nodesInfoContainer.getAvailableNodes(selfNode);
            long speed = (long) (Utils.getRandomNumberInRange(500, 700) * availableNodes.size() * 0.2);
//            long speed = 1500;
            try {
                Thread.sleep(speed);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            availableNodes = nodesInfoContainer.getAvailableNodes(selfNode);
            if (availableNodes.size() > 0) {
//                NodeInfo nodeToCheck = availableNodes.get(Utils.getRandomNumberInRange(0, availableNodes.size() - 1));
                NodeInfo nodeToCheck = availableNodes.get(0);
                try {
                    Socket nodeToCheckSocket = new Socket(nodeToCheck.getIpAddess(), nodeToCheck.getPingPort());

//                    System.out.print("\033[H\033[2J");
                    printWhoIRemoved();
                    ControlledLogger.log("My speed is " + speed + "ms");
                    ControlledLogger.log("=======================");
                    ControlledLogger.log("I'm " + selfNode.getPingPort() + " checking the " + nodeToCheck.getPingPort());
                    ControlledLogger.log("My status " + (selfNode.isActivePlayer() ? "active" : "n/active"));
                    ControlledLogger.log("=======================\n");

                    proceedCommunation(nodeToCheckSocket, nodeToCheck);

//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                    nodeToCheckSocket.close();

                } catch (IOException e) {
                    ControlledLogger.log("failed to to connect to " + nodeToCheck);
                    nodesInfoContainer.setNodeToDead(nodeToCheck);
                    this.whoIRemoved.add(nodeToCheck);

//                    ////////////////////
//                    // Recall later
                    int n = availableNodes.size() / 3;
                    System.out.println("TRY " + n);
                    notifyNnodes(n, selfNode);
//                    ////////////////////

//                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
