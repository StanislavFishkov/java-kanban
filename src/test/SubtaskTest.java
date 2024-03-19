import kanban.model.Subtask;
import kanban.model.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubtaskTest {

    @Test
    void shouldBeEqualById() {
        Subtask subtask1 = new Subtask(101, TaskStatus.NEW, "Задача 1", "Описание задачи 1", 23);
        Subtask subtask2 = new Subtask(101, TaskStatus.IN_PROGRESS, "Задача два", "Без описания", 45);

        assertEquals(subtask1, subtask2);
    }

}