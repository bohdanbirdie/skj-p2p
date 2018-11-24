package server;

import shared.NodeInfo;
import shared.NodesInfoContainer;
import shared.Utils;
import tournament.GameResult;
import tournament.Tournament;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class GameServer implements Runnable {

    private ServerSocket server;
    private NodesInfoContainer nodesInfoContainer;
    private NodeInfo selfNode;
    private Tournament tournament;

    public GameServer(ServerSocket server, NodesInfoContainer nodesInfoContainer, NodeInfo selfNode) {
        this.server = server;
        this.nodesInfoContainer = nodesInfoContainer;
        this.selfNode = selfNode;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public void run() {

        System.out.println("waiting for new GAME connections");
        System.out.println("Game server port is: " + server.getLocalPort());

        try {
            while (true) {
                Socket socket = server.accept();

                ObjectOutputStream oout = new ObjectOutputStream((socket.getOutputStream()));
                ObjectInputStream ooi = new ObjectInputStream(socket.getInputStream());

                // Receive client node and picked number
                Integer receivedNumberFromClient = (Integer) ooi.readObject();
                NodeInfo whoIPlayWith = (NodeInfo) ooi.readObject();

                // Send server picked number
                Integer serverNumber = Utils.getRandomNumberInRange(1, 10);
                oout.writeObject(serverNumber);

                // Receive result of the game from client
                Boolean isServerWinner = (Boolean) ooi.readObject();
                GameResult gameResult;
                if (isServerWinner) {
                    gameResult = new GameResult(selfNode, whoIPlayWith, selfNode);
                } else {
                    gameResult = new GameResult(selfNode, whoIPlayWith, whoIPlayWith);
                }

                tournament.saveGameResultByKey(selfNode, gameResult);

                socket.close();
            }
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

}
