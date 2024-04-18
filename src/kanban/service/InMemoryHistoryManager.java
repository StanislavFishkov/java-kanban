package kanban.service;

import kanban.model.*;

import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryHistoryManager implements HistoryManager {
    static class Node {
        private Node previous;
        private Node next;
        private final Task task;

        public Node(Task task) {
            this.task = task;
        }

        public Node getPrevious() {
            return previous;
        }

        public void setPrevious(Node previous) {
            this.previous = previous;
        }

        public Node getNext() {
            return next;
        }

        public void setNext(Node next) {
            this.next = next;
        }

        public Task getTask() {
            return task;
        }
    }

    Node firstNode;
    Node lastNode;
    private final HashMap<Integer, Node> history;

    public InMemoryHistoryManager() {
        this.history = new HashMap<>();
    }

    private Node linkLast(Task task) {
        Node node = new Node(copy(task));
        if (firstNode == null) {
            firstNode = node;
        } else {
            lastNode.setNext(node);
            node.setPrevious(lastNode);
        }
        lastNode = node;
        history.put(task.getId(), node);
        return node;
    }

    private void removeNode(Node node) {
        if (node == null) return;
        Node prevNode = node.getPrevious();
        Node nextNode = node.getNext();
        Integer taskId = node.getTask().getId();

        if (prevNode == null) {
            firstNode = nextNode;
        } else {
            prevNode.setNext(nextNode);
        }
        if (nextNode == null) {
            lastNode = prevNode;
        } else {
            nextNode.setPrevious(prevNode);
        }
        history.remove(taskId);
    }

    private ArrayList<Task> getTasks() {
        ArrayList<Task> tasks = new ArrayList<>();
        Node currentNode = firstNode;
        while (currentNode != null) {
            tasks.add(currentNode.getTask());
            currentNode = currentNode.getNext();
        }
        return tasks;
    }

    private Task copy(Task task) {
        if (task instanceof Subtask) {
            return new Subtask((Subtask) task);
        } else if (task instanceof Epic) {
            return new Epic((Epic) task);
        } else {
            return new Task(task);
        }
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        remove(task.getId());
        linkLast(task);
    }

    @Override
    public void remove(int id) {
        if (history.containsKey(id)) {
            removeNode(history.get(id));
        }
    }

    @Override
    public ArrayList<Task> getHistory() {
        return getTasks();
    }
}
