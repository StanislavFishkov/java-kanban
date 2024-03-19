package kanban;

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

    public Subtask(int id, TaskStatus status, String name, String description, Integer epic) {
        super(id, status, name, description);
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
                '}';
    }

}
