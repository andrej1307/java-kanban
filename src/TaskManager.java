import tasks.*;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private HashMap<Integer, Task> taskList;
    private Integer Id;

    public TaskManager() {
        Id = 100000;
        taskList = new HashMap<>();
    }

    // Метод генерации очередного идентификатора задачи
    private Integer nextID() {
        return ++Id;
    }

    // Метод добавления новой задачи
    public void addTask(Task newTask) {
        Integer ID = nextID();
        newTask.setID(ID);
        taskList.put(ID, newTask);
    }

    // Метод выдачи массива строк описания объектов в коллекции
    // в качестве параметров фильтра принимаются: TASK, EPIC, SUBTASK, ALL
    public ArrayList<String> getStringsAllTasks(TaskFilter filter) {
        if (taskList.isEmpty()){
            return null;
        }
        ArrayList<String> recordSet = new ArrayList<>();
        for (Integer index : taskList.keySet()) {
            switch (filter) {
                case TASK:
                    if(taskList.get(index).getClass().getCanonicalName().equals("tasks.Task")) {
                        recordSet.add(getStringFromObject(taskList.get(index)));
                    }
                    break;
                case EPIC:
                    if(taskList.get(index).getClass().getCanonicalName().equals("tasks.Epic")) {
                        recordSet.add(getStringFromObject(taskList.get(index)));
                    }
                    break;
                case SUBTASK:
                    if(taskList.get(index).getClass().getCanonicalName().equals("tasks.Subtask")) {
                        recordSet.add(getStringFromObject(taskList.get(index)));
                    }
                    break;
                case ALL:
                default:
                    recordSet.add(getStringFromObject(taskList.get(index)));
                break;
            }
        }
        return recordSet;
    }

    // Метод выдачи строки информации об объекте с учетом класса задачи
    public String getStringFromObject(Task task) {
        if (task.getClass().getCanonicalName().equals("tasks.Task")) {
            return task.toString();
        } else if (task.getClass().getCanonicalName().equals("tasks.Epic")) {
            Epic epic = (Epic) task;
            return epic.toString();
        } else if (task.getClass().getCanonicalName().equals("tasks.Subtask")) {
            Subtask s = (Subtask) task;
            return s.toString();
        }
        return "Объект не распознан." + task.getClass().getCanonicalName();
    }

    // Метод получения объекта по индексу (любого объекта, в том числе и наследованного от Task)
    public Task getRecordBuId(Integer Id) {
        return taskList.get(Id);
    }

    // Метод получения объекта класса Task из коллекции по индексу
    // В озвращает ссылку на объект если найден, иначе null
    public Task getTaskById(Integer Id) {
        Task record = taskList.get(Id);
        if (record == null) {
            return null;
        } else if (!record.getClass().getCanonicalName().equals("tasks.Task")) {
            return null;
        }
        return record;
    }

    // Метод получения объекта класса Epic из коллекции по индексу
    // В озвращает ссылку на объект если найден, иначе null
    public Epic getEpicById(Integer Id) {
        Task record = getRecordBuId(Id);
        if (record == null) {
            return null;
        } else if (!record.getClass().getCanonicalName().equals("tasks.Epic")) {
            return null;
        }
        return (Epic)record;
    }

    // Метод получения объекта класса Subtask из коллекции по индексу
    // В озвращает ссылку на объект если найден, иначе null
    public Subtask getSubtaskById(Integer Id) {
        Task record = getRecordBuId(Id);
        if (record == null) {
            return null;
        } else if (!record.getClass().getCanonicalName().equals("tasks.Subtask")) {
            return null;
        }
        return (Subtask)record;
    }

    // Метод обновления задачи
    public void updateTask(Task task) {
        taskList.put(task.getID(), task);
    }

    /**
     * Удаление задачи по идентификатору (любой задачи: Task, Epic, Subtask).
     * Если идентификатор соответствует эпику, то удаляем все его сабтаски.
     * Если идентификатор указывает на сабтаск, то предварительно удаляем его
     * из списка подзадач эпика
     *
     * @param id - идентификатор записи в списке менеджера задач
     */
   public void removeTaskByID(Integer id) {
        Task record = getRecordBuId(id);
        if (record == null) {
            return;
        }
        Epic epic;
        if (record.getClass().getCanonicalName().equals("Epic")) {
            epic = (Epic)record;
            ArrayList<Subtask> subtasks = epic.getSubtasks();
            for (Subtask s : subtasks) {
                // Удаляем все сабтаски эпика
                taskList.remove(s.getID());
            }
        } else if (record.getClass().getCanonicalName().equals("tasks.Subtask")) {
            Subtask subtask = (Subtask) record;
            epic = subtask.getEpic();
            epic.removeSubtask(subtask); // удаляем подзадачу из списка эпика
        }
        taskList.remove(id);
    }

    public ArrayList<Task> getTaskList(TaskFilter filter) {
        String maket;

        switch (filter) {
            case TASK:
                maket = "tasks.Task";
                break;
            case EPIC:
                maket = "tasks.Epic";
                break;
            case SUBTASK:
                maket = "tasks.Subtask";
                break;
            case ALL:
            default:
                maket ="";
                break;
        }
        ArrayList<Task> tasks = new ArrayList<>();
        for (Integer Id : taskList.keySet()) {
            if (maket.isEmpty()) {
                tasks.add(taskList.get(Id));
            } else {
                if (taskList.get(Id).getClass().getCanonicalName().equals(maket)) {
                    tasks.add(taskList.get(Id));
                }
            }
        }
        return tasks;
    }

    // Удаление всех объектов класса Task
    public void removeAllTasks() {
        ArrayList<Task> tasks = getTaskList(TaskFilter.TASK);
        for (Task task : tasks) {
            removeTaskByID(task.getID());
        }
    }

    // Удаление всех объектов класса Epic
    public void removeAllEpics() {
        ArrayList<Task> tasks = getTaskList(TaskFilter.EPIC);
        for (Task task : tasks) {
            removeTaskByID(task.getID());
        }
    }

    // Удаление всех объектов класса Subtask
    public void removeAllSubtasks() {
        ArrayList<Task> tasks = getTaskList(TaskFilter.SUBTASK);
        for (Task task : tasks) {
            removeTaskByID(task.getID());
        }
    }

    // Очистка списка задач
    public void clearTaskList() {
        taskList.clear();;
    }

    // получение всех сабтасков для эпика по известному объекту
    public ArrayList<Subtask> getSubtasksByEpic(Epic epic) {
        return epic.getSubtasks();
    }

    // получение всех сабтасков для эпика по известному идентификатору
    public ArrayList<Subtask> getSubtasksByEpic(Integer Id) {
        return getEpicById(Id).getSubtasks();
    }

    public int getTaskListSize() {
        return taskList.size();
    }
}
