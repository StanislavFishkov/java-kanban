package kanban.service;

import kanban.exception.NotFoundException;
import kanban.exception.TasksIntersectedException;
import kanban.model.Epic;
import kanban.model.Subtask;
import kanban.model.Task;
import kanban.model.TaskStatus;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected Integer idCounter;
    protected final HashMap<Integer, Task> tasks;
    protected final HashMap<Integer, Subtask> subtasks;
    protected final HashMap<Integer, Epic> epics;
    private final HistoryManager historyManager;
    private final TreeSet<Task> sortedTasks;

    public InMemoryTaskManager() {
        idCounter = 0;
        tasks = new HashMap<>();
        subtasks = new HashMap<>();
        epics = new HashMap<>();
        historyManager = Managers.getDefaultHistory();
        sortedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));
    }

    private void calculateEpicFields(Epic epic) {
        if (epic.getSubtasks().isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            epic.setStartTime(null);
            epic.setEndTime(null);
            epic.setDuration(null);
            return;
        }

        TaskStatus calculatedStatus = null;
        for (Integer id : epic.getSubtasks()) {
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

        epic.setStartTime(epic.getSubtasks().stream()
                .map(id -> Optional.ofNullable(subtasks.get(id).getStartTime()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .min(Comparator.naturalOrder())
                .orElse(null)
        );

        epic.setDuration(epic.getSubtasks().stream()
                .map(id -> Optional.ofNullable(subtasks.get(id).getDuration()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce(Duration::plus)
                .orElse(null)
        );

        epic.setEndTime(epic.getSubtasks().stream()
                .map(id -> Optional.ofNullable(subtasks.get(id).getEndTime()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(Comparator.naturalOrder())
                .orElse(null)
        );

    }

    private boolean areTasksIntersected(Task t1, Task t2) {
        if (t1.getStartTime() == null || t2.getStartTime() == null || t1.getEndTime() == null || t2.getEndTime() == null) {
            return false;
        } else {
            return t1.getEndTime().isAfter(t2.getStartTime()) && t1.getStartTime().isBefore(t2.getEndTime());
        }
    }

    private void checkForIntersections(Task task) {
        if (getPrioritizedTasks().stream()
                .filter(task2 -> !task2.equals(task))
                .anyMatch(task2 -> areTasksIntersected(task, task2))) {
            throw new TasksIntersectedException("Задача пересекается с другими задачами в менеджере.");
        }
    }

    protected void addToSortedTasks(Task task) {
        if (task.getStartTime() != null) {
            sortedTasks.add(task);
        }
    }

    protected void removeFromSortedTasks(Task task) {
        if (task.getStartTime() != null) {
            sortedTasks.remove(task);
        }
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

    private void clearHistory(HashMap<Integer, ? extends Task> tasksToRemove) {
        for (Integer id : tasksToRemove.keySet()) {
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteAllTasks() {
        clearHistory(tasks);
        tasks.values().forEach(this::removeFromSortedTasks);
        tasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        for (Epic epic : epics.values()) {
            epic.getSubtasks().clear();
            calculateEpicFields(epic);
        }
        clearHistory(subtasks);
        subtasks.values().forEach(this::removeFromSortedTasks);
        subtasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        clearHistory(subtasks);
        subtasks.values().forEach(this::removeFromSortedTasks);
        subtasks.clear();
        clearHistory(epics);
        epics.clear();
    }

    @Override
    public Task getTask(Integer id) {
        if (!tasks.containsKey(id)) throw new NotFoundException(String.format("Нет таска с id: %s", id));
        Task task = tasks.get(id);
        historyManager.add(task);
        return new Task(task);
    }

    @Override
    public Subtask getSubtask(Integer id) {
        if (!subtasks.containsKey(id)) throw new NotFoundException(String.format("Нет сабтаска с id: %s", id));
        Subtask subtask = subtasks.get(id);
        historyManager.add(subtask);
        return new Subtask(subtask);
    }

    @Override
    public Epic getEpic(Integer id) {
        if (!epics.containsKey(id)) throw new NotFoundException(String.format("Нет эпика с id: %s", id));
        Epic epic = epics.get(id);
        historyManager.add(epic);
        return new Epic(epic);
    }

    @Override
    public Task addTask(Task task) {
        if (task == null) return null;
        checkForIntersections(task);

        task.setId(++idCounter);
        tasks.put(idCounter, task);
        addToSortedTasks(task);
        return task;
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        if (subtask == null) return null;
        Epic epic = epics.get(subtask.epic);
        if (epic == null) return null;
        checkForIntersections(subtask);

        subtask.setId(++idCounter);
        subtasks.put(idCounter, subtask);
        epic.getSubtasks().add(subtask.getId());
        calculateEpicFields(epic);
        addToSortedTasks(subtask);
        return subtask;
    }

    @Override
    public Epic addEpic(Epic epic) {
        if (epic == null) return null;
        if (!epic.getSubtasks().isEmpty()) {
            epic.getSubtasks().clear();
        }
        epic.setId(++idCounter);
        epics.put(idCounter, epic);
        return epic;
    }

    @Override
    public Task updateTask(Task task) {
        if (task == null) return null;
        Task oldTask = tasks.get(task.getId());
        if (oldTask == null) throw new NotFoundException(String.format("Нет таска с id: %s", task.getId()));
        checkForIntersections(task);

        tasks.put(task.getId(), task);
        removeFromSortedTasks(oldTask);
        addToSortedTasks(task);
        return task;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        if (subtask == null) return null;
        Subtask oldSubtask = subtasks.get(subtask.getId());
        if (oldSubtask == null) throw new NotFoundException(String.format("Нет сабтаска с id: %s", subtask.getId()));

        Epic epic = epics.get(subtask.epic);
        if (epic == null) throw new NotFoundException(String.format("Нет эпика с id: %s", subtask.epic));
        if (subtask.getId().equals(subtask.epic)) return null;
        checkForIntersections(subtask);

        // У подзадачи мог измениться эпик. В этом случае требуются дополнительные действия.
        if (!subtask.epic.equals(oldSubtask.epic)) {
            Epic oldEpic = epics.get(oldSubtask.epic);
            oldEpic.getSubtasks().remove(oldSubtask.getId());
            calculateEpicFields(oldEpic);
            epic.getSubtasks().add(subtask.getId());
        }
        subtasks.put(subtask.getId(), subtask);
        calculateEpicFields(epic);
        removeFromSortedTasks(oldSubtask);
        addToSortedTasks(subtask);
        return subtask;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        if (epic == null) return null;
        Epic oldEpic = epics.get(epic.getId());
        if (oldEpic == null) throw new NotFoundException(String.format("Нет эпика с id: %s", epic.getId()));

        // Перенесем список привязанных подзадач в новый инстанс эпика.
        epic.setSubtasks(epics.get(epic.getId()).getSubtasks());
        epics.put(epic.getId(), epic);
        calculateEpicFields(epic);
        return epic;
    }

    @Override
    public void deleteTask(Integer id) {
        if (!tasks.containsKey(id)) throw new NotFoundException(String.format("Нет таска с id: %s", id));
        removeFromSortedTasks(tasks.get(id));
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteSubtask(Integer id) {
        if (!subtasks.containsKey(id)) throw new NotFoundException(String.format("Нет сабтаска с id: %s", id));
        Subtask subtask = subtasks.get(id);
        removeFromSortedTasks(subtask);
        Epic epic = epics.get(subtask.epic);
        epic.getSubtasks().remove(id);
        subtasks.remove(id);
        calculateEpicFields(epic);
        historyManager.remove(id);
    }

    @Override
    public void deleteEpic(Integer id) {
        if (!epics.containsKey(id)) throw new NotFoundException(String.format("Нет эпика с id: %s", id));
        for (Integer subtaskId : epics.get(id).getSubtasks()) {
            removeFromSortedTasks(subtasks.get(subtaskId));
            subtasks.remove(subtaskId);
            historyManager.remove(subtaskId);
        }
        epics.remove(id);
        historyManager.remove(id);
    }

    @Override
    public ArrayList<Subtask> getEpicSubtasks(Epic epic) {
        if (epic == null || !epics.containsKey(epic.getId())) return new ArrayList<>();
        return epic.getSubtasks().stream()
                .map(this::getSubtask)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ArrayList<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new LinkedList<>(sortedTasks);
    }
}
