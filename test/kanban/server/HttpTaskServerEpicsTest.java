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

public class HttpTaskServerEpicsTest extends HttpTaskServerTest {
    static class EpicListTypeToken extends TypeToken<List<Epic>> {
    }

    public HttpTaskServerEpicsTest() throws IOException {
        super();
    }

    @Test
    public void testGetEpics() throws IOException, InterruptedException {
        // создаём задачи в менеджере
        Epic epic1 = taskManager.addEpic(new Epic("Test 1", "Testing epic 1"));
        Epic epic2 = taskManager.addEpic(new Epic("Test 2", "Testing epic 2",
                LocalDateTime.of(2024, 12, 1, 15, 12), Duration.ofMinutes(5)));

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        // вызываем рест, отвечающий за получение задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что вернулся корректный json
        assertDoesNotThrow(() -> gson.fromJson(response.body(), new EpicListTypeToken().getType()), "Должен возвращатся корректный json.");
        List<Epic> epics = gson.fromJson(response.body(), new EpicListTypeToken().getType());

        assertEquals(2, epics.size(), "Неверное количество эпиков.");
    }

    @Test
    public void testGetEpicSubtasks() throws IOException, InterruptedException {
        // создаём задачи в менеджере
        Epic epic = taskManager.addEpic(new Epic("Epic", "Epic test"));
        Subtask subtask1 = taskManager.addSubtask(new Subtask("Test 1", "Testing subtask 1", epic.getId()));
        Subtask subtask2 = taskManager.addSubtask(new Subtask("Test 2", "Testing subtask 2", epic.getId(),
                LocalDateTime.of(2024, 12, 1, 15, 12), Duration.ofMinutes(5)));

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format("http://localhost:8080/epics/%s/subtasks", epic.getId()));
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        // вызываем рест, отвечающий за получение задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что вернулся корректный json
        assertDoesNotThrow(() -> gson.fromJson(response.body(), new HttpTaskServerSubtasksTest.SubtasksListTypeToken().getType()),
                "Должен возвращатся корректный json.");
        List<Subtask> subtasks = gson.fromJson(response.body(), new HttpTaskServerSubtasksTest.SubtasksListTypeToken().getType());

        assertEquals(2, subtasks.size(), "Неверное количество сабтасков у эпика.");
    }

    @Test
    public void testGetEpic() throws IOException, InterruptedException {
        // создаём задачу в менеджере
        Epic epic = taskManager.addEpic(new Epic("Test 2", "Testing epic 2",
                LocalDateTime.of(2024, 12, 1, 15, 12), Duration.ofMinutes(5)));

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();

        // Создадим URI c некорректным номером
        URI url = URI.create(String.format("http://localhost:8080/epics/%s", epic.getId() + 1));
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        // вызываем рест, отвечающий за получение задачи
        HttpResponse<String> responseNotExist = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(404, responseNotExist.statusCode());

        // Создадим URI c корректным номером
        url = URI.create(String.format("http://localhost:8080/epics/%s", epic.getId()));
        request = HttpRequest.newBuilder().uri(url).GET().build();

        // вызываем рест, отвечающий за получение задачи
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что вернулся корректный json
        assertDoesNotThrow(() -> gson.fromJson(response.body(), Epic.class), "Должен возвращатся корректный json.");
        Epic epicFromRequest = gson.fromJson(response.body(), Epic.class);

        assertNotNull(epicFromRequest, "Эпики не возвращаются");
        assertEquals(epic, epicFromRequest, "Возвращается не тот эпик.");
        assertTrue(epic.equalsByAllFields(epicFromRequest), "Эпики содержат разные поля.");
    }

    @Test
    public void testPostEpic() throws IOException, InterruptedException {
        // создаём задачу
        Epic epic1 = new Epic("Test 1", "Testing epic 1",
                LocalDateTime.of(2024, 12, 1, 15, 10), Duration.ofMinutes(90));
        // конвертируем её в JSON
        String epicJson = gson.toJson(epic1);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        List<Epic> epicsFromManager = taskManager.getEpics();

        assertNotNull(epicsFromManager, "Эпики не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        assertEquals(epic1.getName(), epicsFromManager.get(0).getName(), "Некорректное имя эпика");

        // Попробуем создать задачу с пересекающимся интервалом
        // создаём задачу
        Epic epic2 = new Epic("Test 2", "Testing epic 2");

        // Обновим задачу
        epic2.setId(epicsFromManager.get(0).getId());

        // конвертируем её в JSON
        epicJson = gson.toJson(epic2);

        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();

        // вызываем рест, отвечающий за создание задач
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что в менеджере так и осталась одна задача, но с другим именем
        epicsFromManager = taskManager.getEpics();

        assertNotNull(epicsFromManager, "Эпики не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        assertEquals(epic2.getName(), epicsFromManager.get(0).getName(), "Некорректное имя эпика");
    }

    @Test
    public void testDeleteEpic() throws IOException, InterruptedException {
        // создаём задачу в менеджере
        Epic epic = taskManager.addEpic(new Epic("Test 2", "Testing epic 2"));

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();

        // Создадим URI c некорректным номером
        URI url = URI.create(String.format("http://localhost:8080/epics/%s", epic.getId() + 1));
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        // вызываем рест, отвечающий за удаление задачи
        HttpResponse<String> responseNotExist = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(404, responseNotExist.statusCode());

        // Создадим URI c корректным номером
        url = URI.create(String.format("http://localhost:8080/epics/%s", epic.getId()));
        request = HttpRequest.newBuilder().uri(url).DELETE().build();

        // вызываем рест, отвечающий за получение задачи
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(200, response.statusCode());

        assertEquals(0, taskManager.getTasks().size(), "Эпик не удалился.");
    }
}