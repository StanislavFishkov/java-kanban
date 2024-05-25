package kanban.service;

import kanban.exception.ManagerSaveException;
import kanban.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {
    private final File file;

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public Task addTask(Task task) {
        Task addedTask = super.addTask(task);
        save();
        return addedTask;
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        Subtask addedSubtask = super.addSubtask(subtask);
        save();
        return addedSubtask;
    }

    @Override
    public Epic addEpic(Epic epic) {
        Epic addedEpic = super.addEpic(epic);
        save();
        return addedEpic;
    }

    @Override
    public Task updateTask(Task task) {
        Task updatedTask = super.updateTask(task);
        save();
        return updatedTask;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        Subtask updatedSubtask = super.updateSubtask(subtask);
        save();
        return updatedSubtask;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic updatedEpic = super.updateEpic(epic);
        save();
        return updatedEpic;
    }

    @Override
    public void deleteTask(Integer id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteSubtask(Integer id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteEpic(Integer id) {
        super.deleteEpic(id);
        save();
    }

    public File getFile() {
        return file;
    }

    public FileBackedTaskManager(File file) {
        super();
        this.file = file;
    }

    private void save() {
        // create folder if it doesn't exist
        if (!file.exists()) {
            File folder = file.getParentFile();
            if (folder != null && !folder.exists()) {
                if (!folder.mkdirs()) {
                    throw new ManagerSaveException("Не удалось создать родительские директории файла для сохранения.");
                }
            }
        }

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            bufferedWriter.write(TaskParser.getHeader());
            for (Task task : getTasks()) {
                bufferedWriter.newLine();
                bufferedWriter.write(TaskParser.taskToString(task));
            }
            for (Epic epic : getEpics()) {
                bufferedWriter.newLine();
                bufferedWriter.write(TaskParser.taskToString(epic));
            }
            for (Subtask subtask : getSubtasks()) {
                bufferedWriter.newLine();
                bufferedWriter.write(TaskParser.taskToString(subtask));
            }
        } catch (IOException exception) {
            throw new ManagerSaveException(exception);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(file);
        if (file.exists()) {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
                // skip header
                bufferedReader.readLine();

                Integer maxId = 0;
                while (bufferedReader.ready()) {
                    String line = bufferedReader.readLine();
                    Task task = TaskParser.taskFromString(line);
                    if (task.getId() > maxId) {
                        maxId = task.getId();
                    }
                    switch (task.getTaskType()) {
                        case TASK -> fileBackedTaskManager.tasks.put(task.getId(), task);
                        case EPIC -> fileBackedTaskManager.epics.put(task.getId(), (Epic) task);
                        case SUBTASK -> fileBackedTaskManager.subtasks.put(task.getId(), (Subtask) task);
                    }
                }
                fileBackedTaskManager.idCounter = maxId;
            } catch (IOException exception) {
                throw new ManagerSaveException(exception);
            }

            fileBackedTaskManager.getTasks()
                    .forEach(fileBackedTaskManager::addToSortedTasks);

            // fill in list of subtasks' ids into its epics
            for (Subtask subtask : fileBackedTaskManager.getSubtasks()) {
                Epic epic = fileBackedTaskManager.epics.get(subtask.epic);
                epic.getSubtasks().add(subtask.getId());
                fileBackedTaskManager.addToSortedTasks(subtask);
            }
        }
        return fileBackedTaskManager;
    }

    public static void main(String[] args) {
        File file;
        try {
            file = File.createTempFile("testSavedFile", ".csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        TaskManager taskManager1 = new FileBackedTaskManager(file);

        taskManager1.addTask(new Task("Task1", "Task1",
                LocalDateTime.of(2024, 5, 10, 15, 34), Duration.ofMinutes(90)));
        taskManager1.addTask(new Task("Task2", "Task2"));
        Epic epic1 = new Epic("Epic1", "Epic1");
        taskManager1.addEpic(epic1);
        taskManager1.addEpic(new Epic("Epic2", "Epic2"));
        taskManager1.addSubtask(new Subtask("Subtask1", "Subtask1", epic1.getId()));

        Subtask subtask2 = new Subtask("Subtask2", "Subtask2", epic1.getId());
        subtask2.setStartTime(LocalDateTime.of(2024, 6, 1, 12, 0));
        subtask2.setDuration(Duration.ofMinutes(90));
        taskManager1.addSubtask(subtask2);

        Subtask subtask3 = new Subtask("Subtask3", "Subtask3", epic1.getId());
        subtask3.setStartTime(LocalDateTime.of(2024, 4, 7, 10, 0));
        subtask3.setDuration(Duration.ofMinutes(24 * 60));
        taskManager1.addSubtask(subtask3);

        // check all tasks exist in a new TaskManager, when loaded from file
        TaskManager taskManager2 = FileBackedTaskManager.loadFromFile(file);

        ArrayList<Task> allTasks1 = new ArrayList<>(taskManager1.getTasks());
        allTasks1.addAll(taskManager1.getSubtasks());
        allTasks1.addAll(taskManager1.getEpics());
        for (Task task1 : allTasks1) {
            Task task2 = null;
            switch (task1.getTaskType()) {
                case TASK -> task2 = taskManager2.getTask(task1.getId());
                case EPIC -> task2 = taskManager2.getEpic(task1.getId());
                case SUBTASK -> task2 = taskManager2.getSubtask(task1.getId());
            }
            if (!task1.equals(task2)) {
                throw new ManagerSaveException("Ошибка при восстановлении из файла: списки задач не идентичны.");
            }
        }
        System.out.printf("Восстановление из файла %s прошло успешно: списки задач идентичны.%n", file);
        System.out.println(taskManager2.getPrioritizedTasks());
    }
}
