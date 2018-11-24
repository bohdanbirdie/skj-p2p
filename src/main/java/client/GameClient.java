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
import java.util.List;

public class GameClient implements Runnable {
    private NodesInfoContainer nodesInfoContainer;
    private NodeInfo selfNode;
    private Tournament tournament;

    public GameClient(NodesInfoContainer nodesInfoContainer, NodeInfo selfNode) {
        this.nodesInfoContainer = nodesInfoContainer;
        this.selfNode = selfNode;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public void proceedGame(Socket nodeToCheckSocket, NodeInfo whoIPlayWith) throws IOException, ClassNotFoundException {

        nodesInfoContainer.updateNodeInfo(whoIPlayWith);

        ObjectOutputStream oout = new ObjectOutputStream((nodeToCheckSocket.getOutputStream()));
        ObjectInputStream ooin = new ObjectInputStream((nodeToCheckSocket.getInputStream()));

        Integer clientNumber = Utils.getRandomNumberInRange(1, 10);

        // Send client node and picked number
        oout.writeObject(clientNumber);
        oout.writeObject(selfNode);

        Integer receivedNumberFromServer = (Integer) ooin.readObject();

        // We always start counting from client
        Boolean isServerWinner = ((clientNumber + receivedNumberFromServer) % 2) == 0;

        System.out.println("I " + selfNode.getPingPort() + " played with " + whoIPlayWith.getPingPort() + " and " + (isServerWinner ? "won" : "lost"));
        oout.writeObject(isServerWinner);

        GameResult gameResult;
        if (isServerWinner) {
            gameResult = new GameResult(selfNode, whoIPlayWith, whoIPlayWith);
        } else {
            gameResult = new GameResult(selfNode, whoIPlayWith, selfNode);
        }

        tournament.saveGameResultByKey(selfNode, gameResult);


    }

    @Override
    public void run() {
        while (true) {
            long speed = (long) (Utils.getRandomNumberInRange(1000, 1200));
//            long speed = (long) (2000);
            try {
                Thread.sleep(speed);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            List<NodeInfo> allAvailableNodes = nodesInfoContainer.getAvailableNodesForGameOnly(selfNode);
//            ControlledLogger.log("I can connect to " + allAvailableNodes);
            List<NodeInfo> availableNodes = tournament.extractUnplayedNodes(allAvailableNodes, selfNode);
//            ControlledLogger.log("I can connect to availableNodes " + availableNodes);
            if (availableNodes.size() > 0 && selfNode.isActivePlayer()) {

//                long speed = (long) (Utils.getRandomNumberInRange(500, 700) * availableNodes.size() * 0.2);

                NodeInfo whoIPlayWith = availableNodes.get(0);
                try {
                    Socket nodeToCheckSocket = new Socket(whoIPlayWith.getIpAddess(), whoIPlayWith.getGamePort());

                    proceedGame(nodeToCheckSocket, whoIPlayWith);

                    nodeToCheckSocket.close();

                } catch (IOException e) {
                    ControlledLogger.log("failed to to connect to " + whoIPlayWith);
                    nodesInfoContainer.setNodeToDead(whoIPlayWith);

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
