import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;

public class Main {
    // создаем объект менеджера задач при помощи утилитарного класса
    public static TaskManager manager = Managers.getDefault();

    public static void main(String[] args) {

        // Список задач загружениз файла
        printAllTasks(manager);

        System.out.println("\nПроверяем статус эпика : " + manager.getEpic(2).toString());

        System.out.println("\nПроверяем статус эпика : " + manager.getEpic(6).toString());

        System.out.println("\nПроверяем статус задачи : " + manager.getTask(1).toString());

        System.out.println("\nПроверяем статус подзадачи : " + manager.getSubtask(5).toString());

        System.out.println("\nПроверяем статус подзадачи : " + manager.getSubtask(4).toString());
        printHistory(manager);

        // Задаем имя файла для сохранения измененийв списке задач
        ((FileBackedTaskManager) manager).setSaveFileName(".\\data\\tasksSave.csv");

        manager.addNewTask(new Task("Задача №4", "****",
                LocalDateTime.of(2024, 12, 31, 11, 45),
                Duration.ofMinutes(15)));

        System.out.println("\n******* Сортировка задач по времени запуска. **********************");
        manager.getPrioritizedTasks().stream()
                .forEach((Task t) -> System.out.println(t.toString()));
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