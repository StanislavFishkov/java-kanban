package kanban;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {
    private final ArrayList<Task> history;

    public InMemoryHistoryManager() {
        this.history = new ArrayList<>();
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
        if (history.size() == 10) {
            history.removeFirst();
        }
        history.add(copy(task));
    }

    @Override
    public ArrayList<Task> getHistory() {
        return history;
    }
}
