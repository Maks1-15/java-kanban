package service;

import task.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Integer, Node> mapHistory = new HashMap<>();
    private Node head; // узел головы
    private Node tail; // узел хвоста

    @Override
    public int addTaskInMapHistory(Task task) {
        if (task == null) {
            return -1;
        }
        // удаляем запись, если она была до этого записана в истории
        if (mapHistory.containsKey(task.getId())) {
            removeIdByHistoryMap(task.getId());
        }
        // добавляем актуальную запись
        mapHistory.put(task.getId(), linkLast(task));
        return task.getId();
    }

    @Override
    public void removeIdByHistoryMap(int id) {
        if (id < 0 || !mapHistory.containsKey(id)) {
            return;
        }
        removeNode(mapHistory.get(id));
        mapHistory.remove(id);
    }

    @Override
    public List<Task> getHistory() {
        List<Task> historyList = new ArrayList<>();
        Node currentNode = head;
        while (Objects.nonNull(currentNode)) {
            historyList.add(currentNode.getTaskInNode());
            currentNode = currentNode.next;
        }
        return historyList;
    }

    private Node linkLast(Task task) {
        if (task == null) {
            return null;
        }

        final Node newNode = new Node(tail, task, null);
        if (tail != null) {
            tail.next = newNode;
        }

        tail = newNode;
        if (head == null) {
            head = newNode;
        }
        return newNode;
    }

    private void removeNode(Node n) {
        if (n == null) {
            return;
        }

        if (n.prev == null && n.next == null) {
            return;
        }

        if (n.prev != null) {
            n.prev.next = n.next;
        } else {
            head = n.next;
        }

        if (n.next != null) {
            n.next.prev = n.prev;
        } else {
            tail = n.prev;
        }
    }

    private class Node {

        private Node next;
        private Task task;
        private Node prev;

        public Node(Node prev, Task task, Node next) {
            this.next = next;
            this.task = task;
            this.prev = prev;
        }

        public Task getTaskInNode() {
            return task;
        }
    }
}
