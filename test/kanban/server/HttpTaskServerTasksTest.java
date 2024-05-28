package kanban.server;

import com.google.gson.reflect.TypeToken;
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

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerTasksTest extends HttpTaskServerTest {
    static class TaskListTypeToken extends TypeToken<List<Task>> {
    }

    public HttpTaskServerTasksTest() throws IOException {
        super();
    }

    @Test
    public void testGetTasks() throws IOException, InterruptedException {
        // создаём задачи в менеджере
        Task task1 = taskManager.addTask(new Task("Test 1", "Testing task 1"));
        Task task2 = taskManager.addTask(new Task("Test 2", "Testing task 2",
                LocalDateTime.of(2024, 12, 1, 15, 12), Duration.ofMinutes(5)));

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        // вызываем рест, отвечающий за получение задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что вернулся корректный json
        assertDoesNotThrow(() -> gson.fromJson(response.body(), new TaskListTypeToken().getType()), "Должен возвращатся корректный json.");
        List<Task> tasks = gson.fromJson(response.body(), new TaskListTypeToken().getType());

        assertEquals(2, tasks.size(), "Неверное количество задач.");
    }

    @Test
    public void testGetTask() throws IOException, InterruptedException {
        // создаём задачу в менеджере
        Task task = taskManager.addTask(new Task("Test 2", "Testing task 2",
                LocalDateTime.of(2024, 12, 1, 15, 12), Duration.ofMinutes(5)));

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();

        // Создадим URI c некорректным номером
        URI url = URI.create(String.format("http://localhost:8080/tasks/%s", task.getId() + 1));
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        // вызываем рест, отвечающий за получение задачи
        HttpResponse<String> responseNotExist = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(404, responseNotExist.statusCode());

        // Создадим URI c корректным номером
        url = URI.create(String.format("http://localhost:8080/tasks/%s", task.getId()));
        request = HttpRequest.newBuilder().uri(url).GET().build();

        // вызываем рест, отвечающий за получение задачи
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что вернулся корректный json
        assertDoesNotThrow(() -> gson.fromJson(response.body(), Task.class), "Должен возвращатся корректный json.");
        Task taskFromRequest = gson.fromJson(response.body(), Task.class);

        assertNotNull(taskFromRequest, "Задачи не возвращаются");
        assertEquals(task, taskFromRequest, "Возвращается не та задача.");
        assertTrue(task.equalsByAllFields(taskFromRequest), "Задачи содержат разные поля.");
    }

    @Test
    public void testPostTask() throws IOException, InterruptedException {
        // создаём задачу
        Task task1 = new Task("Test 1", "Testing task 1",
                LocalDateTime.of(2024, 12, 1, 15, 10), Duration.ofMinutes(90));
        // конвертируем её в JSON
        String taskJson = gson.toJson(task1);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        List<Task> tasksFromManager = taskManager.getTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals(task1.getName(), tasksFromManager.get(0).getName(), "Некорректное имя задачи");

        // Попробуем создать задачу с пересекающимся интервалом
        // создаём задачу
        Task task2 = new Task("Test 2", "Testing task 2",
                LocalDateTime.of(2024, 12, 1, 16, 12), Duration.ofMinutes(5));
        // конвертируем её в JSON
        taskJson = gson.toJson(task2);

        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        // вызываем рест, отвечающий за создание задач
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(406, response.statusCode());

        // Обновим задачу
        task2.setId(tasksFromManager.get(0).getId());

        // конвертируем её в JSON
        taskJson = gson.toJson(task2);

        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        // вызываем рест, отвечающий за создание задач
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что в менеджере так и осталась одна задача, но с другим именем
        tasksFromManager = taskManager.getTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals(task2.getName(), tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void testDeleteTask() throws IOException, InterruptedException {
        // создаём задачу в менеджере
        Task task = taskManager.addTask(new Task("Test 2", "Testing task 2",
                LocalDateTime.of(2024, 12, 1, 15, 12), Duration.ofMinutes(5)));

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();

        // Создадим URI c некорректным номером
        URI url = URI.create(String.format("http://localhost:8080/tasks/%s", task.getId() + 1));
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        // вызываем рест, отвечающий за удаление задачи
        HttpResponse<String> responseNotExist = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(404, responseNotExist.statusCode());

        // Создадим URI c корректным номером
        url = URI.create(String.format("http://localhost:8080/tasks/%s", task.getId()));
        request = HttpRequest.newBuilder().uri(url).DELETE().build();

        // вызываем рест, отвечающий за получение задачи
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(200, response.statusCode());

        assertEquals(0, taskManager.getTasks().size(), "Задача не удалилась.");
    }
}