package kanban.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    private Integer id;
    private TaskStatus status;
    private String name;
    private String description;
    private LocalDateTime startTime;
    private Duration duration;

    public Task() {
        this.status = TaskStatus.NEW;
        this.name = "";
        this.description = "";
    }

    public Task(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }

    public Task(String name, String description, LocalDateTime startTime, Duration duration) {
        this();
        this.name = name;
        this.description = description;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Task(int id, TaskStatus status, String name, String description) {
        this.id = id;
        this.status = status;
        this.name = name;
        this.description = description;
    }

    public Task(int id, TaskStatus status, String name, String description, LocalDateTime startTime, Duration duration) {
        this.id = id;
        this.status = status;
        this.name = name;
        this.description = description;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Task(Task task) {
        this.id = task.getId();
        this.status = task.getStatus();
        this.name = task.getName();
        this.description = task.getDescription();
        this.startTime = task.getStartTime();
        this.duration = task.getDuration();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", status=" + status +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", startTime='" + startTime + '\'' +
                ", duration='" + duration + '\'' +
                ", endTime='" + getEndTime() + '\'' +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public TaskTypes getTaskType() {
        return TaskTypes.TASK;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        } else {
            return startTime.plus(duration);
        }
    }
    public void setId(int id) {
        this.id = id;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }
}
