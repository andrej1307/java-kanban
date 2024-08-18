import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

public class Main {
    // создаем объект менеджера задач при помощи утилитарного класса
    public static TaskManager manager = Managers.getDefault();

    public static void main(String[] args) {

        // добавляем простую задачу
        int taskId = manager.addNewTask(new Task("Выпить кофе.",
                "Кофе с молоком, 1 ложка сахара."));

        // добавляем простую задачу
        taskId = manager.addNewTask(new Task("Прочитать новости."));

        // добавляем эпик
        int epicId = manager.addNewEpic(new Epic("Выполнить иотговый проект \"Спринт №6\"."));

        // добавляем подзадачу к эпику
        int subtaskId = manager.addNewSubtask(new Subtask(epicId,
                "Создать ветку в GIT."));

        // добавляем подзадачу к эпику
        subtaskId = manager.addNewSubtask(new Subtask(epicId, "Изменить программу java-kanban",
                "Написать программу истории задач без дублей."));

        // добавляем подзадачу к эпику
        subtaskId = manager.addNewSubtask(new Subtask(epicId,
                "Сформировать \"Pull requests\" и отправить ревьюверу."));

        // добавляем эпик
        epicId = manager.addNewEpic(new Epic("Выполнить \"Спринт №7\" практикума."));

        printAllTasks(manager);

        manager.getTask(0).setStatus(TaskStatus.DONE);
        System.out.println("\nИзменили статус задачи : " + manager.getTask(0).toString());
        printHistory(manager);


        manager.getTask(1).setStatus(TaskStatus.IN_PROGRESS);
        System.out.println("\nИзменили статус задачи : " + manager.getTask(1).toString());
        printHistory(manager);

        Subtask subtask = manager.getSubtasks(3);
        subtask.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask);
        System.out.println("\nИзменили статус подзадачи : " + manager.getSubtasks(3).toString());
        printHistory(manager);

        subtask = manager.getSubtasks(4);
        subtask.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask);
        System.out.println("\nИзменили статус подзадачи : " + manager.getSubtasks(4).toString());
        printHistory(manager);

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

        manager.removeEpic(2);
        System.out.println("\nУдалили эпик с подзадачами.");
        printAllTasks(manager);
        printHistory(manager);

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