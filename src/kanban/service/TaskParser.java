package kanban.service;

import kanban.model.*;

import java.util.LinkedList;

public final class TaskParser {
    public static final String DELIMITER = ",";

    public static String taskToString(Task task) {
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

    public static Task taskFromString(String value) {
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
}
