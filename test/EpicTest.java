import kanban.model.Epic;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EpicTest {

    @Test
    void shouldBeEqualById() {
        Epic epic1 = new Epic(101, "Задача 1", "Описание задачи 1");
        Epic epic2 = new Epic(101, "Задача номер два", "Без описания");

        assertEquals(epic1, epic2);
    }

}