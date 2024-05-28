package kanban.server;

import kanban.model.Epic;
import kanban.model.Subtask;
import kanban.model.Task;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskServerHistoryTest extends HttpTaskServerTest {

    public HttpTaskServerHistoryTest() throws IOException {
        super();
    }

    @Test
    public void testGetHistory() throws IOException, InterruptedException {
        // создаём задачи в менеджере
        Task task1 = taskManager.addTask(new Task("Test 1", "Testing task 1"));
        Task task2 = taskManager.addTask(new Task("Test 2", "Testing task 2",
                LocalDateTime.of(2024, 12, 1, 15, 12), Duration.ofMinutes(5)));
        Epic epic = taskManager.addEpic(new Epic("Epic", "Epic test"));
        Subtask subtask1 = taskManager.addSubtask(new Subtask("Test 1", "Testing subtask 1", epic.getId()));


        // Просмотрим задачу и сабтаск
        taskManager.getTask(task1.getId());
        taskManager.getSubtask(subtask1.getId());

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        // вызываем рест, отвечающий за получение задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что вернулся корректный json
        assertDoesNotThrow(() -> gson.fromJson(response.body(), new HttpTaskServerTasksTest.TaskListTypeToken().getType()),
                "Должен возвращатся корректный json.");
        List<Task> tasks = gson.fromJson(response.body(), new HttpTaskServerTasksTest.TaskListTypeToken().getType());

        assertEquals(2, tasks.size(), "Неверное количество задач.");
    }
}