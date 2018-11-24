package shared;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NodesInfoContainer {
    private List<NodeInfo> networkNodes;
    private NodeInfo selfNode;

    public NodesInfoContainer() {
        this.networkNodes = new ArrayList<>();
    }

    public void setSelfNode(NodeInfo selfNode) {
        this.selfNode = selfNode;
    }

    public synchronized void updateNodeInfo(NodeInfo nodeInfo) {
        this.networkNodes.forEach(node -> {
            if(node.equals(nodeInfo)) {
                node.setWasAliveTimestamp(new Timestamp(System.currentTimeMillis()).getTime());
            }
        });
    }

    public synchronized void setNodeToDead(NodeInfo nodeInfo) {
        this.networkNodes.forEach(node -> {
            if (node.equals(nodeInfo)) {
                node.setDead(true);
            }
        });
    }

    public synchronized void setNodeToActivePlayer(NodeInfo nodeInfo) {
        this.networkNodes.forEach(node -> {
            if (node.equals(nodeInfo)) {
                node.setActivePlayer(true);
            }
        });
    }

    public synchronized List<NodeInfo> getNetworkNodes() {
        return networkNodes;
    }

    public synchronized List<NodeInfo> getAvailableNodes(NodeInfo nodeInfo) {
        return this.getNetworkNodes().stream().filter(node -> !node.equals(nodeInfo) && !node.isDead()).collect(Collectors.toList());
    }

    public synchronized List<NodeInfo> getAvailableNodesForGameOnly(NodeInfo nodeInfo) {
        return this.getNetworkNodes()
                .stream().filter(node -> !node.equals(nodeInfo) && !node.isDead() && node.isActivePlayer())
                .collect(Collectors.toList());
    }

    public synchronized void setNetworkNodes(List<NodeInfo> networkNodes) {
        this.networkNodes = Utils.mergerUniqueLatest(this.networkNodes, networkNodes);
        if (selfNode != null) {
            if (selfNode.isActivePlayer()){
                setNodeToActivePlayer(selfNode);
            }
        }
    }
}
