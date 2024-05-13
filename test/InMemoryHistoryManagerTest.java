import java.util.List;

import kanban.model.Task;
import kanban.model.TaskStatus;
import kanban.service.InMemoryHistoryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InMemoryHistoryManagerTest {
    private InMemoryHistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void add() {
        final String initialName = "Test addNewTask";
        final String initialDescription = "Test addNewTask description";
        Task task = new Task(1, TaskStatus.NEW, initialName, initialDescription);

        historyManager.add(task);
        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История пустая.");
        assertEquals(1, history.size(), "История не верного размера.");

        Task inHistoryTask = history.get(0);
        assertEquals(task.toString(), inHistoryTask.toString(), "Описание тасков в истории не совпадают после добавления.");

        task.setName("Another name");
        task.setDescription("Another name");

        assertNotEquals(task.toString(), inHistoryTask.toString(), "Описание таска в истории изменяется.");
        assertEquals(initialName, inHistoryTask.getName(), "Поля таска в истории  изменяются.");
        assertEquals(initialDescription, inHistoryTask.getDescription(), "Поля таска в истории изменяются.");

        Task task2 = new Task(2, TaskStatus.NEW, "Task2", "Description 2");
        historyManager.add(task2);
        historyManager.add(task);
        history = historyManager.getHistory();
        assertNotNull(history, "История пустая.");
        assertEquals(2, history.size(), "При повторном добавлении в историю изначальный просмотр не удаляется.");

        assertEquals(task2, history.get(0), "Не верный порядок просмотра задач поддерживается.");
        assertEquals(task, history.get(1), "Не верный порядок просмотра задач поддерживается.");
    }

    @Test
    void remove() {
        final String initialName = "Test addNewTask";
        final String initialDescription = "Test addNewTask description";
        Task task = new Task(1, TaskStatus.NEW, initialName, initialDescription);
        historyManager.add(task);
        task.setId(2);
        historyManager.add(task);
        task.setId(3);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size(), "В истории сохранились не все задачи.");

        historyManager.remove(2);
        history = historyManager.getHistory();
        assertEquals(2, history.size(), "Задача не удалена из истории.");

        if (history.size() == 2) {
            assertEquals(1, history.get(0).getId(), "Из истории удаляется не правильная задача.");
            assertEquals(3, history.get(1).getId(), "Из истории удаляется не правильная задача.");
        }
    }

}