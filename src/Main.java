/**
 * Программа демонстрации рабботы методов класса TaskManager
 */
import tasks.*;

public class Main {
    // создаем объект менеджера задач при помощи утилитарного класса
    public static TaskManager manager = Managers.getDefault();

    public static void main(String[] args) {

        // добавляем простую задачу
        int taskId = manager.addNewTask(new Task("Выпить кофе",
                "Кофе с молоком, 1 ложка сахара."));

        // добавляем простую задачу
        taskId = manager.addNewTask(new Task("Прочитать новости"));

        // добавляем эпик
        int epicId = manager.addNewEpic(new Epic("Выполнить спринт №4 \"Практикума\"."));

        // добавляем подзадачу к эпику
        int subtaskId = manager.addNewSubtask(new Subtask(epicId,"Изучить теорию.",
                "Изучить объекты и методы класса Object."));

        // добавляем подзадачу к эпику
        subtaskId = manager.addNewSubtask(new Subtask(epicId, "подготовить итоговый проект.",
                "Написать программу итогового проекта."));

        printAllTasks(manager);

        manager.getTask(0).setStatus(TaskStatus.DONE);
        Task task = manager.getTask(0);
        manager.getTask(1).setStatus(TaskStatus.IN_PROGRESS);
        task = manager.getTask(1);

        System.out.println("\nПоменяли статус у двух задач.");
        printAllTasks(manager);

        Subtask subtask = manager.getSubtasks(3);
        subtask.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask);
        subtask = manager.getSubtasks(3);

        subtask = manager.getSubtasks(4);
        subtask.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask);
        subtask = manager.getSubtasks(4);

        System.out.println("\nПоменяли статус у подзадач 3, 4.");
        printAllTasks(manager);

        subtaskId = manager.addNewSubtask(new Subtask(2, "Устранить замечания к проекту"));

        System.out.println("\nДобавили 1 подзадачу");
        printAllTasks(manager);

        manager.removeTask(0);
        manager.removeSubtask(4);

        System.out.println("\nУдалили 1 задачу и 1 подзадачу");
        printAllTasks(manager);

        Epic e = manager.getEpic(2);

        manager.removeAllSubtasks();
        e = manager.getEpic(2);

        System.out.println("\nУдалили все подзадачи");
        printAllTasks(manager);

        System.out.println("\n--");
        task = manager.getTask(1);
        printAllTasks(manager);
    }

    /**
     * Вывод на экран списка всех задач
     */
    public static void printAllTasks(TaskManager manager) {
        System.out.println("\nВ списке задач " + manager.getNumberOfObjects() + " записей.");
        System.out.println("Задачи:");
        for (Task t : manager.getTaskList()) {
            System.out.println(t.toString());
        }
        System.out.println("Эпики:");
        for (Epic e : manager.getEpicList()) {
            System.out.println(e.toString());
            for (Subtask s : manager.getSubtasksByEpic(e.getId())) {
                System.out.println("-->" + s.toString());
            }
        }
        System.out.println("Подзадачи:");
        for (Subtask s : manager.getSubtaskList()) {
            System.out.println(s.toString());
        }
        System.out.println("История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task.toString());
        }
    }

}
