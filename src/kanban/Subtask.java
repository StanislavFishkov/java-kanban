package kanban;

public class Subtask extends Task {
    public final Epic epic;

    public Subtask(Epic epic) {
        super();
        this.epic = epic;
    }

    public Subtask(String name, String description, Epic epic) {
        super(name, description);
        this.epic = epic;
    }

    public Subtask(int id, TaskStatus status, String name, String description, Epic epic) {
        super(id, status, name, description);
        this.epic = epic;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + getId() +
                ", status=" + getStatus() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", epic.id='" + epic.getId() + '\'' +
                '}';
    }

}
