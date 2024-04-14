import kanban.service.HistoryManager;
import kanban.service.Managers;
import kanban.service.TaskManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ManagersTest {

    @Test
    void taskManagerShouldNotBeNull() {
        TaskManager taskManager = Managers.getDefault();

        assertNotNull(taskManager, "Должен возвращаться проинициализированный TaskManager.");
    }

    @Test
    void historyManagerShouldNotBeNull() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        assertNotNull(historyManager, "Должен возвращаться проинициализированный HistoryManager.");
    }

}