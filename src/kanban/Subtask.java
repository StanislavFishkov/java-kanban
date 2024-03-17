package kanban;

public class Subtask extends Task {
    public final Integer epic;

    public Subtask(Epic epic) {
        super();
        this.epic = epic.getId();
    }

    public Subtask(String name, String description, Epic epic) {
        super(name, description);
        this.epic = epic.getId();
    }

    public Subtask(int id, TaskStatus status, String name, String description, Epic epic) {
        super(id, status, name, description);
        this.epic = epic.getId();
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + getId() +
                ", status=" + getStatus() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", epic.id='" + epic + '\'' +
                '}';
    }

}
