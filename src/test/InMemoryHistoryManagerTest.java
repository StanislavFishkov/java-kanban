import java.util.List;

import kanban.model.Task;
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
        Task task = new Task(initialName, initialDescription);

        historyManager.add(task);
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не пустая.");
        assertEquals(1, history.size(), "История не пустая.");

        Task inHistoryTask = history.get(0);
        assertEquals(task.toString(), inHistoryTask.toString(), "Описание тасков в истории совпадают после добавления.");

        task.setName("Another name");
        task.setDescription("Another name");

        assertNotEquals(task.toString(), inHistoryTask.toString(), "Описание таска в истории не изменяется.");
        assertEquals(initialName, inHistoryTask.getName(), "Поля таска в истории не изменяется.");
        assertEquals(initialDescription, inHistoryTask.getDescription(), "Поля таска в истории не изменяется.");
    }

}