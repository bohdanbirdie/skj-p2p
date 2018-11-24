package tournament;

import shared.NodeInfo;
import shared.NodesInfoContainer;

import java.util.*;
import java.util.stream.Collectors;

public class Tournament {
    private Map<NodeInfo, List<GameResult>> gamesMap;
    private NodeInfo selfNode;

    public Tournament(NodeInfo nodeInfo) {
        this.selfNode = nodeInfo;
        this.gamesMap = new HashMap<>();
        this.gamesMap.put(nodeInfo, new ArrayList<>());
    }

    public synchronized void setSelfActiveStatus(boolean status) {
        selfNode.setActivePlayer(status);
    }

    public synchronized NodeInfo getSelfNode() {
        return selfNode;
    }

    public synchronized void saveGameResultsForNodes(NodeInfo self, NodeInfo opponent, GameResult gameResult) {
        List<GameResult> selfGames = this.gamesMap.get(self);
        List<GameResult> opponentGames = this.gamesMap.get(opponent);

        boolean keyExist = selfGames != null;

        if (keyExist) {
            selfGames.add(gameResult);
        } else {
            this.gamesMap.put(self, new ArrayList<>(Collections.singletonList(gameResult)));
        }

        keyExist = opponentGames != null;
        if (keyExist) {
            opponentGames.add(gameResult);
        } else {
            this.gamesMap.put(opponent, new ArrayList<>(Collections.singletonList(gameResult)));
        }
    }


    public synchronized Map<NodeInfo, List<GameResult>> getGamesMap() {
        return gamesMap;
    }

    public synchronized List<NodeInfo> extractUnplayedNodes(List<NodeInfo> availableNodes, NodeInfo selfNode) {
        if (availableNodes == null) {
            availableNodes = new ArrayList<>();
        }

        List<GameResult> gamesPlayedByMe = this.gamesMap.get(selfNode);
        if (gamesPlayedByMe == null) {
            return availableNodes;
        }
        List<NodeInfo> whoIPlayedWith = gamesPlayedByMe
                .stream()
                .map(GameResult::getOpponent)
                .collect(Collectors.toList());

        return availableNodes
                .stream()
                .filter(node -> !whoIPlayedWith.contains(node))
                .collect(Collectors.toList());
    }

    public synchronized String checkIfIPlayedWith(NodeInfo nodeInfo) {
        List<GameResult> gamesPlayedByMe = this.gamesMap.get(selfNode);

        if (gamesPlayedByMe == null) return "not yet";
        List<GameResult> gamesPlayedByMeWithOther = gamesPlayedByMe
                .stream()
                .filter(gameResult -> nodeInfo.equals(gameResult.getOpponent()))
                .collect(Collectors.toList());

        if (gamesPlayedByMeWithOther.size() > 0) {
            return gamesPlayedByMeWithOther.get(0).checkIfWinner(selfNode) ? " I won " : "I lost";
        }
        return "not yet";
    }

    public synchronized void mergeGamesMapWithNewMap(Map<NodeInfo, List<GameResult>> newMap) {
        Map<NodeInfo, List<GameResult>> myConcurrentMap = new HashMap<>();

        newMap.forEach((key, value) -> {
            boolean keyExist = this.gamesMap.get(key) != null;
            if (keyExist) {
                List<GameResult> duplicatesList = new ArrayList<>(this.gamesMap.get(key));
                duplicatesList.addAll(newMap.get(key));
                duplicatesList.sort(Comparator.comparing(GameResult::getPlayedTimestamp).reversed());
                Set<GameResult> setWithoutDuplicates = new HashSet<>(duplicatesList);
                myConcurrentMap.put(key, new ArrayList<>(setWithoutDuplicates));
            } else {
                List<GameResult> duplicatesList = new ArrayList<>(newMap.get(key));
                duplicatesList.sort(Comparator.comparing(GameResult::getPlayedTimestamp).reversed());
                Set<GameResult> setWithoutDuplicates = new HashSet<>(duplicatesList);
                myConcurrentMap.put(key, new ArrayList<>(setWithoutDuplicates));
            }
        });

        this.gamesMap = myConcurrentMap;
    }

    public boolean checkIfSelfPlayedWithEveryone(NodesInfoContainer nodesInfoContainer) {
        List<NodeInfo> availableActiveAndNotDealPlayers = nodesInfoContainer.getNetworkNodes()
                .stream()
                .filter(nodeInfo -> nodeInfo.isActivePlayer() && !nodeInfo.isDead() && !nodeInfo.equals(selfNode))
                .collect(Collectors.toList());

        List<GameResult> selfPlayedGames = this.gamesMap.get(selfNode);
        if (selfPlayedGames == null) return true;
        List<NodeInfo> selfPlayedWith = selfPlayedGames
                .stream()
                .map(GameResult::getOpponent)
                .filter(nodeInfo -> !nodeInfo.equals(selfNode))
                .collect(Collectors.toList());

        System.out.println(selfPlayedWith.size() + " " + availableActiveAndNotDealPlayers.size());

        return selfPlayedWith.size() >= availableActiveAndNotDealPlayers.size();
    }
}
