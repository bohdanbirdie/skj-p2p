package shared;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {
    public synchronized static List<NodeInfo> mergerUniqueLatest(List<NodeInfo> first, List<NodeInfo> second){
        Map<Integer, List<NodeInfo>> grouped = Stream.concat(first.stream(), second.stream())
//                .forEach(node )
                .collect(Collectors.groupingBy(NodeInfo::getPingPort));

        List<NodeInfo> result = new ArrayList<>();
        Iterator<Map.Entry<Integer, List<NodeInfo>>> it = grouped.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, List<NodeInfo>> pair = it.next();

            List<NodeInfo> deadCase = pair.getValue().stream().filter(node -> node.isDead()).collect(Collectors.toList());
            if (deadCase.size() > 0) {
                deadCase.get(0).setWasAliveTimestamp();
                result.add(deadCase.get(0));
//                System.out.println("Tried to push wrong value");
            } else {
                NodeInfo lastContact = Collections.max(pair.getValue(), Comparator.comparing(c -> c.getWasAliveTimestamp()));
                result.add(lastContact);
            }
            it.remove(); // avoids a ConcurrentModificationException
        }
        result.sort(Comparator.comparing(NodeInfo::getWasAliveTimestamp));
        return result;
    }

    public static int getRandomNumberInRange(int min, int max) {
        if (max == min) return max;
        if (min >= max) {
            return 0;
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }
}
