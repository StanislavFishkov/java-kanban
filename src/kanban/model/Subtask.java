package kanban.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Subtask extends Task {
    public final Integer epic;

    public Subtask(Integer epic) {
        super();
        this.epic = epic;
    }

    public Subtask(String name, String description, Integer epic) {
        super(name, description);
        this.epic = epic;
    }

    public Subtask(String name, String description, Integer epic, LocalDateTime startTime, Duration duration) {
        super(name, description, startTime, duration);
        this.epic = epic;
    }

    public Subtask(int id, TaskStatus status, String name, String description, Integer epic) {
        super(id, status, name, description);
        this.epic = epic;
    }

    public Subtask(int id, TaskStatus status, String name, String description, Integer epic, LocalDateTime startTime,
                   Duration duration) {
        super(id, status, name, description, startTime, duration);
        this.epic = epic;
    }

    public Subtask(Subtask subtask) {
        super(subtask);
        this.epic = subtask.epic;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + getId() +
                ", status=" + getStatus() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", epic.id='" + epic + '\'' +
                ", startTime='" + getStartTime() + '\'' +
                ", duration='" + getDuration() + '\'' +
                ", endTime='" + getEndTime() + '\'' +
                '}';
    }

    @Override
    public boolean equalsByAllFields(Object o) {
        return super.equalsByAllFields(o) && Objects.equals(epic, ((Subtask) o).epic);
    }

    @Override
    public TaskTypes getTaskType() {
        return TaskTypes.SUBTASK;
    }
}
