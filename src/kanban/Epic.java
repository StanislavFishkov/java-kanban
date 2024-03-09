package kanban;

import java.util.ArrayList;

public class Epic extends Task {
    protected ArrayList<Integer> subtasks;

    public Epic() {
        super();
        this.subtasks = new ArrayList<>();
    }

    public Epic(String name, String description) {
        super(name, description);
        this.subtasks = new ArrayList<>();
    }

    public Epic(int id, String name, String description) {
        super(id, TaskStatus.NEW, name, description);
        this.subtasks = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + super.getId() +
                ", status=" + getStatus() +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", subtasks.size='" + subtasks.size() + '\'' +
                '}';
    }

}
