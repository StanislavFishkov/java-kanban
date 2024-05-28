package kanban.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {
    protected final ArrayList<Integer> subtasks;
    private LocalDateTime endTime;

    public Epic() {
        super();
        this.subtasks = new ArrayList<>();
    }

    public Epic(String name, String description) {
        super(name, description);
        this.subtasks = new ArrayList<>();
    }

    public Epic(String name, String description, LocalDateTime startTime, Duration duration) {
        super(name, description, startTime, duration);
        this.subtasks = new ArrayList<>();
    }

    public Epic(int id, String name, String description) {
        super(id, TaskStatus.NEW, name, description);
        this.subtasks = new ArrayList<>();
    }

    public Epic(int id, TaskStatus status, String name, String description) {
        super(id, status, name, description);
        this.subtasks = new ArrayList<>();
    }

    public Epic(int id, TaskStatus status, String name, String description, LocalDateTime startTime, Duration duration) {
        super(id, status, name, description, startTime, duration);
        this.subtasks = new ArrayList<>();
    }

    public Epic(Epic epic) {
        super(epic);
        this.subtasks = new ArrayList<>(epic.subtasks);
        this.endTime = epic.getEndTime();
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", status=" + getStatus() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", subtasks.size='" + subtasks.size() + '\'' +
                ", startTime='" + getStartTime() + '\'' +
                ", duration='" + getDuration() + '\'' +
                ", endTime='" + getEndTime() + '\'' +
                '}';
    }

    @Override
    public boolean equalsByAllFields(Object o) {
        return super.equalsByAllFields(o) && Objects.equals(endTime, ((Epic) o).endTime)
                && Objects.equals(subtasks, ((Epic) o).subtasks);
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public ArrayList<Integer> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(ArrayList<Integer> subtasks) {
        this.subtasks.clear();
        if (subtasks == null) return;
        this.subtasks.addAll(subtasks);
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public TaskTypes getTaskType() {
        return TaskTypes.EPIC;
    }
}
