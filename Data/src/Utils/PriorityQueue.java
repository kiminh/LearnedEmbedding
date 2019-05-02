package Utils;

import java.util.*;

public class PriorityQueue<K extends Comparable,V> {
    private int limit;
    private int length;
    private TreeMap<K, Set<V>> queue;
    private boolean isAscending;

    public PriorityQueue(int limit, String opt) {
        this.limit = limit;
        this.length = 0;
        this.queue = new TreeMap<>();
        switch (opt) {
            case "ascending":
                isAscending = true;
                break;
            case "descending":
                isAscending = false;
                break;
            default:
                throw new IllegalArgumentException("Option error! Must be ascending ot descending");
        }
    }
    public void insert(K key, V value) {
        if(length < limit) {
            updateQueue(key, value);
            length++;
        }
        else {
            if((isAscending && key.compareTo(queue.lastKey()) <= 0) ||
                    (!isAscending && key.compareTo(queue.firstKey()) >= 0)) {
                updateQueue(key, value);
                cutQueue(queue);
            }
        }
    }
    public List<V> serialize() {
        List<V> queueList = new ArrayList<>();
        if(isAscending) {
            while(!queue.isEmpty()) {
                K k = queue.firstKey();
                Set<V> tempSet = queue.get(k);
                queueList.addAll(tempSet);
                queue.pollFirstEntry();
            }
        }
        else {
            while(!queue.isEmpty()) {
                K k = queue.lastKey();
                Set<V> tempSet = queue.get(k);
                queueList.addAll(tempSet);
                queue.pollLastEntry();
            }
        }
        return queueList;
    }
    public void clear() {
        this.queue.clear();
    }

    private void updateQueue(K key, V value) {
        if(queue.containsKey(key)) {
            queue.get(key).add(value);
        }
        else {
            Set<V> tempSet = new HashSet<>();
            tempSet.add(value);
            queue.put(key, tempSet);
        }
    }
    private void cutQueue(TreeMap<K, Set<V>> queue) {
        if(isAscending) {
            Set<V> tempSet = queue.get(queue.lastKey());
            V elementToRemove = tempSet.stream().findFirst().orElse(null);
            queue.get(queue.lastKey()).remove(elementToRemove);
            if(queue.get(queue.lastKey()).size() == 0)
                queue.pollLastEntry();
        }
        else {
            Set<V> tempSet = queue.get(queue.firstKey());
            V elementToRemove = tempSet.stream().findFirst().orElse(null);
            queue.get(queue.firstKey()).remove(elementToRemove);
            if(queue.get(queue.firstKey()).size() == 0)
                queue.pollFirstEntry();
        }
    }
}
