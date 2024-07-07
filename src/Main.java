/**
 * Программа демонстрации методов класса TaskManager
 */
import java.util.ArrayList;

public class Main {
    // создаем объект менеджера задач
    public static TaskManager taskManager = new TaskManager();

    public static void main(String[] args) {
        // добавляем простую задачу
        taskManager.addTask(new Task("Выпить кофе",
                "Кофе с молоком, 1 ложка сахара."));

        // добавляем простую задачу
        taskManager.addTask(new Task("Прочитать новости"));

        // объект "Epic" нужен для привязки подзадач
        Epic epic = new Epic("Выполнить спринт №4 \"Практикума\".");
        // добавляем эпик
        taskManager.addTask(epic);

        Subtask subtask = new Subtask(epic, "Изучить теорию.",
                "Изучить объекты и методы класса Object.");
        subtask.setStatus(TaskStatus.DONE);
        // добавляем подзадачу к эпику
        taskManager.addTask(subtask);

        subtask = new Subtask(epic, "подготовить итоговый проект.",
                "Написать программу итогового проекта.");
        subtask.setStatus(TaskStatus.IN_PROGRESS);
        // добавляем подзадачу к эпику
        taskManager.addTask(subtask);

        printAllTasks();

        int id = 100002;
        Task task = taskManager.getTaskById(id);
        System.out.println("\nВыбираем задачу по ID="+ id + "\n" + task.toString());

        id = 100003;
        epic = taskManager.getEpicById(id);
        System.out.println("Выбираем эпик по ID="+ id + "\n" + epic.toString());

        id = 100005;
        subtask = taskManager.getSubtaskById(id);
        System.out.println("Выбираем подзадачу по ID=" + id + "\n" + subtask.toString());

        taskManager.addTask(new Task("Задача №3"));
        taskManager.addTask(new Task("Задача №4"));
        taskManager.addTask(new Epic("Ещё один эпик"));
        epic = taskManager.getEpicById(100008);
        taskManager.addTask(new Subtask(epic, "подзадача №1."));
        taskManager.addTask(new Subtask(epic, "подзадача №1."));

        System.out.println("\nВсе задачи Task:");
        ArrayList<Task> tasks = taskManager.getTaskList(TaskFilter.TASK);
        for (Task t : tasks) {
            System.out.println(t.toString());
        }

        System.out.println("\nВсе задачи Epic:");
        tasks = taskManager.getTaskList(TaskFilter.EPIC);
        for (Task t : tasks) {
            System.out.println(((Epic)t).toString());
        }

        System.out.println("\nВсе подзадачи Subtask:");
        tasks = taskManager.getTaskList(TaskFilter.SUBTASK);
        for (Task t : tasks) {
            System.out.println(((Subtask)t).toString());
        }

        id = 100008;
        epic = taskManager.getEpicById(id);
        System.out.println("\nВсе подзадачи эпика " + epic.getID());
        ArrayList<Subtask> subtasks = epic.getSubtasks();
        if(subtasks != null) {
            for (Subtask s : subtasks) {
                System.out.println(s.toString());
            }
        }

        id = 100007;
        task = taskManager.getTaskById(id);
        System.out.println("\nВыбираем задачу по ID="+ id + "\n" + task.toString());
        System.out.println("Обновляем выбранную задачу");
        task.setDescription("эта задача обновлена.");
        task.setStatus(TaskStatus.DONE);
        taskManager.updateTask(task);
        task = taskManager.getTaskById(id);
        System.out.println(task.toString());

        printAllTasks();

        id = 100009;
        System.out.println("\nУдаляем задачу ID=" + id);
        taskManager.removeTaskByID(id);
        printAllTasks();

        System.out.println("\nУдаляем все Task!");
        taskManager.removeAllTasks();
        printAllTasks();

        System.out.println("\nУдаляем все Subask!");
        taskManager.removeAllSubtasks();
        printAllTasks();

        System.out.println("\nУдаляем все Epic!");
        taskManager.removeAllEpics();
        printAllTasks();

    }

    public static void printAllTasks() {
        System.out.println("\nВ списке задач " + taskManager.getTaskNumber() + " записей.");
        ArrayList<String> taskTextList = taskManager.getStringsAllTasks(TaskFilter.ALL);
        if (taskTextList == null) return;
        for (String str : taskTextList) {
            System.out.println(str);
        }

    }

}
