package tournament;

import shared.NodeInfo;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Tournament {
    private Map<NodeInfo, List<GameResult>> gamesMap;
    private NodeInfo selfNode;

    public Tournament(NodeInfo nodeInfo) {
        this.selfNode = nodeInfo;
//        System.out.println("Creating empty tournament");
        this.gamesMap = new HashMap<>();
        if (nodeInfo.isActivePlayer()) {
            this.gamesMap.put(nodeInfo, new ArrayList<>());
        }
    }

    public synchronized void setSelfActiveStatus(boolean status) {
        selfNode.setActivePlayer(status);
    }

    public synchronized NodeInfo getSelfNode() {
        return selfNode;
    }

    public synchronized void saveGameResultByKey(NodeInfo key, GameResult gameResult) {
        List<GameResult> mapValue = this.gamesMap.get(key);
        boolean keyExsist = mapValue != null;
        if (keyExsist) {
            mapValue.add(gameResult);
        } else {
            this.gamesMap.put(key, new ArrayList<>(Arrays.asList(gameResult)));
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
        List<NodeInfo> listOfWhoIPlayedWith = gamesPlayedByMe.stream().map(GameResult::getOpponent).collect(Collectors.toList());

        return availableNodes.stream().filter(node -> !listOfWhoIPlayedWith.contains(node)).collect(Collectors.toList());
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

        Map<NodeInfo, List<GameResult>> myConcurentMap = new HashMap<>();

        newMap.forEach((key, value) -> {
            boolean keyExist = this.gamesMap.get(key) != null;
            if (keyExist) {
                List<GameResult> duplicatesList = new ArrayList<>(this.gamesMap.get(key));
                duplicatesList.addAll(newMap.get(key));
                Set<GameResult> setWithoutDuplicates = new HashSet<>(duplicatesList);
                myConcurentMap.put(key, new ArrayList<>(setWithoutDuplicates));
            } else {
                List<GameResult> duplicatesList = new ArrayList<>(newMap.get(key));
                Set<GameResult> setWithoutDuplicates = new HashSet<>(duplicatesList);
                myConcurentMap.put(key, new ArrayList<>(setWithoutDuplicates));
            }
        });
//        System.out.println(myConcurentMap);
        this.gamesMap = myConcurentMap;
//
//        for (NodeInfo key : newMap.keySet()) {
//            boolean keyExsist = this.gamesMap.get(key) != null;
//            if (keyExsist) {
//                this.gamesMap.get(key).addAll(newMap.get(key));
//            } else {
//                this.gamesMap.put(key, newMap.get(key));
//            }
//        }
//
//        Iterator it = newMap.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry pair = (Map.Entry)it.next();
////            System.out.println(pair.getKey() + " = " + pair.getValue());
//
//            NodeInfo key = (NodeInfo) pair.getKey();
//            boolean keyExsist = this.gamesMap.get(pair.getKey()) != null;
//            if (keyExsist) {
//                this.gamesMap.get(key).addAll(newMap.get(key));
//            } else {
//                this.gamesMap.put(key, newMap.get(key));
//            }
//            it.remove(); // avoids a ConcurrentModificationException
//        }
    }
}
