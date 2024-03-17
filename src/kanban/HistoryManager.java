package kanban;

import java.util.ArrayList;

interface HistoryManager {
    void add(Task task);
    ArrayList<Task> getHistory();
}
