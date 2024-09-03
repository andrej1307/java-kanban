import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> taskList;
    private final Map<Integer, Epic> epicList;
    private final Map<Integer, Subtask> subtaskList;
    private Integer idMain = 0;
    private final HistoryManager viewHistory = Managers.getDefaultHistory();

    // Инициализируем переменные в конструкторе
    public InMemoryTaskManager() {
        taskList = new HashMap<>();
        epicList = new HashMap<>();
        subtaskList = new HashMap<>();
    }

    // Метод добавления новой задачи
    @Override
    public int addNewTask(Task newTask) {
        if (newTask == null) {
            return -1;
        }
        Integer id = idMain++;
        newTask.setId(id);
        taskList.put(id, newTask);
        return id;
    }

    // Метод добавления нового эпика
    @Override
    public int addNewEpic(Epic newEpic) {
        if (newEpic == null) {
            return -1;
        }
        Integer id = idMain++;
        newEpic.setId(id);
        epicList.put(id, newEpic);
        return id;
    }

    // Метод добавления новой подзадачи
    @Override
    public int addNewSubtask(Subtask newSubtask) {
        if (newSubtask == null) {
            return -1;
        }
        Epic epic = epicList.get(newSubtask.getEpicId());
        if (epic == null) {
            return -2;
        }
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
/*        if (!taskList.containsKey(id)) {
            return -1;
        } */
        taskList.put(id, task);
        return id;
    }

    // Метод обновления эпика
    @Override
    public int updateEpic(Epic newEpic) {
        int id = newEpic.getId();
/*        if (!epicList.containsKey(id)) {
            return -1;
        } */
        epicList.put(id, newEpic);
        newEpic.reloadSubtakList(getEpic(id).getSubtasks());
        setStatusEpic(id);
        return id;
    }

    /**
     * Обновление объекта Subtask.
     * проверяем _________________
     * _ существование соответствующего эпика. Если не найдены, то возвращаем  код меньше 0.
     * Если и эпик и подзадача существуют заменяем объект подзадачи на новый
     *
     * @param newSubtask - идентификатор объекта, содержащий новую информацию
     * @return - id обновленно подзадачи, или меньше нуля если произошла ошибка
     */
    @Override
    public int updateSubtask(Subtask newSubtask) {
        int id = newSubtask.getId();
/*        if (!subtaskList.containsKey(id)) {
            return -1;
        }  */
        int epicId = newSubtask.getEpicId();
        if (!epicList.containsKey(epicId)) {
            return -2;
        }
        subtaskList.put(id, newSubtask);
        setStatusEpic(epicId);
        return id;
    }

    /**
     * Пересчет статуса эпика по указанному идентификатору
     *
     * @param epicId- идентификатор объекта, содержащий новую информацию
     */
    private void setStatusEpic(Integer epicId) {
        if (!epicList.containsKey(epicId)) {
            return;
        }
        Epic epic = epicList.get(epicId);
        if (epic.getSubtasks().isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }
        int countNew = 0;
        int countInProgress = 0;
        int countDone = 0;
        for (Integer idSubtask : epic.getSubtasks()) {
            TaskStatus status = subtaskList.get(idSubtask).getStatus();
            if (status == TaskStatus.NEW) {
                countNew++;
            }
            if (status == TaskStatus.IN_PROGRESS) {
                countInProgress++;
            }
            if (status == TaskStatus.DONE) {
                countDone++;
            }
        }

        if (countNew > 0 && countInProgress == 0 && countDone == 0) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        if (countNew == 0 && countInProgress == 0 && countDone > 0) {
            epic.setStatus(TaskStatus.DONE);
            return;
        }

        epic.setStatus(TaskStatus.IN_PROGRESS);
    }


    @Override
    public void removeTask(Integer taskId) {
        taskList.remove(taskId);
        viewHistory.remove(taskId);
    }

    /**
     * Удаление эпика и всех связанных с ним подзадач
     *
     * @param epicId- идентификатор объекта
     */
    @Override
    public void removeEpic(Integer epicId) {
        if (!epicList.containsKey(epicId)) {
            return;
        }
        for (Integer idSubtask : epicList.get(epicId).getSubtasks()) {
            subtaskList.remove(idSubtask);
            viewHistory.remove(idSubtask);
        }
        epicList.remove(epicId);
        viewHistory.remove(epicId);
    }

    /**
     * Удаление подзадачи по идентификатору
     * Удаляем предварительно из спика соответствующего эпика
     * и из общего списка позадач.
     *
     * @param subtaskId - идентификатор подзадачи
     */
    @Override
    public void removeSubtask(Integer subtaskId) {
        Integer epicId = subtaskList.get(subtaskId).getEpicId();
        epicList.get(epicId).removeSubtask(subtaskId);
        subtaskList.remove(subtaskId);
        viewHistory.remove(subtaskId);
        setStatusEpic(epicId);
    }

    @Override
    public List<Task> getTaskList() {
        return new ArrayList<>(taskList.values());
    }

    @Override
    public List<Epic> getEpicList() {
        return new ArrayList<>(epicList.values());
    }

    @Override
    public ArrayList<Subtask> getSubtaskList() {
        return new ArrayList<>(subtaskList.values());
    }

    // Удаление всех объектов класса Task
    @Override
    public void removeAllTasks() {
        for (Task task : taskList.values()) {
            viewHistory.remove(task.getId());
        }
        taskList.clear();
    }

    // Удаление всех объектов класса Epic
    @Override
    public void removeAllEpics() {
        for (Epic epic : epicList.values()) {
            for (int subtaskId : epic.getSubtasks()) {
                viewHistory.remove(subtaskId);
            }
            viewHistory.remove(epic.getId());
        }
        epicList.clear();
        subtaskList.clear();
    }

    // Удаление всех объектов класса Subtask
    @Override
    public void removeAllSubtasks() {
        for (Epic epic : getEpicList()) {
            epic.removeAllSubtasks();
        }
        for (Subtask subtask : subtaskList.values()) {
            viewHistory.remove(subtask.getId());
        }
        subtaskList.clear();
    }

    /**
     * Получение списка всех подзадач длля заданного эпика
     *
     * @param epicId - идентификатор эпика
     * @return - список подзадач
     */
    @Override
    public List<Subtask> getSubtasksByEpic(Integer epicId) {
        Epic epic = epicList.get(epicId);
        List<Subtask> subtasks = new ArrayList<>();
        for (Integer subtaskId : epic.getSubtasks()) {
            subtasks.add(subtaskList.get(subtaskId));
        }
        return subtasks;
    }

    /**
     * Метод просмотра использованных задач
     *
     * @return - возвращает список использованных объектов
     */
    @Override
    public List<Task> getHistory() {
        return viewHistory.getHistory();
    }

    public void clear() {
        removeAllTasks();
        removeAllEpics();
        idMain = 0;
    }

    /**
     * Пересчет идентификатора задач после загрузки данных из файла
     */
    public void resetMainId() {
        int maxId = 0;
        for (int i : taskList.keySet()) {
            if (i > maxId) maxId = i;
        }
        for (int i : epicList.keySet()) {
            if (i > maxId) maxId = i;
        }
        for (int i : subtaskList.keySet()) {
            if (i > maxId) maxId = i;
        }
        idMain = maxId + 1;
    }
}
