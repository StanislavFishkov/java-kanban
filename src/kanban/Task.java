package kanban;

import java.util.Objects;

public class Task {
    private Integer id;
    private TaskStatus status;
    private String name;
    private String description;

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

    public Task(int id, TaskStatus status, String name, String description) {
        this.id = id;
        this.status = status;
        this.name = name;
        this.description = description;
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

    protected void setId(int id) {
        this.id = id;
    }

    protected void setStatus(TaskStatus status) {
        this.status = status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
