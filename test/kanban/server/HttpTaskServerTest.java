package kanban.server;

import com.google.gson.Gson;
import kanban.service.InMemoryTaskManager;
import kanban.service.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

public class HttpTaskServerTest {
    TaskManager manager;
    HttpTaskServer taskServer;
    Gson gson;

    public HttpTaskServerTest() throws IOException {
        // создаём экземпляр InMemoryTaskManager
        manager = new InMemoryTaskManager();
        // передаём его в качестве аргумента в конструктор HttpTaskServer
        taskServer = new HttpTaskServer(manager);
        gson = taskServer.getGson();
    }

    @BeforeEach
    public void setUp() throws IOException {
        manager.deleteAllTasks();
        manager.deleteAllSubtasks();
        manager.deleteAllEpics();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }
}
