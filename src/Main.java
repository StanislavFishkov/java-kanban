import kanban.exception.NotFoundException;
import kanban.model.*;
import kanban.service.*;

import java.time.Duration;
import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();

        System.out.println("History (size " + taskManager.getHistory().size() + "): " + taskManager.getHistory());

        Task task1 = new Task("Прогулка", "Необходимо пройти больше 10 000 шагов.");
        taskManager.addTask(task1);

        Task task2 = new Task();
        task2.setName("Помыть посуду");
        taskManager.addTask(task2);
        taskManager.getTask(task2.getId());

        Epic epic1 = new Epic("Спринт 4", "Пройти полностью 4-й спринт на Яндекс.Практикум.");
        taskManager.addEpic(epic1);
        Subtask subtask1 = new Subtask("Теория", "Изучить всю теорию и пройти все задачи.", epic1.getId());
        taskManager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("Финальное задание", "Сдать финальное задание спринта 4.", epic1.getId(),
                LocalDateTime.of(2024, 1, 10, 10, 0), Duration.ofMinutes(840));
        taskManager.addSubtask(subtask2);

        taskManager.getTask(task2.getId());
        System.out.println("\nHistory (size " + taskManager.getHistory().size() + "): " + taskManager.getHistory());

        Epic epic2 = new Epic();
        epic2.setName("Покупки");
        epic2.setDescription("Купить еду и моющие средства.");
        taskManager.addEpic(epic2);
        Subtask subtask3 = new Subtask(epic2.getId());
        subtask3.setName("Список покупок");
        subtask3.setDescription("Составить подробный список покупок.");
        taskManager.addSubtask(subtask3);

        System.out.println("----Вывод 1-----");
        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getEpics());
        System.out.println(taskManager.getSubtasks());
        System.out.println(taskManager.getEpicSubtasks(epic1));

        System.out.println("\nHistory (size " + taskManager.getHistory().size() + "): " + taskManager.getHistory());

        System.out.println("----Вывод 2-----");
        Task updateTask1 = new Task(task1.getId(), TaskStatus.DONE, task1.getName(), task1.getDescription());
        taskManager.updateTask(updateTask1);

        Subtask updateSubtask1 = new Subtask(subtask1.getId(), TaskStatus.DONE, subtask1.getName(),
                subtask1.getDescription(), epic1.getId(), LocalDateTime.of(2024, 3, 10, 20, 0),
                Duration.ofMinutes(600));
        taskManager.updateSubtask(updateSubtask1);

        Epic updateEpic1 = new Epic(epic1.getId(), epic1.getName() + "!", epic1.getDescription());
        taskManager.updateEpic(updateEpic1);

        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getEpics());
        System.out.println(taskManager.getSubtasks());
        System.out.println(taskManager.getEpicSubtasks(epic1));

        System.out.println("\nHistory (size " + taskManager.getHistory().size() + "): " + taskManager.getHistory());
        System.out.println("Ordered list: " + taskManager.getPrioritizedTasks());

        System.out.println("----Вывод 3-----");
        Epic epic = taskManager.getEpic(epic2.getId());
        epic.setSubtasks(null);
        taskManager.updateEpic(epic);

        try {
            taskManager.deleteTask(null);
        } catch (NotFoundException exception) {
            System.out.println(exception.getMessage());
        }
        taskManager.deleteTask(task2.getId());
        taskManager.deleteSubtask(subtask2.getId());
        taskManager.deleteEpic(epic1.getId());

        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getEpics());
        System.out.println(taskManager.getSubtasks());

        System.out.println(taskManager.getEpicSubtasks(epic2));

        System.out.println("\nHistory (size " + taskManager.getHistory().size() + "): " + taskManager.getHistory());
        System.out.println("Ordered list: " + taskManager.getPrioritizedTasks());

        System.out.println("----Вывод 4-----");
        taskManager.deleteAllTasks();
        System.out.println("History (size " + taskManager.getHistory().size() + "): " + taskManager.getHistory());
        System.out.println("Ordered list: " + taskManager.getPrioritizedTasks());

        System.out.println("----Вывод 5-----");
        taskManager.deleteAllSubtasks();
        System.out.println("History (size " + taskManager.getHistory().size() + "): " + taskManager.getHistory());
        System.out.println("Ordered list: " + taskManager.getPrioritizedTasks());

        System.out.println("----Вывод 6-----");
        taskManager.deleteAllEpics();
        System.out.println("History (size " + taskManager.getHistory().size() + "): " + taskManager.getHistory());
        System.out.println("Ordered list: " + taskManager.getPrioritizedTasks());
    }
}
