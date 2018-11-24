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
        // Print list of nodes that current node check and marked as dead
        // Used to display that only few nodes will try to connect to dead nodes before being
        // notified that node is dead by other nodes in the network
        ControlledLogger.log("I Removed: ");
        this.whoIRemoved.forEach(nodeInfo -> ControlledLogger.log(String.valueOf(nodeInfo.getPingPort())));
    }

    private void notifyNeighboursAboutDeadNode(int notifyAmount, NodeInfo selfNode) {
        if (notifyAmount != 0) {
            for (int i = 0; i <= notifyAmount; i++) {
                List<NodeInfo> availableNodes = nodesInfoContainer.getAvailableNodes(selfNode);
                if (i < availableNodes.size()) {
                    try {
                        NodeInfo nodeToCheck = availableNodes.get(i);
                        Socket nodeToCheckSocket = new Socket(nodeToCheck.getIpAddess(), nodeToCheck.getPingPort());
                        nodesInfoContainer.updateNodeInfo(nodeToCheck);

                        ObjectOutputStream connectionOutputStream = new ObjectOutputStream((nodeToCheckSocket.getOutputStream()));
                        ObjectInputStream connectionInputStream = new ObjectInputStream((nodeToCheckSocket.getInputStream()));

                        connectionOutputStream.writeObject(nodesInfoContainer.getNetworkNodes());

                        List<NodeInfo> updatedNetworkNodes = (List<NodeInfo>) connectionInputStream.readObject();
                        updatedNetworkNodes.sort(Comparator.comparing(NodeInfo::getWasAliveTimestamp));

                        nodesInfoContainer.setNetworkNodes(updatedNetworkNodes);

                        exchangeTournamentState(connectionOutputStream, connectionInputStream, nodeToCheck);
                        nodeToCheckSocket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            ControlledLogger.log("No more nodes to connect");
        }
    }

    private void exchangeTournamentState(ObjectOutputStream connectionOutputStream, ObjectInputStream connectionInputStream, NodeInfo nodeToCheck) {
        try {
            connectionOutputStream.writeObject(this.tournament.getGamesMap());
            Map<NodeInfo, List<GameResult>> updatedGamesMap = (Map<NodeInfo, List<GameResult>>) connectionInputStream.readObject();
            this.tournament.mergeGamesMapWithNewMap(updatedGamesMap);
        } catch (IOException e) {
            nodesInfoContainer.setNodeToDead(nodeToCheck);
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void proceedCommunication(Socket nodeToCheckSocket, NodeInfo nodeToCheck) throws IOException, ClassNotFoundException {

        nodesInfoContainer.updateNodeInfo(nodeToCheck);

        ObjectOutputStream connectionOutputStream = new ObjectOutputStream((nodeToCheckSocket.getOutputStream()));
        ObjectInputStream connectionInputStream = new ObjectInputStream((nodeToCheckSocket.getInputStream()));

        connectionOutputStream.writeObject(nodesInfoContainer.getNetworkNodes());

        List<NodeInfo> updatedNetworkNodes = (List<NodeInfo>) connectionInputStream.readObject();

        exchangeTournamentState(connectionOutputStream, connectionInputStream, nodeToCheck);

        // Display nodes that we aware of
        ControlledLogger.log("I know about: ");
        updatedNetworkNodes
                .stream()
                .filter(nodeInfo -> !nodeInfo.equals(selfNode))
                .sorted(Comparator.comparingInt(NodeInfo::getPingPort))
                .forEach(node ->
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
            // Little bit of randomness in the application
            long speed = (long) (Utils.getRandomNumberInRange(500, 700) * availableNodes.size() * 0.2);
            try {
                Thread.sleep(speed);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            availableNodes = nodesInfoContainer.getAvailableNodes(selfNode);
            if (availableNodes.size() > 0) {
                NodeInfo nodeToCheck = availableNodes.get(0);

                try {
                    Socket nodeToCheckSocket = new Socket(nodeToCheck.getIpAddess(), nodeToCheck.getPingPort());

                    ControlledLogger.log("\033[H\033[2J");
                    printWhoIRemoved();
                    ControlledLogger.log("My speed is " + speed + "ms");
                    ControlledLogger.log("=======================");
                    ControlledLogger.log("I'm " + selfNode.getPingPort() + " checking the " + nodeToCheck.getPingPort());
                    ControlledLogger.log("My status " + (selfNode.isActivePlayer() ? "active" : "n/active"));
                    ControlledLogger.log("=======================\n");

                    proceedCommunication(nodeToCheckSocket, nodeToCheck);

                    nodeToCheckSocket.close();

                } catch (IOException e) {
                    ControlledLogger.log("failed to to connect to " + nodeToCheck);
                    nodesInfoContainer.setNodeToDead(nodeToCheck);
                    this.whoIRemoved.add(nodeToCheck);

                    int n = availableNodes.size() / 3;
                    notifyNeighboursAboutDeadNode(n, selfNode);

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
