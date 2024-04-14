import kanban.model.Task;
import kanban.model.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskTest {

    @Test
    void shouldBeEqualById() {
        Task task1 = new Task(101, TaskStatus.NEW, "Задача 1", "Описание задачи 1");
        Task task2 = new Task(101, TaskStatus.IN_PROGRESS, "Задача номер два", "Без описания");

        assertEquals(task1, task2);
    }
}