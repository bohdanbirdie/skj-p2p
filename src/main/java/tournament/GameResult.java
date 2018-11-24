package tournament;

import shared.NodeInfo;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

public class GameResult implements Serializable {
    private NodeInfo selfNode;
    private NodeInfo opponent;
    private NodeInfo winner;
    private long playedTimestamp;

    public GameResult(NodeInfo selfNode, NodeInfo opponent, NodeInfo winner) {
        this.selfNode = selfNode;
        this.opponent = opponent;
        this.winner = winner;
        this.playedTimestamp = new Timestamp(System.currentTimeMillis()).getTime();
    }

    public NodeInfo getSelfNode() {
        return selfNode;
    }

    public NodeInfo getOpponent() {
        return opponent;
    }

    public NodeInfo getWinner() {
        return winner;
    }

    public boolean checkIfWinner(NodeInfo node) {
        return node.equals(winner);
    }

    public long getPlayedTimestamp() {
        return playedTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameResult that = (GameResult) o;
        return Objects.equals(selfNode, that.selfNode) &&
                Objects.equals(opponent, that.opponent) &&
                Objects.equals(winner, that.winner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(selfNode, opponent, winner);
    }
}
