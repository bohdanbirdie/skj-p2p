package shared;

// This tool is used to avoid situations when client A connect and try to play with server B
// and in the same moment client B try to play with server A
public class GameSynchronizer {
    static private NodeInfo currentPlayerWhoIPlayWith;

    public synchronized static boolean doIPlayWith(NodeInfo nodeInfo) {
        if (currentPlayerWhoIPlayWith == null) return false;
        return currentPlayerWhoIPlayWith.equals(nodeInfo);
    }

    public synchronized static void setCurrentPlayerWhoIPlayWith(NodeInfo currentPlayerWhoIPlayWith) {
        GameSynchronizer.currentPlayerWhoIPlayWith = currentPlayerWhoIPlayWith;
    }
}
