import tasks.*;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private HashMap<Integer, Task> taskList;
    private HashMap<Integer, Epic> epicList;
    private HashMap<Integer, Subtask> subtaskList;
    private Integer idMain = 0;

    // Инициализируем переменные в конструкторе
    public TaskManager() {
        taskList = new HashMap<>();
        epicList = new HashMap<>();
        subtaskList = new HashMap<>();
    }

    // Метод добавления новой задачи
    public void addNewTask(Task newTask) {
        Integer id = ++idMain;
        newTask.setId(id);
        taskList.put(id, newTask);
    }

    // Метод добавления нового эпика
    public void addNewEpic(Epic newEpic) {
        Integer id = ++idMain;
        newEpic.setId(id);
        epicList.put(id, newEpic);
    }

    // Метод добавления новоq подзадачи
    public void addNewSubtask(Subtask newSubtask) {
        Epic epic = getEpicById(newSubtask.getEpicId());
        if (epic == null) { return; }
        Integer id = ++idMain;
        newSubtask.setId(id);
        subtaskList.put(id, newSubtask);
        epic.addSubtask(newSubtask.getId());
        calculateStatusEpic(epic.getId());
    }

    // определение общего числа задач всех типов в менеджере
    public int getNumberOfObjects() {
        return taskList.size() + epicList.size() + subtaskList.size();
    }

    // Метод получения задачи по индексу
    public Task getTaskById(Integer id) {
        return taskList.get(id);
    }

    // Метод получения эпика по индексу
    public Epic getEpicById(Integer id) {
        return epicList.get(id);
    }
    // Метод получения подзадачи по индексу
    public Subtask getSubtasksById(Integer id) {
        return subtaskList.get(id);
    }

    // Метод обновления задачи
    public int updateTask(Task task) {
        int id = task.getId();
        if (!taskList.keySet().contains(id)) { return 0; }
        taskList.put(id, task);
        return id;
    }

    // Метод обновления эпика
    public int updateEpic(Epic newEpic) {
        int id = newEpic.getId();
        if (!epicList.keySet().contains(id)) { return 0; }
        newEpic.reloadSubtakList(getEpicById(id).getSubtasks());
        epicList.put(id, newEpic);
        calculateStatusEpic(id);
        return id;
    }

    /**
     * Обновление объекта Subtask.
     * проверяем существования подзадачи с указанным идентификатором
     * и существование соответствующего эпика. Если не найдены, то возвращаем 0
     * @param newSubtask
     * @return  - 0 если не найдены соответствующая подзадача и эпик
     */
    public int updateSubtask(Subtask newSubtask) {
        int id = newSubtask.getId();
        if (!subtaskList.keySet().contains(id)) { return 0; }
        int epicId = getSubtasksById(id).getEpicId();
        if (!epicList.keySet().contains(epicId)) {
            return 0;
        }
        newSubtask.setEpicId(epicId);
        subtaskList.put(id, newSubtask);
        calculateStatusEpic(epicId);
        return id;
    }

    /**
     * Пересчет статуса эпика по указанному идентификатору
     * @param epicID
     */
    private void calculateStatusEpic(Integer epicID) {
        if (!epicList.keySet().contains(epicID)) { return; }
        Epic epic = getEpicById(epicID);
        if (epic.getSubtasks().isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }
        int countNew=0;
        int countInProgress=0;
        int countDone=0;
        for (Integer idSubtask: epic.getSubtasks()) {
            TaskStatus status = subtaskList.get(idSubtask).getStatus();
            if (status == TaskStatus.NEW) { countNew++; }
            if (status == TaskStatus.IN_PROGRESS) { countInProgress++; }
            if (status == TaskStatus.DONE) { countDone++; }
        }

        epic.setStatus(TaskStatus.IN_PROGRESS);

        if (countNew > 0 && countInProgress == 0 && countDone == 0) {
            epic.setStatus(TaskStatus.NEW);
        }
        if (countNew == 0 && countInProgress == 0 && countDone > 0) {
            epic.setStatus(TaskStatus.DONE);
        }
    }


    public void removeTaskById(Integer taskId) {
        taskList.remove(taskId);
    }

    /**
     * Удаление эпика и всех связанных с ним подзадач
     * @param epicId
     */
    public void removeEpicById(Integer epicId) {
        if (!epicList.keySet().contains(epicId)) { return; }
        for (Integer idSubtask : getEpicById(epicId).getSubtasks()) {
            subtaskList.remove(idSubtask);
        }
        epicList.remove(epicId);
    }

    /**
     * Удаление подзадачи по идентификатору
     * Удаляем предварительно из спика соответствующего эпика
     * и из общего списка позадач.
     * @param subtaskId
     */
    public void removeSubtaskById(Integer subtaskId) {
        Integer epicId = getSubtasksById(subtaskId).getEpicId();
        getEpicById(epicId).removeSubtask(subtaskId);
        subtaskList.remove(subtaskId);
        calculateStatusEpic(epicId);
    }

    public ArrayList<Task> getTaskList() {
        ArrayList<Task> tasks = new ArrayList<>(taskList.values());
        return tasks;
    }

    public ArrayList<Epic> getEpicList() {
        ArrayList<Epic> epics = new ArrayList<>(epicList.values());
        return epics;
    }

    public ArrayList<Subtask> getSubtaskList() {
        ArrayList<Subtask> subtasks = new ArrayList<>(subtaskList.values());
        return subtasks;
    }

    // Удаление всех объектов класса Task
    public void removeAllTasks() {
        taskList.clear();
    }

    // Удаление всех объектов класса Epic
    public void removeAllEpics() {
        epicList.clear();
        subtaskList.clear();
    }

    // Удаление всех объектов класса Subtask
    public void removeAllSubtasks() {
        for (Epic epic : getEpicList()) {
            epic.removeAllSubtasks();
        }
        subtaskList.clear();
    }

    /**
     * Получение списка всех подзадач длля заданного эпика
     * @param epicId - идентификатор эпика
     * @return       - ArrayList<Subtask> список подзадач
     */
    public ArrayList<Subtask> getSubtasksByEpic(Integer epicId) {
        Epic epic = getEpicById(epicId);
        ArrayList<Subtask> subtasks = new ArrayList<>();
        for (Integer subtaskId : epic.getSubtasks()) {
            subtasks.add(getSubtasksById(subtaskId));
        }
        return subtasks;
    }

}
