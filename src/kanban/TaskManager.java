package kanban;

import java.util.ArrayList;

public interface TaskManager {
    ArrayList<Task> getTasks();

    ArrayList<Subtask> getSubtasks();

    ArrayList<Epic> getEpics();

    void deleteAllTasks();

    void deleteAllSubtasks();

    void deleteAllEpics();

    Task getTask(Integer id);

    Subtask getSubtask(Integer id);

    Epic getEpic(Integer id);

    void addTask(Task task);

    void addSubtask(Subtask subtask);

    void addEpic(Epic epic);

    void updateTask(Task task);

    void updateSubtask(Subtask subtask);

    void updateEpic(Epic epic);

    void deleteTask(Integer id);

    void deleteSubtask(Integer id);

    void deleteEpic(Integer id);

    ArrayList<Subtask> getEpicSubtasks(Epic epic);

    ArrayList<Task> getHistory();
}
