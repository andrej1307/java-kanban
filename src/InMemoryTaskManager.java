import exceptions.TaskCrossTimeException;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> taskList;
    private final Map<Integer, Epic> epicList;
    private final Map<Integer, Subtask> subtaskList;
    private Integer idMain = 0;
    private final Map<Task, String> tasksSortedByTime;
    private final HistoryManager viewHistory = Managers.getDefaultHistory();

    // компаратор для упоорядочивания задач по ремени запуска,
    // а при совпадении по возрастанию идентификатора.
    // Задачи с временем null помещаются в начало.
    private final Comparator<Task> taskComparator = Comparator.comparing(Task::getStartTime,
            Comparator.nullsFirst(Comparator.naturalOrder())).thenComparing(Task::getId);

    // Инициализируем переменные в конструкторе
    public InMemoryTaskManager() {
        taskList = new HashMap<>();
        epicList = new HashMap<>();
        subtaskList = new HashMap<>();
        tasksSortedByTime = new TreeMap<>(taskComparator);
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
        addTaskToSortedMap(newTask);
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
        addTaskToSortedMap(newSubtask);
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
    public Subtask getSubtask(Integer id) {
        Subtask s = subtaskList.get(id);
        viewHistory.add(s);
        return s;
    }

    // Метод обновления задачи
    @Override
    public int updateTask(Task task) {
        int id = task.getId();
        taskList.put(id, task);
        addTaskToSortedMap(task);
        return id;
    }

    // Метод обновления эпика
    @Override
    public int updateEpic(Epic newEpic) {
        int id = newEpic.getId();
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
        int epicId = newSubtask.getEpicId();
        if (!epicList.containsKey(epicId)) {
            return -2;
        }
        subtaskList.put(id, newSubtask);
        setStatusEpic(epicId);
        addTaskToSortedMap(newSubtask);
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
        setEpicTime(epicId);

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
        Task task = taskList.get(taskId);
        tasksSortedByTime.remove(task);
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
        Task task = subtaskList.get(subtaskId);
        tasksSortedByTime.remove(task);

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
            tasksSortedByTime.remove(task);
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
            tasksSortedByTime.remove(subtask);
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
        List<Subtask> subtasks = new ArrayList<>();

        subtasks = subtaskList.values().stream()
                .filter((Subtask subtask) -> subtask.getEpicId() == epicId)
                .collect(Collectors.toList());
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

    // очистка всех задач и эпиков
    public void clear() {
        removeAllTasks();
        removeAllEpics();
        tasksSortedByTime.clear();
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

    /**
     * Вычисление времени старта и завершения эпика на основе времен подзадач
     *
     * @param epicId - идентификатор эпика
     */
    private void setEpicTime(Integer epicId) {
        Subtask subtask;
        LocalDateTime minDateTime;
        LocalDateTime finishTime;
        int minutesOfDuration = 0;

        Epic epic = epicList.get(epicId);
        List<Integer> subtasks = epic.getSubtasks();
        if (subtasks.isEmpty()) {
            epic.setStartTime(null);
            epic.setEndTime(null);
            epic.setDuration(null);
            return;
        } else {
            subtask = subtaskList.get(subtasks.get(0));
            minDateTime = subtask.getStartTime();
            finishTime = subtask.getEndTime();
        }

        for (int subtaskId : subtasks) {
            subtask = subtaskList.get(subtaskId);
            LocalDateTime subtaskStartTime = subtask.getStartTime();
            LocalDateTime subtaskEndTime = subtask.getEndTime();
            minutesOfDuration += subtask.getDuration().toMinutes();

            if (subtaskStartTime.isBefore(minDateTime)) {
                minDateTime = subtaskStartTime;
            }
            if (subtaskEndTime.isAfter(finishTime)) {
                finishTime = subtaskEndTime;
            }
        }
        epic.setStartTime(minDateTime);
        epic.setEndTime(finishTime);
        epic.setDuration(Duration.ofMinutes(minutesOfDuration));
    }

    /**
     * Метод сортировки списка задач по времени начала выполнения
     *
     * @return - отсортированный список
     */
    @Override
    public List<Task> getPrioritizedTasks() {

        List<Task> sortedTaskList = new ArrayList<>();

        for (Map.Entry<Task, String> entry : tasksSortedByTime.entrySet()) {
            sortedTaskList.addLast(entry.getKey());
        }
        return sortedTaskList;
    }

    /**
     * Добавление задачик к хранилищу отсортированному по времени начала
     *
     * @param task - задача для добавления
     */
    private void addTaskToSortedMap(Task task) {
        if (task.getStartTime() == null) {
            /* ТЗ-7:
            Дата начала задачи по каким-то причинам может быть не задана.
            Тогда при добавлении её не следует учитывать в списке задач и подзадач,
            отсортированных по времени начала.
             */
            return;
        }

        LocalDateTime curentTime = LocalDateTime.now();
        if (tasksSortedByTime.size() == 0) {
            tasksSortedByTime.put(task, curentTime.format(Task.DATE_TIME_FORMATTER));
            return;
        }

        // Проверяем пересечение времени добавляемой задачи с существующими задачами
        List<Task> crossTime = getPrioritizedTasks().stream()
                .filter((Task existsTask) -> !checkTimeFree(task, existsTask))
                .collect(Collectors.toList());

        if (crossTime.isEmpty()) {
            tasksSortedByTime.put(task, curentTime.format(Task.DATE_TIME_FORMATTER));
        } else {
            String message = "Конфликт по времени исполнения.\n " + task.toString();
            throw new TaskCrossTimeException(message, "число конфликтов - " + crossTime.size());
        }
    }

    private void removeFromSortedList(Task task) {
        tasksSortedByTime.remove(task);
    }

    /**
     * Определение непересечения временных интервалов двух задач
     *
     * @param task1 - задача для сравнения
     * @param task2 - задача для сравнения
     * @return - true, если время работы задач не пересекается, иначе false
     */
    private boolean checkTimeFree(Task task1, Task task2) {
        if (task1.equals(task2)) {
            return true;
        }
        LocalDateTime task1Start = task1.getStartTime();
        LocalDateTime task1End = task1.getEndTime();
        LocalDateTime task2Start = task2.getStartTime();
        LocalDateTime task2End = task2.getEndTime();

        return (task2Start.isBefore(task1Start) && task2End.isBefore(task1Start)) ||
                task2Start.isAfter(task1End);
    }


}