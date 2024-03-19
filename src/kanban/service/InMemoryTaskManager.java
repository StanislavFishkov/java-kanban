package kanban.service;

import kanban.model.*;

import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {
    private Integer idCounter;
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Subtask> subtasks;
    private final HashMap<Integer, Epic> epics;
    private final HistoryManager historyManager;


    public InMemoryTaskManager() {
        idCounter = 0;
        tasks = new HashMap<>();
        subtasks = new HashMap<>();
        epics = new HashMap<>();
        historyManager = Managers.getDefaultHistory();
    }

    private void calculateEpicStatus(Epic epic) {
        if (epic.getSubtasks().isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }
        TaskStatus calculatedStatus = null;
        for (Integer id : epic.getSubtasks()) {
            TaskStatus status = getSubtask(id).getStatus();
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


    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        for (Epic epic : epics.values()) {
            epic.getSubtasks().clear();
            calculateEpicStatus(epic);
        }
        subtasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        subtasks.clear();
        epics.clear();
    }

    @Override
    public Task getTask(Integer id) {
        Task task = tasks.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Subtask getSubtask(Integer id) {
        Subtask subtask = subtasks.get(id);
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public Epic getEpic(Integer id) {
        Epic epic = epics.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Task addTask(Task task) {
        task.setId(++idCounter);
        tasks.put(idCounter, task);
        return task;
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        Epic epic = getEpic(subtask.epic);
        if (epic == null) {
            return null;
        }

        subtask.setId(++idCounter);
        subtasks.put(idCounter, subtask);
        epic.getSubtasks().add(subtask.getId());
        calculateEpicStatus(epic);
        return subtask;
    }

    @Override
    public Epic addEpic(Epic epic) {
        if (!epic.getSubtasks().isEmpty()) {
            epic.getSubtasks().clear();
        }
        epic.setId(++idCounter);
        epics.put(idCounter, epic);
        return epic;
    }

    @Override
    public Task updateTask(Task task) {
        Task oldTask = getTask(task.getId());
        if (oldTask == null) {
            return null;
        }
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        Subtask oldSubtask = getSubtask(subtask.getId());
        if (oldSubtask == null) {
            return null;
        }

        Epic epic = getEpic(subtask.epic);
        if (epic == null) {
            return null;
        }

        if (subtask.getId().equals(subtask.epic)) {
            return null;
        }

        // У подзадачи мог измениться эпик. В этом случае требуются дополнительные действия.
        if (!subtask.epic.equals(oldSubtask.epic)) {
            Epic oldEpic = getEpic(oldSubtask.epic);
            oldEpic.getSubtasks().remove(oldSubtask.getId());
            calculateEpicStatus(oldEpic);
            epic.getSubtasks().add(subtask.getId());
        }
        subtasks.put(subtask.getId(), subtask);
        calculateEpicStatus(epic);
        return subtask;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic oldEpic = getEpic(epic.getId());
        if (oldEpic == null) {
            return null;
        }

        // Перенесем список привязанных подзадач в новый инстанс эпика.
        epic.setSubtasks(getEpic(epic.getId()).getSubtasks());
        epics.put(epic.getId(), epic);
        calculateEpicStatus(epic);
        return epic;
    }

    @Override
    public void deleteTask(Integer id) {
        tasks.remove(id);
    }

    @Override
    public void deleteSubtask(Integer id) {
        Subtask subtask = getSubtask(id);
        Epic epic = getEpic(subtask.epic);
        epic.getSubtasks().remove(id);
        subtasks.remove(id);
        calculateEpicStatus(epic);
    }

    @Override
    public void deleteEpic(Integer id) {
        for (Integer subtaskId : getEpic(id).getSubtasks()) {
            subtasks.remove(subtaskId);
        }
        epics.remove(id);
    }

    @Override
    public ArrayList<Subtask> getEpicSubtasks(Epic epic) {
        ArrayList<Subtask> epicSubtasks = new ArrayList<>();
        for (Integer id : epic.getSubtasks()) {
            epicSubtasks.add(getSubtask(id));
        }
        return epicSubtasks;
    }

    @Override
    public ArrayList<Task> getHistory() {
        return historyManager.getHistory();
    }
}
