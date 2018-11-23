package shared;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

public class NodeInfo implements Serializable {
    private String ipAddess;
    private int gamePort;
    private int pingPort;
    private long wasAliveTimestamp;
    private boolean isDead;


    public NodeInfo(String ipAddess, int gamePort, int pingPort) {
        this.ipAddess = ipAddess;
        this.gamePort = gamePort;
        this.pingPort = pingPort;
        this.isDead = false;
        this.wasAliveTimestamp = new Timestamp(System.currentTimeMillis()).getTime();
        toString();
    }

    public String getIpAddess() {
        return ipAddess;
    }

    public int getGamePort() {
        return gamePort;
    }

    public int getPingPort() {
        return pingPort;
    }

    public boolean isDead() {
        return isDead;
    }

    public void setWasAliveTimestamp(long newTimestamp) {
        this.wasAliveTimestamp = newTimestamp;
    }
    public void setWasAliveTimestamp() {
        this.wasAliveTimestamp = new Timestamp(System.currentTimeMillis()).getTime();
    }

    public void setDead(boolean dead) {
        isDead = dead;
    }

    public long getWasAliveTimestamp() {
        return wasAliveTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeInfo nodeInfo = (NodeInfo) o;
        return gamePort == nodeInfo.gamePort &&
                pingPort == nodeInfo.pingPort &&
//                isDead == nodeInfo.isDead &&
                Objects.equals(ipAddess, nodeInfo.ipAddess);
    }

    @Override
    public int hashCode() {

        return Objects.hash(ipAddess, gamePort, pingPort);
    }

    @Override
    public String toString() {
        return "NodeInfo{" +
                "ipAddess='" + ipAddess + '\'' +
                ", gamePort=" + gamePort +
                ", pingPort=" + pingPort +
                ", wasAliveTimestamp=" + wasAliveTimestamp +
                '}';
    }
}
