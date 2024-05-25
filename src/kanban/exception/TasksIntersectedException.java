package kanban.exception;

public class TasksIntersectedException extends RuntimeException {
    public TasksIntersectedException(String message) {
        super(message);
    }

    public TasksIntersectedException(Throwable cause) {
        super(cause);
    }
}
