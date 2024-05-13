import kanban.model.*;
import kanban.service.FileBackedTaskManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    @BeforeEach
    void setUp() throws IOException {
        taskManager = new FileBackedTaskManager(File.createTempFile("FileBackedTaskManagerSaveTest", ".csv"));
    }

    @Test
    void loadFromEmptyFile() {
        FileBackedTaskManager taskManagerFromFile = FileBackedTaskManager.loadFromFile(taskManager.getFile());

        assertTrue(taskManagerFromFile.getTasks().isEmpty(), "Загрузка из пустого файла: список задач не пустой.");
        assertTrue(taskManagerFromFile.getEpics().isEmpty(),
                "Загрузка из пустого файла: список эпиков не пустой.");
        assertTrue(taskManagerFromFile.getSubtasks().isEmpty(),
                "Загрузка из пустого файла: список сабтасков не пустой.");
    }

    @Test
    void saveAndLoadFromFile() {
        assertEquals(0L, taskManager.getFile().length(), "Файл не пустой при создании.");

        Task task = taskManager.addTask(new Task("t1", "t1"));
        Epic epic = taskManager.addEpic(new Epic("e1", "e1"));
        Subtask subtask = taskManager.addSubtask(new Subtask("s1", "s1", epic.getId()));

        assertNotEquals(0L, taskManager.getFile().length(), "Файл пустой после добавления задач.");

        FileBackedTaskManager taskManagerFromFile = FileBackedTaskManager.loadFromFile(taskManager.getFile());

        List<Task> tasks = taskManagerFromFile.getTasks();
        List<Epic> epics = taskManagerFromFile.getEpics();
        List<Subtask> subtasks = taskManagerFromFile.getSubtasks();

        assertEquals(taskManager.getTasks().size(), tasks.size(),
                "Количество восстановленных задач не совпадате с количеством сохраненных.");
        assertEquals(taskManager.getEpics().size(),
                epics.size(),
                "Количество восстановленных задач не совпадате с количеством сохраненных.");
        assertEquals(taskManager.getSubtasks().size(), subtasks.size(),
                "Количество восстановленных задач не совпадате с количеством сохраненных.");

        assertEquals(task, tasks.getFirst(), "Восстановленная задача не совпадает с изначальной.");
        assertEquals(epic, epics.getFirst(), "Восстановленный эпик не совпадает с изначальным.");
        assertEquals(subtask, subtasks.getFirst(), "Восстановленный сабтаск не совпадает с изначальным.");

        Subtask loadedSubtask = subtasks.getFirst();

        assertEquals(subtask.getTaskType(), loadedSubtask.getTaskType(),
                "В восстановленном сабтаске неверное виртуальное поле taskType.");
        assertEquals(subtask.getName(), loadedSubtask.getName(),
                "В восстановленном сабтаске неверное поле name.");
        assertEquals(subtask.getDescription(), loadedSubtask.getDescription(),
                "В восстановленном сабтаске неверное поле description.");
        assertEquals(subtask.getStatus(), loadedSubtask.getStatus(),
                "В восстановленном сабтаске неверное поле status.");
        assertEquals(subtask.epic, loadedSubtask.epic, "В восстановленном сабтаске неверное поле epic.");

    }
}