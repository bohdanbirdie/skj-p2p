package shared;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {
    public static char[] chars = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
            'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
            'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
            'y', 'z', '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C', 'D',
            'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
            'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z', '!', '@',
            '#', '$', '%', '^', '&', '(', ')', '+',
            '-', '*', '/', '[', ']', '{', '}', '=',
            '<', '>', '?', '_', '"', '.', ',', ' '
    };

    public synchronized static List<NodeInfo> mergerUniqueLatest(List<NodeInfo> first, List<NodeInfo> second) {
        Map<Integer, List<NodeInfo>> grouped = concatAndGroup(first, second);

        List<NodeInfo> result = new ArrayList<>();
        Iterator<Map.Entry<Integer, List<NodeInfo>>> it = grouped.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<Integer, List<NodeInfo>> pair = it.next();

            List<NodeInfo> deadCase = getOnlyDeadNodes(pair.getValue());
            if (deadCase.size() > 0) {
                // After merging we might have two same nodes, but one of then can be marked as a dead
                // and another - not, so if there was at least one dead node - we will take it and update timestamp
                deadCase.get(0).setWasAliveTimestamp();
                result.add(deadCase.get(0));
            } else {
                // Otherwise - same the latest updated node to the list
                NodeInfo latestUpdated = Collections.max(pair.getValue(), Comparator.comparing(NodeInfo::getWasAliveTimestamp));
                result.add(latestUpdated);
            }
            it.remove(); // avoid¬s a ConcurrentModificationException
        }
        result.sort(Comparator.comparing(NodeInfo::getWasAliveTimestamp));
        return result;
    }

    private synchronized static List<NodeInfo> getOnlyDeadNodes(List<NodeInfo> nodesList) {
        return nodesList
                .stream()
                .filter(NodeInfo::isDead)
                .collect(Collectors.toList());
    }

    private synchronized static Map<Integer, List<NodeInfo>> concatAndGroup(List<NodeInfo> first, List<NodeInfo> second) {
        return Stream
                .concat(first.stream(), second.stream())
                .collect(Collectors.groupingBy(NodeInfo::getPingPort));
    }

    public static int getRandomNumberInRange(int min, int max) {
        if (max == min) return max;
        if (min >= max) {
            return 0;
        }
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public static String encrypt(String text, int offset) throws Exception {
        char[] plain = text.toCharArray();

        for (int i = 0; i < plain.length; i++) {
            for (int j = 0; j < chars.length; j++) {
                if (j <= chars.length - offset) {
                    if (plain[i] == chars[j]) {
                        plain[i] = chars[j + offset];
                        break;
                    }
                } else if (plain[i] == chars[j]) {
                    plain[i] = chars[j - (chars.length - offset + 1)];
                }
            }
        }
        return String.valueOf(plain);
    }

    public static String decrypt(String cip, int offset) throws Exception {
        {
            char[] cipher = cip.toCharArray();
            for (int i = 0; i < cipher.length; i++) {
                for (int j = 0; j < chars.length; j++) {
                    if (j >= offset && cipher[i] == chars[j]) {
                        cipher[i] = chars[j - offset];
                        break;
                    }
                    if (cipher[i] == chars[j] && j < offset) {
                        cipher[i] = chars[(chars.length - offset + 1) + j];
                        break;
                    }
                }
            }
            return String.valueOf(cipher);
        }
    }
}
