import kanban.exception.ManagerSaveException;
import kanban.exception.NotFoundException;
import kanban.exception.TasksIntersectedException;
import kanban.model.*;
import kanban.service.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    @Test
    void addNewTask() {
        Task task = new Task("Test addNewTask", "Test addNewTask description");
        taskManager.addTask(task);
        final int taskId = task.getId();

        final Task savedTask = taskManager.getTask(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void addNewEpic() {
        Epic epic = new Epic("Test addNewEpic", "Test addNewEpic description");
        taskManager.addEpic(epic);
        final int epicId = epic.getId();

        final Epic savedEpic = taskManager.getEpic(epicId);

        assertNotNull(savedEpic, "¨Эпик не найден.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");

        final List<Epic> epics = taskManager.getEpics();

        assertNotNull(epics, "Эпики не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(epic, epics.get(0), "Эпики не совпадают.");
    }

    @Test
    void addNewSubtask() {
        Epic epic = new Epic("Test addNewEpic", "Test addNewEpic description");
        taskManager.addEpic(epic);

        final String initialName = "Test addNewSubtask";
        final String initialDescription = "Test addNewSubtask description";
        final int epicId = epic.getId();
        Subtask subtask = new Subtask(0, TaskStatus.IN_PROGRESS, initialName, initialDescription, epicId);
        taskManager.addSubtask(subtask);
        final int subtaskId = subtask.getId();

        final Subtask savedSubtask = taskManager.getSubtask(subtaskId);

        assertNotNull(savedSubtask, "¨Сабтаск не найден.");
        assertEquals(subtask, savedSubtask, "Сабтаски не совпадают.");
        assertEquals(initialName, savedSubtask.getName(), "Поле Name изменилось при сохранении.");
        assertEquals(initialDescription, savedSubtask.getDescription(), "Поле Description изменилось при сохранении.");
        assertEquals(epicId, savedSubtask.epic, "Поле epic изменилось при сохранении.");
        assertEquals(TaskStatus.IN_PROGRESS, savedSubtask.getStatus(), "Поле Status изменилось при сохранении.");

        final List<Subtask> subtasks = taskManager.getSubtasks();

        assertNotNull(subtasks, "Сабтаски не возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество сабтасков.");
        assertEquals(subtask, subtasks.get(0), "Сабтаски не совпадают.");
    }

    @Test
    void addEpicAsSelfSubtask() {
        Epic epic = new Epic("Test Epic", "Test Epic desc");
        taskManager.addEpic(epic);
        final int epicId = epic.getId();

        Subtask subtask = new Subtask(epicId, TaskStatus.NEW, "Test Subtask", "Test Subtask desc", epicId);
        Subtask inMemorySubtask = taskManager.addSubtask(subtask);

        assertNotEquals(inMemorySubtask.getId(), epicId, "Добавлен сабтаск с id собственного эпика.");

        Subtask subtask1 = new Subtask(epicId, TaskStatus.NEW, "Test Subtask 2", "Test Subtask 2 desc", epicId);

        assertThrows(NotFoundException.class, () -> taskManager.updateSubtask(subtask1),
                "Обновлен сабтаск с id собственного эпика.");
    }

    @Test
    void addSubtaskAsSelfEpic() {
        Epic epic = new Epic("Test Epic", "Test Epic desc");
        taskManager.addEpic(epic);
        final int epicId = epic.getId();

        Subtask subtask = new Subtask("Test Subtask", "Test Subtask desc", epicId);
        Subtask inMemorySubtask = taskManager.addSubtask(subtask);
        final int subtaskId = inMemorySubtask.getId();

        subtask = new Subtask("Test Subtask 2", "Test Subtask 2 desc", subtaskId);
        inMemorySubtask = taskManager.addSubtask(subtask);
        assertNull(inMemorySubtask, "Добавлен сабтаск с собственным id в качестве эпика.");

        Subtask subtask1 = new Subtask(subtaskId, TaskStatus.NEW, "Test Subtask 3", "Test Subtask 3 desc", subtaskId);
        assertThrows(NotFoundException.class, () -> taskManager.updateSubtask(subtask1),
                "Обновлен сабтаск с собственным id в качестве эпика.");
    }

    @Test
    void isNotPossibleToAddTaskByUpdate() {
        Task task = new Task(100, TaskStatus.NEW, "Test Task", "Test task desc");

        assertThrows(NotFoundException.class, () -> taskManager.updateTask(task), "Добавлена задача через метод обновления.");
        assertTrue(taskManager.getTasks().isEmpty(), "Добавлена задача через метод обновления.");
    }

    @Test
    void isNotPossibleToAddSubtaskByUpdate() {
        Epic epic = new Epic(100, "Test Epic", "Test Epic description");
        epic = taskManager.addEpic(epic);

        Subtask subtask = new Subtask(100, TaskStatus.NEW, "Test addNewTask", "Test addNewTask description", epic.getId());

        assertThrows(NotFoundException.class, () -> taskManager.updateSubtask(subtask), "Добавлен сабтаск через метод обновления.");
        assertTrue(taskManager.getSubtasks().isEmpty(), "Добавлен сабтаск через метод обновления.");
    }

    @Test
    void isNotPossibleToAddEpicByUpdate() {
        Epic epic = new Epic(100, "Test Epic", "Test Epic description");

        assertThrows(NotFoundException.class, () -> taskManager.updateEpic(epic), "Добавлена задача через метод обновления.");
        assertTrue(taskManager.getEpics().isEmpty(), "Добавлена задача через метод обновления.");
    }

    @Test
    void checkEpicCalculatedFieldsAndIntervalIntersection() {
        int epicId = taskManager.addEpic(new Epic("Epic", "Epic")).getId();
        assertEquals(TaskStatus.NEW, taskManager.getEpic(epicId).getStatus(), "Статус нового эпика не NEW.");

        Subtask subtask1 = taskManager.addSubtask(new Subtask("Subtask1", "Subtask1", epicId,
                LocalDateTime.of(2024, 5, 7, 14, 15), Duration.ofMinutes(90)));
        Subtask subtask2 = taskManager.addSubtask(new Subtask("Subtask2", "Subtask2", epicId,
                LocalDateTime.of(2024, 5, 1, 10, 0), Duration.ofMinutes(210)));
        Subtask subtask3 = taskManager.addSubtask(new Subtask("Subtask3", "Subtask3", epicId,
                LocalDateTime.of(2024, 5, 31, 12, 45), Duration.ofMinutes(100)));
        Epic epic = taskManager.getEpic(epicId);
        assertEquals(TaskStatus.NEW, epic.getStatus(),
                "Статус эпика, у которого все задачи NEW, не NEW.");
        assertEquals(Duration.ofMinutes(400), epic.getDuration(),
                "Неверный расчет Duration эпика.");
        assertEquals(subtask2.getStartTime(), epic.getStartTime(),
                "Неверный расчет StartTime эпика.");
        assertEquals(subtask3.getEndTime(), epic.getEndTime(),
                "Неверный расчет EndTime эпика.");

        Subtask updateSubtask = new Subtask(subtask2);
        updateSubtask.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(updateSubtask);
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(epicId).getStatus()
                , "Статус эпика, у которого есть задачи New и DONE, не IN_PROGRESS.");

        updateSubtask = new Subtask(subtask1);
        updateSubtask.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(updateSubtask);
        updateSubtask = new Subtask(subtask3);
        updateSubtask.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(updateSubtask);
        assertEquals(TaskStatus.DONE, taskManager.getEpic(epicId).getStatus(),
                "Статус эпика, у которого все задачи DONE, не DONE.");

        updateSubtask = new Subtask(subtask3);
        updateSubtask.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(updateSubtask);
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(epicId).getStatus(),
                "Статус эпика, у которого есть хотя бы одна задача IN_PROGRESS, не IN_PROGRESS.");

        Task task = new Task("Task", "Task",
                LocalDateTime.of(2024, 5, 31, 13, 15), Duration.ofMinutes(100));
        assertThrows(TasksIntersectedException.class, () -> taskManager.addTask(task),
                "Возможно добавить задачу с пересекающимся интервалом");

        Subtask subtask4 = taskManager.addSubtask(new Subtask("Subtask4", "Subtask4", epicId));
        Subtask updateSubtask1 = new Subtask(subtask4);
        updateSubtask1.setStartTime(LocalDateTime.of(2024, 5, 7, 14, 15));
        updateSubtask1.setDuration(Duration.ofMinutes(60));
        assertThrows(TasksIntersectedException.class, () -> taskManager.updateSubtask(updateSubtask1),
                "Возможно добавить задачу с пересекающимся интервалом");
    }
}