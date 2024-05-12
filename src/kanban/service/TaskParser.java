package kanban.service;

import kanban.model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

public final class TaskParser {
    public static final String DELIMITER = ",";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");

    public static String getHeader() {
        return "id,type,name,status,description,epic,startTime,duration";
    }

    public static String taskToString(Task task) {
        LinkedList<String> strings = new LinkedList<>();
        strings.add(task.getId().toString());
        strings.add(task.getTaskType().name());
        strings.add(task.getName());
        strings.add(task.getStatus().name());
        strings.add(task.getDescription());
        strings.add(task.getTaskType() == TaskTypes.SUBTASK ? ((Subtask) task).epic.toString() : "");
        strings.add(task.getStartTime() == null ? "" : task.getStartTime().format(DATE_TIME_FORMATTER));
        strings.add(task.getDuration() == null ? "" : String.valueOf(task.getDuration().toMinutes()));
        return String.join(DELIMITER, strings);
    }

    public static Task taskFromString(String value) {
        Task task;
        String[] strings = value.split(DELIMITER, -1);
        int id = Integer.parseInt(strings[0]);
        TaskTypes taskType = TaskTypes.valueOf(strings[1]);
        String name = strings[2];
        TaskStatus taskStatus = TaskStatus.valueOf(strings[3]);
        String description = strings[4];
        LocalDateTime startTime = strings[6].isEmpty() ? null : LocalDateTime.parse(strings[6], DATE_TIME_FORMATTER);
        Duration duration = strings[7].isEmpty() ? null : Duration.ofMinutes(Long.parseLong(strings[7]));
        switch (taskType) {
            case TASK -> task = new Task(id, taskStatus, name, description, startTime, duration);
            case EPIC -> task = new Epic(id, taskStatus, name, description, startTime, duration);
            case SUBTASK -> task = new Subtask(id, taskStatus, name, description, Integer.parseInt(strings[5])
                    , startTime, duration);
            default -> task = null;
        }
        return task;
    }
}
