/**
 * Программа демонстрации рабботы методов класса TaskManager
 */
import tasks.*;

public class Main {
    // создаем объект менеджера задач
    public static TaskManager taskManager = new TaskManager();

    public static void main(String[] args) {

        // добавляем простую задачу
        taskManager.addNewTask(new Task("Выпить кофе",
                "Кофе с молоком, 1 ложка сахара."));

        // добавляем простую задачу
        taskManager.addNewTask(new Task("Прочитать новости"));

        // объект "Epic" нужен для привязки подзадач
        Epic epic = new Epic("Выполнить спринт №4 \"Практикума\".");
        // добавляем эпик
        taskManager.addNewEpic(epic);

        // добавляем подзадачу к эпику
        taskManager.addNewSubtask(new Subtask(epic.getId(),"Изучить теорию.",
                "Изучить объекты и методы класса Object."));

        // добавляем подзадачу к эпику
        taskManager.addNewSubtask(new Subtask(epic.getId(), "подготовить итоговый проект.",
                "Написать программу итогового проекта."));

        printAllTasks();

        taskManager.getTaskById(1).setStatus(TaskStatus.DONE);
        taskManager.getTaskById(2).setStatus(TaskStatus.IN_PROGRESS);

        Subtask subtask = taskManager.getSubtasksById(4);
        subtask.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask);

        subtask = taskManager.getSubtasksById(5);
        subtask.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtask);

        printAllTasks();

        taskManager.addNewSubtask(new Subtask(3, "Устранить замечания к проекту"));

        printAllTasks();

        subtask = new Subtask(epic.getId(), "Повторить теорию \"спринт 4\".");
        subtask.setId(4);
        taskManager.updateSubtask(subtask);

        printAllTasks();

        taskManager.removeTaskById(1);
        taskManager.removeSubtaskById(5);

        printAllTasks();

        subtask.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask);
        printAllTasks();

        taskManager.removeAllSubtasks();
        printAllTasks();
    }

    /**
     * Вывод на экран списка всех задач
     */
    public static void printAllTasks() {
        System.out.println("\nВ списке задач " + taskManager.getNumberOfObjects() + " записей.");
        for (Task t : taskManager.getTaskList()) {
            System.out.println(t.toString());
        }
        for (Epic e : taskManager.getEpicList()) {
            System.out.println(e.toString());
        }
        for (Subtask s : taskManager.getSubtaskList()) {
            System.out.println(s.toString());
        }
    }

}
