import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

public class Main {
    // создаем объект менеджера задач при помощи утилитарного класса
    public static TaskManager manager = Managers.getDefault();

    public static void main(String[] args) {

        // Список задач загружениз файла

        System.out.println("\nПроверяем статус эпика : " + manager.getEpic(2).toString());
        printHistory(manager);

        System.out.println("\nПроверяем статус эпика : " + manager.getEpic(6).toString());
        printHistory(manager);

        System.out.println("\nПроверяем статус задачи : " + manager.getTask(0).toString());
        printHistory(manager);

        System.out.println("\nПроверяем статус подзадачи : " + manager.getSubtasks(5).toString());
        printHistory(manager);

        System.out.println("\nПроверяем статус подзадачи : " + manager.getSubtasks(4).toString());
        printHistory(manager);

        printAllTasks(manager);
    }

    /**
     * Вывод на экран списка всех задач
     *
     * @param manager - менеджер задач
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
    }

    /**
     * Распечатка истории обращения к задачам
     *
     * @param manager - менеджер задач
     */
    public static void printHistory(TaskManager manager) {
        System.out.println("История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task.toString());
        }
    }

}