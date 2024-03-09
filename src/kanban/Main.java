package kanban;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        Task task1 = new Task("Прогулка", "Необходимо пройти больше 10 000 шагов.");
        taskManager.addTask(task1);

        Task task2 = new Task();
        task2.name = "Помыть посуду";
        taskManager.addTask(task2);

        Epic epic1 = new Epic("Спринт 4", "Пройти полностью 4-й спринт на Яндекс.Практикум.");
        taskManager.addEpic(epic1);
        Subtask subtask1 = new Subtask("Теория", "Изучить всю теорию и пройти все задачи.", epic1);
        taskManager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("Финальное задание", "Сдать финальное задание спринта 4.", epic1);
        taskManager.addSubtask(subtask2);

        Epic epic2 = new Epic();
        epic2.name = "Покупки";
        epic2.description = "Купить еду и моющие средства.";
        Subtask subtask3 = new Subtask(epic2);
        subtask3.name = "Список покупок";
        subtask3.description = "Составить подробный список покупок.";
        taskManager.addEpic(epic2);
        taskManager.addSubtask(subtask3);

        System.out.println("----Вывод 1-----");
        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getEpics());
        System.out.println(taskManager.getSubtasks());

        System.out.println("----Вывод 2-----");
        Task updateTask1 = new Task(task1.getId(), TaskStatus.DONE, task1.name, task1.description);
        taskManager.updateTask(updateTask1);

        Subtask updateSubtask1 = new Subtask(subtask1.getId(), TaskStatus.DONE, subtask1.name, subtask1.description, epic2);
        taskManager.updateSubtask(updateSubtask1);

        Epic updateEpic1 = new Epic(epic1.getId(), epic1.name + "!", epic1.description);
        taskManager.updateEpic(updateEpic1);

        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getEpics());
        System.out.println(taskManager.getSubtasks());

        System.out.println("----Вывод 3-----");
        taskManager.deleteTask(task2.getId());
        taskManager.deleteSubtask(subtask2.getId());
        taskManager.deleteEpic(epic1.getId());

        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getEpics());
        System.out.println(taskManager.getSubtasks());

        System.out.println(taskManager.getEpicSubtasks(epic2));
    }
}
