package kanban.server;

import com.google.gson.reflect.TypeToken;
import kanban.model.Epic;
import kanban.model.Subtask;
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

public class HttpTaskServerSubtasksTest extends HttpTaskServerTest {
    static class SubtasksListTypeToken extends TypeToken<List<Subtask>> {
    }

    public HttpTaskServerSubtasksTest() throws IOException {
        super();
    }

    @Test
    public void testGetSubtasks() throws IOException, InterruptedException {
        // создаём задачи в менеджере
        Epic epic = taskManager.addEpic(new Epic("Epic", "Epic test"));
        Subtask subtask1 = taskManager.addSubtask(new Subtask("Test 1", "Testing subtask 1", epic.getId()));
        Subtask subtask2 = taskManager.addSubtask(new Subtask("Test 2", "Testing subtask 2", epic.getId(),
                LocalDateTime.of(2024, 12, 1, 15, 12), Duration.ofMinutes(5)));

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        // вызываем рест, отвечающий за получение задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что вернулся корректный json
        assertDoesNotThrow(() -> gson.fromJson(response.body(), new SubtasksListTypeToken().getType()), "Должен возвращатся корректный json.");
        List<Subtask> subtasks = gson.fromJson(response.body(), new SubtasksListTypeToken().getType());

        assertEquals(2, subtasks.size(), "Неверное количество подзадач.");
    }

    @Test
    public void testGetSubtask() throws IOException, InterruptedException {
        // создаём задачу в менеджере
        Epic epic = taskManager.addEpic(new Epic("Epic", "Epic test"));
        Subtask subtask = taskManager.addSubtask(new Subtask("Test 2", "Testing subtask 2", epic.getId(),
                LocalDateTime.of(2024, 12, 1, 15, 12), Duration.ofMinutes(5)));

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();

        // Создадим URI c некорректным номером
        URI url = URI.create(String.format("http://localhost:8080/subtasks/%s", subtask.getId() + 1));
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        // вызываем рест, отвечающий за получение задачи
        HttpResponse<String> responseNotExist = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(404, responseNotExist.statusCode());

        // Создадим URI c корректным номером
        url = URI.create(String.format("http://localhost:8080/subtasks/%s", subtask.getId()));
        request = HttpRequest.newBuilder().uri(url).GET().build();

        // вызываем рест, отвечающий за получение задачи
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что вернулся корректный json
        assertDoesNotThrow(() -> gson.fromJson(response.body(), Subtask.class), "Должен возвращатся корректный json.");
        Subtask subtaskFromRequest = gson.fromJson(response.body(), Subtask.class);

        assertNotNull(subtaskFromRequest, "Подзадачи не возвращаются");
        assertEquals(subtask, subtaskFromRequest, "Возвращается не та подзадача.");
        assertTrue(subtask.equalsByAllFields(subtaskFromRequest), "Подзадачи содержат разные поля.");
    }

    @Test
    public void testPostSubtask() throws IOException, InterruptedException {
        // создаём задачу
        Epic epic = taskManager.addEpic(new Epic("Epic", "Epic test"));
        Subtask subtask1 = new Subtask("Test 1", "Testing subtask 1", epic.getId(),
                LocalDateTime.of(2024, 12, 1, 15, 10), Duration.ofMinutes(90));
        // конвертируем её в JSON
        String subtaskJson = gson.toJson(subtask1);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        List<Subtask> subtasksFromManager = taskManager.getSubtasks();

        assertNotNull(subtasksFromManager, "Подзадачи не возвращаются");
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество подзадач");
        assertEquals(subtask1.getName(), subtasksFromManager.get(0).getName(), "Некорректное имя подзадачи");

        // Попробуем создать подзадачу с пересекающимся интервалом
        // создаём задачу
        Subtask subtask2 = new Subtask("Test 2", "Testing subtask 2", epic.getId(),
                LocalDateTime.of(2024, 12, 1, 16, 12), Duration.ofMinutes(5));
        // конвертируем её в JSON
        subtaskJson = gson.toJson(subtask2);

        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();

        // вызываем рест, отвечающий за создание задач
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(406, response.statusCode());

        // Обновим задачу
        subtask2.setId(subtasksFromManager.get(0).getId());

        // конвертируем её в JSON
        subtaskJson = gson.toJson(subtask2);

        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();

        // вызываем рест, отвечающий за создание задач
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что в менеджере так и осталась одна задача, но с другим именем
        subtasksFromManager = taskManager.getSubtasks();

        assertNotNull(subtasksFromManager, "Подзадачи не возвращаются");
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество подзадач");
        assertEquals(subtask2.getName(), subtasksFromManager.get(0).getName(), "Некорректное имя подзадачи");
    }

    @Test
    public void testDeleteSubtask() throws IOException, InterruptedException {
        // создаём задачу в менеджере
        Epic epic = taskManager.addEpic(new Epic("Epic", "Epic test"));
        Subtask subtask = taskManager.addSubtask(new Subtask("Test 2", "Testing subtask 2", epic.getId(),
                LocalDateTime.of(2024, 12, 1, 15, 12), Duration.ofMinutes(5)));

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();

        // Создадим URI c некорректным номером
        URI url = URI.create(String.format("http://localhost:8080/subtasks/%s", subtask.getId() + 1));
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        // вызываем рест, отвечающий за удаление задачи
        HttpResponse<String> responseNotExist = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(404, responseNotExist.statusCode());

        // Создадим URI c корректным номером
        url = URI.create(String.format("http://localhost:8080/subtasks/%s", subtask.getId()));
        request = HttpRequest.newBuilder().uri(url).DELETE().build();

        // вызываем рест, отвечающий за получение задачи
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(200, response.statusCode());

        assertEquals(0, taskManager.getTasks().size(), "Подзадача не удалилась.");
    }
}
