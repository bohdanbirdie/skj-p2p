package server;

import shared.GameSynchronizer;
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
import java.util.Map;

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
        try {
            while (true) {
                Socket socket = server.accept();

                ObjectOutputStream oout = new ObjectOutputStream((socket.getOutputStream()));
                ObjectInputStream ooi = new ObjectInputStream(socket.getInputStream());

                // Receive client node and picked number
                String clientEncryptedNumber = (String) ooi.readObject();
                NodeInfo whoIPlayWith = (NodeInfo) ooi.readObject();

                if (tournament.checkIfIPlayedWith(whoIPlayWith) != "not yet" && GameSynchronizer.doIPlayWith(whoIPlayWith)) {
                    System.out.println("DUPLICATE GAME");
                    String response = "DECLINE";
                    oout.writeObject(response);
                } else {
                    String response = "ACCEPT";
                    oout.writeObject(response);

                    // Send server picked number
                    Integer serverNumber = Utils.getRandomNumberInRange(1, 10);
                    Integer randomKey = Utils.getRandomNumberInRange(0, Utils.chars.length);

                    String encryptedNumber = Utils.encrypt(String.valueOf(serverNumber), randomKey);

                    oout.writeObject(encryptedNumber);
                    Integer clientRandomKey = (Integer) ooi.readObject();
                    oout.writeObject(randomKey);

                    // Receive result of the game from client
                    Boolean isServerWinner = (Boolean) ooi.readObject();
                    GameResult gameResult;
                    if (isServerWinner) {
                        gameResult = new GameResult(selfNode, whoIPlayWith, selfNode);
                    } else {
                        gameResult = new GameResult(selfNode, whoIPlayWith, whoIPlayWith);
                    }

                    tournament.saveGameResultsForNodes(selfNode, whoIPlayWith, gameResult);
                    oout.writeObject(tournament.getGamesMap());
                    Map<NodeInfo, List<GameResult>> updatedGamesMap = (Map<NodeInfo, List<GameResult>>) ooi.readObject();
                    tournament.mergeGamesMapWithNewMap(updatedGamesMap);
                    tournament.saveGameResultsForNodes(selfNode, whoIPlayWith, gameResult);
                    this.tournament.removeInactivePlayersFromTournament(this.nodesInfoContainer);
                }
                socket.close();
            }
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

}
