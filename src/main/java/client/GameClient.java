package client;

import shared.*;
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

        GameSynchronizer.setCurrentPlayerWhoIPlayWith(whoIPlayWith);
        nodesInfoContainer.updateNodeInfo(whoIPlayWith);

        ObjectOutputStream socketOutputStream = new ObjectOutputStream((nodeToCheckSocket.getOutputStream()));
        ObjectInputStream socketInputStream = new ObjectInputStream((nodeToCheckSocket.getInputStream()));

        Integer clientNumber = Utils.getRandomNumberInRange(1, 10);

        // Send client node and picked number
        socketOutputStream.writeObject(clientNumber);
        socketOutputStream.writeObject(selfNode);

        String response = (String) socketInputStream.readObject();
        if (response.equals("ACCEPT")) {

            Integer receivedNumberFromServer = (Integer) socketInputStream.readObject();

            // We always start counting from client
            Boolean isServerWinner = ((clientNumber + receivedNumberFromServer) % 2) == 0;

            System.out.println("I " + selfNode.getPingPort() + " played with " + whoIPlayWith.getPingPort() + " and " + (isServerWinner ? "won" : "lost"));
            socketOutputStream.writeObject(isServerWinner);

            GameResult gameResult;
            if (isServerWinner) {
                gameResult = new GameResult(selfNode, whoIPlayWith, whoIPlayWith);
            } else {
                gameResult = new GameResult(selfNode, whoIPlayWith, selfNode);
            }

            tournament.saveGameResultsForNodes(selfNode, whoIPlayWith, gameResult);
            nodeToCheckSocket.close();
        } else {
            nodeToCheckSocket.close();
            GameSynchronizer.setCurrentPlayerWhoIPlayWith(null);
        }
    }

    @Override
    public void run() {
        while (true) {
            long speed = (long) (Utils.getRandomNumberInRange(1000, 1200));
            try {
                Thread.sleep(speed);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            List<NodeInfo> availableNodesForGame = nodesInfoContainer.getAvailableNodesForGameOnly(selfNode);
            List<NodeInfo> availableUnplayedWith = tournament.extractUnplayedNodes(availableNodesForGame, selfNode);
            if (availableUnplayedWith.size() > 0 && selfNode.isActivePlayer()) {
                NodeInfo whoIPlayWith = availableUnplayedWith.get(0);
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
