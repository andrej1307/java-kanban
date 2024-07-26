import tasks.*;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {
    private HashMap<Integer, Task> taskList;
    private HashMap<Integer, Epic> epicList;
    private HashMap<Integer, Subtask> subtaskList;
    private Integer idMain = 0;
    private HistoryManager viewHistory = Managers.getDefaultHistory();

    // Инициализируем переменные в конструкторе
    public InMemoryTaskManager() {
        taskList = new HashMap<>();
        epicList = new HashMap<>();
        subtaskList = new HashMap<>();
    }

    // Метод добавления новой задачи
    @Override
    public int addNewTask(Task newTask) {
        if (newTask == null) { return -1; }
        Integer id = idMain++;
        newTask.setId(id);
        taskList.put(id, newTask);
        return id;
    }

    // Метод добавления нового эпика
    @Override
    public int addNewEpic(Epic newEpic) {
        if (newEpic == null) { return -1; }
        Integer id = idMain++;
        newEpic.setId(id);
        epicList.put(id, newEpic);
        return id;
    }

    // Метод добавления новоq подзадачи
    @Override
    public int addNewSubtask(Subtask newSubtask) {
        if (newSubtask == null) { return -1; }
        Epic epic = epicList.get(newSubtask.getEpicId());
        if (epic == null) { return -2;}
        Integer id = idMain++;
        newSubtask.setId(id);
        subtaskList.put(id, newSubtask);
        epic.addSubtask(newSubtask.getId());
        setStatusEpic(epic.getId());
        return id;
    }

    // определение общего числа задач всех типов в менеджере
    @Override
    public int getNumberOfObjects() {
        return taskList.size() + epicList.size() + subtaskList.size();
    }

    // Метод получения задачи по индексу
    @Override
    public Task getTask(Integer id) {
        Task task = taskList.get(id);
        viewHistory.add(task);
        return task;
    }

    // Метод получения эпика по индексу
    @Override
    public Epic getEpic(Integer id) {
        Epic epic = epicList.get(id);
        viewHistory.add(epic);
        return epic;
    }
    // Метод получения подзадачи по индексу
    @Override
    public Subtask getSubtasks(Integer id) {
        Subtask s = subtaskList.get(id);
        viewHistory.add(s);
        return s;
    }

    // Метод обновления задачи
    @Override
    public int updateTask(Task task) {
        int id = task.getId();
        if (!taskList.keySet().contains(id)) { return -1; }
        taskList.put(id, task);
        return id;
    }

    // Метод обновления эпика
    @Override
    public int updateEpic(Epic newEpic) {
        int id = newEpic.getId();
        if (!epicList.keySet().contains(id)) { return -1; }
        newEpic.reloadSubtakList(getEpic(id).getSubtasks());
        epicList.put(id, newEpic);
        setStatusEpic(id);
        return id;
    }

    /**
     * Обновление объекта Subtask.
     * проверяем существования подзадачи с указанным идентификатором
     * и существование соответствующего эпика. Если не найдены, то возвращаем 0
     * @param newSubtask
     * @return  - 0 если не найдены соответствующая подзадача и эпик
     */
    @Override
    public int updateSubtask(Subtask newSubtask) {
        int id = newSubtask.getId();
        if (!subtaskList.keySet().contains(id)) { return -1; }
        int epicId = subtaskList.get(id).getEpicId();
        if (!epicList.keySet().contains(epicId)) {
            return 0;
        }
        newSubtask.setEpicId(epicId);
        subtaskList.put(id, newSubtask);
        setStatusEpic(epicId);
        return id;
    }

    /**
     * Пересчет статуса эпика по указанному идентификатору
     * @param epicId
     */
    private void setStatusEpic(Integer epicId) {
        if (!epicList.keySet().contains(epicId)) { return; }
        Epic epic = epicList.get(epicId);
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


    @Override
    public void removeTask(Integer taskId) {
        taskList.remove(taskId);
    }

    /**
     * Удаление эпика и всех связанных с ним подзадач
     * @param epicId
     */
    @Override
    public void removeEpic(Integer epicId) {
        if (!epicList.keySet().contains(epicId)) { return; }
        for (Integer idSubtask : epicList.get(epicId).getSubtasks()) {
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
    @Override
    public void removeSubtask(Integer subtaskId) {
        Integer epicId = subtaskList.get(subtaskId).getEpicId();
        epicList.get(epicId).removeSubtask(subtaskId);
        subtaskList.remove(subtaskId);
        setStatusEpic(epicId);
    }

    @Override
    public ArrayList<Task> getTaskList() {
        ArrayList<Task> tasks = new ArrayList<>(taskList.values());
        return tasks;
    }

    @Override
    public ArrayList<Epic> getEpicList() {
        ArrayList<Epic> epics = new ArrayList<>(epicList.values());
        return epics;
    }

    @Override
    public ArrayList<Subtask> getSubtaskList() {
        ArrayList<Subtask> subtasks = new ArrayList<>(subtaskList.values());
        return subtasks;
    }

    // Удаление всех объектов класса Task
    @Override
    public void removeAllTasks() {
        taskList.clear();
    }

    // Удаление всех объектов класса Epic
    @Override
    public void removeAllEpics() {
        epicList.clear();
        subtaskList.clear();
    }

    // Удаление всех объектов класса Subtask
    @Override
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
    @Override
    public ArrayList<Subtask> getSubtasksByEpic(Integer epicId) {
        Epic epic = epicList.get(epicId);
        ArrayList<Subtask> subtasks = new ArrayList<>();
        for (Integer subtaskId : epic.getSubtasks()) {
            subtasks.add(subtaskList.get(subtaskId));
        }
        return subtasks;
    }

    /**
     * Метод примотра использованных задач
     * @return
     */
    public List<Task> getHistory() {
        return viewHistory.getHistory();
    }

}