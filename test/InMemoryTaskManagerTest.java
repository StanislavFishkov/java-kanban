import java.util.List;

import kanban.model.*;
import kanban.service.InMemoryTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryTaskManagerTest {
    private InMemoryTaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

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

        subtask = new Subtask(epicId, TaskStatus.NEW, "Test Subtask 2", "Test Subtask 2 desc", epicId);
        inMemorySubtask = taskManager.updateSubtask(subtask);

        assertNull(inMemorySubtask, "Обновлен сабтаск с id собственного эпика.");
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

        subtask = new Subtask(subtaskId, TaskStatus.NEW, "Test Subtask 3", "Test Subtask 3 desc", subtaskId);
        inMemorySubtask = taskManager.updateSubtask(subtask);
        assertNull(inMemorySubtask, "Обновлен сабтаск с собственным id в качестве эпика.");
    }

    @Test
    void isNotPossibleToAddTaskByUpdate() {
        Task task = new Task(100, TaskStatus.NEW, "Test Task", "Test task desc");
        Task savedTask = taskManager.updateTask(task);

        assertNull(savedTask, "Добавлена задача через метод обновления.");
        assertTrue(taskManager.getTasks().isEmpty(), "Добавлена задача через метод обновления.");
    }

    @Test
    void isNotPossibleToAddSubtaskByUpdate() {
        Epic epic = new Epic(100, "Test Epic", "Test Epic description");
        epic = taskManager.addEpic(epic);

        Subtask subtask = new Subtask(100, TaskStatus.NEW, "Test addNewTask", "Test addNewTask description", epic.getId());
        Subtask savedSubtask = taskManager.updateSubtask(subtask);

        assertNull(savedSubtask, "Добавлен сабтаск через метод обновления.");
        assertTrue(taskManager.getSubtasks().isEmpty(), "Добавлен сабтаск через метод обновления.");
    }

    @Test
    void isNotPossibleToAddEpicByUpdate() {
        Epic epic = new Epic(100, "Test Epic", "Test Epic description");
        Epic savedEpic = taskManager.updateEpic(epic);

        assertNull(savedEpic, "Добавлена задача через метод обновления.");
        assertTrue(taskManager.getEpics().isEmpty(), "Добавлена задача через метод обновления.");
    }
}