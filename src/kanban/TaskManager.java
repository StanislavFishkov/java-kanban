package kanban;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private Integer idCounter;
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Subtask> subtasks;
    private final HashMap<Integer, Epic> epics;

    public TaskManager() {
        idCounter = 0;
        tasks = new HashMap<>();
        subtasks = new HashMap<>();
        epics = new HashMap<>();
    }

    public ArrayList<Task> getTasks() {
        return new ArrayList<Task>(tasks.values());
    }

    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<Subtask>(subtasks.values());
    }

    public ArrayList<Epic> getEpics() {
        return new ArrayList<Epic>(epics.values());
    }

    private void calculateEpicStatus(Epic epic) {
        if (epic.subtasks.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }
        TaskStatus calculatedStatus = null;
        for (Integer id : epic.subtasks) {
            TaskStatus status = subtasks.get(id).getStatus();
            if (status == TaskStatus.IN_PROGRESS) {
                calculatedStatus = TaskStatus.IN_PROGRESS;
                break;
            } else if (calculatedStatus == null) {
                calculatedStatus = status;
            } else if (calculatedStatus != status) {
                calculatedStatus = TaskStatus.IN_PROGRESS;
                break;
            }
        }
        epic.setStatus(calculatedStatus);
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public void deleteAllSubtasks() {
        for (Epic epic : epics.values()) {
            epic.subtasks.clear();
            calculateEpicStatus(epic);
        }
        subtasks.clear();
    }

    public void deleteAllEpics() {
        subtasks.clear();
        epics.clear();
    }

    public Task getTask(Integer id) {
        return tasks.get(id);
    }

    public Subtask getSubtask(Integer id) {
        return subtasks.get(id);
    }

    public Epic getEpic(Integer id) {
        return epics.get(id);
    }

    public void addTask(Task task) {
        task.setId(++idCounter);
        tasks.put(idCounter, task);
    }

    public void addSubtask(Subtask subtask) {
        subtask.setId(++idCounter);
        subtasks.put(idCounter, subtask);
        subtask.epic.subtasks.add(subtask.getId());
        calculateEpicStatus(subtask.epic);
    }

    public void addEpic(Epic epic) {
        epic.setId(++idCounter);
        epics.put(idCounter, epic);
    }

    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public void updateSubtask(Subtask subtask) {
        Subtask oldSubtask = subtasks.get(subtask.getId());
        // У подзадачи мог измениться эпик. В этом случае требуются дополнительные действия.
        if (!subtask.epic.equals(oldSubtask.epic)) {
            oldSubtask.epic.subtasks.remove(oldSubtask.getId());
            calculateEpicStatus(oldSubtask.epic);
            subtask.epic.subtasks.add(subtask.getId());
        }
        subtasks.put(subtask.getId(), subtask);
        calculateEpicStatus(subtask.epic);
    }

    public void updateEpic(Epic epic) {
        // Перенесем список привязанных подзадач в новый инстанс эпика.
        epic.subtasks = epics.get(epic.getId()).subtasks;
        epics.put(epic.getId(), epic);
        calculateEpicStatus(epic);
    }

    public void deleteTask(Integer id) {
        tasks.remove(id);
    }

    public void deleteSubtask(Integer id) {
        Subtask subtask = subtasks.get(id);
        subtask.epic.subtasks.remove(id);
        subtasks.remove(id);
        calculateEpicStatus(subtask.epic);
    }

    public void deleteEpic(Integer id) {
        for (Integer subtaskId : epics.get(id).subtasks) {
            subtasks.remove(subtaskId);
        }
        epics.remove(id);
    }

    public ArrayList<Subtask> getEpicSubtasks(Epic epic) {
        ArrayList<Subtask> epicSubtasks = new ArrayList<>();
        for (Integer id : epic.subtasks) {
            epicSubtasks.add(subtasks.get(id));
        }
        return epicSubtasks;
    }
}
