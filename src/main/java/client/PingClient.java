package client;

import shared.NodeInfo;
import shared.NodesInfoContainer;
import shared.Utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PingClient implements Runnable {
    private NodesInfoContainer nodesInfoContainer;
    private NodeInfo selfNode;
    private int triesToNotifyAboutDead = 0;
    private int maxTries = 10;
    private List<NodeInfo> whoIRemoved = new ArrayList<>();

    public PingClient(NodesInfoContainer nodesInfoContainer, NodeInfo selfNode) {
        this.nodesInfoContainer = nodesInfoContainer;
        this.selfNode = selfNode;
    }

    private void printWhoIRemoved() {
        System.out.println("I Removed: ");
        this.whoIRemoved.forEach(nodeInfo -> System.out.println(nodeInfo.getPingPort()));
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
                    System.out.println("Updated node");
//                    System.out.println("I know about: ");
//                    updatedNetworkNodes.forEach(node -> System.out.println(node.getPingPort() + "  " + (node.isDead() ? "dead" : "alive")));
//                    nodesInfoContainer.setLockAccess(false);
                    nodesInfoContainer.setNetworkNodes(updatedNetworkNodes);
                    nodeToCheckSocket.close();
                } catch (Exception e) {

                }
            }

        }
    }


    public void run() {
        while (true) {
            List<NodeInfo> availableNodes = nodesInfoContainer.getAvailableNodes(selfNode);
            long speed = (long) (Utils.getRandomNumberInRange(500, 700) * availableNodes.size() * 0.2);
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
                    nodesInfoContainer.updateNodeInfo(nodeToCheck);

                    ObjectOutputStream oout = new ObjectOutputStream((nodeToCheckSocket.getOutputStream()));
                    ObjectInputStream ooin = new ObjectInputStream((nodeToCheckSocket.getInputStream()));


                    System.out.print("\033[H\033[2J");
                    printWhoIRemoved();
                    System.out.println("My speed is " + speed + "ms");

                    System.out.println("=======================");
//                    System.out.println("Sending nodes: \n");
                    System.out.println("I'm " + selfNode.getPingPort() + " checking the " + nodeToCheck.getPingPort());
//                    System.out.println(nodesInfoContainer.getNetworkNodes());
                    System.out.println("=======================\n");

//                    nodesInfoContainer.setLockAccess(true);
                    oout.writeObject(nodesInfoContainer.getNetworkNodes());


                    List<NodeInfo> updatedNetworkNodes = (List<NodeInfo>) ooin.readObject();
//                    updatedNetworkNodes.sort(Comparator.comparing(NodeInfo::getWasAliveTimestamp));
                    System.out.println("I know about: ");
                    updatedNetworkNodes.forEach(node -> System.out.println(node.getPingPort() + "  " + (node.isDead() ? "dead" : "alive")));
//                    nodesInfoContainer.setLockAccess(false);
                    nodesInfoContainer.setNetworkNodes(updatedNetworkNodes);
                    nodeToCheckSocket.close();

                } catch (IOException e) {
                    System.out.println("failed to to connect to " + nodeToCheck);
                    nodesInfoContainer.setNodeToDead(nodeToCheck);
                    this.whoIRemoved.add(nodeToCheck);
                    int n = availableNodes.size() / 3;
                    System.out.println("TRY " + n);
                    notifyNnodes(n, selfNode);

//                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
