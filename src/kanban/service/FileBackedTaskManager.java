package kanban.service;

import kanban.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {
    private static final String DELIMITER = ",";
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

    static class ManagerSaveException extends RuntimeException {
        public ManagerSaveException(String message) {
            super(message);
        }

        public ManagerSaveException(Throwable cause) {
            super(cause);
        }
    }

    public FileBackedTaskManager(File file) {
        super();
        this.file = file;
    }

    static String toString(Task task) {
        LinkedList<String> strings = new LinkedList<>();
        strings.add(task.getId().toString());
        strings.add(task.getTaskType().name());
        strings.add(task.getName());
        strings.add(task.getStatus().name());
        strings.add(task.getDescription());
        if (task.getTaskType() == TaskTypes.SUBTASK) {
            strings.add(((Subtask) task).epic.toString());
        } else {
            strings.add("");
        }
        return String.join(DELIMITER, strings);
    }

    static Task fromString(String value) {
        Task task;
        String[] strings = value.split(DELIMITER);
        int id = Integer.parseInt(strings[0]);
        TaskTypes taskType = TaskTypes.valueOf(strings[1]);
        String name = strings[2];
        TaskStatus taskStatus = TaskStatus.valueOf(strings[3]);
        String description = strings[4];
        switch (taskType) {
            case TASK -> task = new Task(id, taskStatus, name, description);
            case EPIC -> task = new Epic(id, taskStatus, name, description);
            case SUBTASK -> task = new Subtask(id, taskStatus, name, description, Integer.parseInt(strings[5]));
            default -> task = null;
        }
        return task;
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
            bufferedWriter.write("id,type,name,status,description,epic");
            for (Task task : getTasks()) {
                bufferedWriter.newLine();
                bufferedWriter.write(toString(task));
            }
            for (Epic epic : getEpics()) {
                bufferedWriter.newLine();
                bufferedWriter.write(toString(epic));
            }
            for (Subtask subtask : getSubtasks()) {
                bufferedWriter.newLine();
                bufferedWriter.write(toString(subtask));
            }
        } catch (IOException exception) {
            throw new ManagerSaveException(exception);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(file);
        if (file.exists()) {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
                // skip title
                bufferedReader.readLine();

                Integer maxId = 0;
                while (bufferedReader.ready()) {
                    String line = bufferedReader.readLine();
                    Task task = fromString(line);
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

            // fill in list of subtasks' ids into its epics
            for (Subtask subtask : fileBackedTaskManager.getSubtasks()) {
                Epic epic = fileBackedTaskManager.epics.get(subtask.epic);
                epic.getSubtasks().add(subtask.getId());
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

        taskManager1.addTask(new Task("Task1", "Task1"));
        taskManager1.addTask(new Task("Task2", "Task2"));
        Epic epic1 = new Epic("Epic1", "Epic1");
        taskManager1.addTask(epic1);
        taskManager1.addTask(new Epic("Epic2", "Epic2"));
        taskManager1.addSubtask(new Subtask("Subtask1", "Subtask1", epic1.getId()));

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
        System.out.printf("Восстановление из файла %s прошло успешно: списки задач идентичны.", file);
    }
}
