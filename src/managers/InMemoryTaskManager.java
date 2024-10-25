package managers;

import exceptions.NotFoundException;
import exceptions.TimeIntersectionException;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> taskList;
    protected final Map<Integer, Epic> epicList;
    protected final Map<Integer, Subtask> subtaskList;
    private Integer idMain = 1;
    private final Map<Task, String> tasksSortedByTime;
    private final HistoryManager viewHistory = Managers.getDefaultHistory();

    // Компаратор для упоорядочивания задач по ремени запуска,
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
    public int addNewTask(Task newTask) throws TimeIntersectionException {
        if (newTask == null) {
            return -1;
        }
        if (newTask.getStartTime() == null) {
            return -2; // Время начала выполнения задачи обязательный параметр
        }
        if (newTask.getStatus() == null) {
            newTask.setStatus(TaskStatus.NEW);
        }
        if (newTask.getDuration() == null) {
            newTask.setDuration(Duration.ofMinutes(15));
        }
        int intersections = getTaskIntersectionsNum(newTask);
        if (intersections > 0) {
            String message = "Конфликт по времени исполнения. Новая задача:\n " + newTask.toString();
            throw new TimeIntersectionException(message, "число конфликтов - " + intersections);
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
    public int addNewSubtask(Subtask newSubtask)  throws TimeIntersectionException {
        if (newSubtask == null) {
            return -1;
        }
        if (newSubtask.getStartTime() == null) {
            return -2; // Время начала выполнения задачи обязательный параметр
        }
        if (newSubtask.getStatus() == null) {
            newSubtask.setStatus(TaskStatus.NEW);
        }
        if (newSubtask.getDuration() == null) {
            newSubtask.setDuration(Duration.ofMinutes(15));
        }
        Epic epic = epicList.get(newSubtask.getEpicId());
        if (epic == null) {
            return -2;
        }
        int intersections = getTaskIntersectionsNum(newSubtask);
        if (intersections > 0) {
            String message = "Конфликт по времени исполнения. Новая подзадача:\n " + newSubtask.toString();
            throw new TimeIntersectionException(message, "число конфликтов - " + intersections);
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
    public Task getTask(Integer id) throws NotFoundException {
        Task task = taskList.get(id);
        if (task == null) {
            throw new NotFoundException("Задача не найдена", "id=" + id);
        }
        viewHistory.add(task);
        return task;
    }

    // Метод получения эпика по индексу
    @Override
    public Epic getEpic(Integer id) throws NotFoundException {
        Epic epic = epicList.get(id);
        if (epic == null) {
            throw new NotFoundException("Эпик не найден", "id=" + id);
        }
        viewHistory.add(epic);
        return epic;
    }

    // Метод получения подзадачи по индексу
    @Override
    public Subtask getSubtask(Integer id) throws NotFoundException {
        Subtask s = subtaskList.get(id);
        if (s == null) {
            throw new NotFoundException("Подзадача не найдена", "id=" + id);
        }
        viewHistory.add(s);
        return s;
    }

    // Метод обновления задачи
    @Override
    public int updateTask(Task task)  throws TimeIntersectionException {
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.NEW);
        }
        if (task.getDuration() == null) {
            task.setDuration(Duration.ofMinutes(15));
        }
        int intersections = getTaskIntersectionsNum(task);
        if (intersections > 0) {
            String message = "Конфликт по времени исполнения. Обновление задачи:\n " + task.toString();
            throw new TimeIntersectionException(message, "число конфликтов - " + intersections);
        }
        int id = task.getId();
        taskList.put(id, task);
        addTaskToSortedMap(task);
        return id;
    }

    // Метод обновления эпика
    @Override
    public int updateEpic(Epic newEpic) {
        int id = newEpic.getId();
        Epic oldEpic = epicList.get(id);
        if (oldEpic != null) {
            // перепмсываум идентификаторы подзадач старого эппика в новый
            for (Integer idSubtask : oldEpic.getSubtasks()) {
                newEpic.addSubtask(idSubtask);
            }
        }
        // заменяем старый эпик на новый
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
    public int updateSubtask(Subtask newSubtask)  throws TimeIntersectionException {
        if (newSubtask.getStatus() == null) {
            newSubtask.setStatus(TaskStatus.NEW);
        }
        if (newSubtask.getDuration() == null) {
            newSubtask.setDuration(Duration.ofMinutes(15));
        }
        int intersections = getTaskIntersectionsNum(newSubtask);
        if (intersections > 0) {
            String message = "Конфликт по времени исполнения. Обновление подзадачи:\n " + newSubtask.toString();
            throw new TimeIntersectionException(message, "число конфликтов - " + intersections);
        }
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

        if (subtaskList.isEmpty() || epic.getSubtasks().isEmpty()) {
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
    public void removeTask(Integer taskId) throws NotFoundException {
        Task task = taskList.get(taskId);
        if (task == null) {
            throw new NotFoundException("Задача не найдена",
                    "id=" + taskId);
        }
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
    public void removeEpic(Integer epicId) throws NotFoundException {
        if (!epicList.containsKey(epicId)) {
            throw new NotFoundException("Эпик не найден", "id=" + epicId);
        }
        for (Integer idSubtask : epicList.get(epicId).getSubtasks()) {
            Task task = subtaskList.get(idSubtask);
            tasksSortedByTime.remove(task);
            subtaskList.remove(idSubtask);
            viewHistory.remove(idSubtask);
        }
        epicList.remove(epicId);
        viewHistory.remove(epicId);
    }

    /**
     * Удаление подзадачи по идентификатору
     * удаляем предварительно из спика соответствующего эпика
     * и из общего списка позадач.
     *
     * @param subtaskId - идентификатор подзадачи
     */
    @Override
    public void removeSubtask(Integer subtaskId) throws NotFoundException {
        if (subtaskList.isEmpty()) {
            throw new NotFoundException("подзадача не найдена",
                    "id=" + subtaskId);
        }
        Task task = subtaskList.get(subtaskId);
        tasksSortedByTime.remove(task);

        Integer epicId = subtaskList.get(subtaskId).getEpicId();
        epicList.get(epicId).removeSubtask(subtaskId);
        subtaskList.remove(subtaskId);
        viewHistory.remove(subtaskId);
        setStatusEpic(epicId);
    }

    @Override
    public List<Task> getTaskList() throws NotFoundException {
        if (taskList.isEmpty()) {
            throw new NotFoundException("Информация не найдена.", "Список задач пуст.");
        }
        return new ArrayList<>(taskList.values());
    }

    @Override
    public List<Epic> getEpicList() throws NotFoundException {
        if (epicList.isEmpty()) {
            throw new NotFoundException("Информация не найдена.", "Список эпиков пуст.");
        }
        return new ArrayList<>(epicList.values());
    }

    @Override
    public ArrayList<Subtask> getSubtaskList() throws NotFoundException {
        if (subtaskList.isEmpty()) {
            throw new NotFoundException("Информация не найдена.", "Список лодзадач пуст.");
        }
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
            viewHistory.remove(epic.getId());
        }
        removeAllSubtasks();
        epicList.clear();
    }

    // Удаление всех объектов класса Subtask
    @Override
    public void removeAllSubtasks() {
        for (Epic epic : epicList.values()) {
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
    public List<Subtask> getSubtasksByEpic(Integer epicId) throws NotFoundException{
        List<Subtask> subtasks = new ArrayList<>();

        subtasks = subtaskList.values().stream()
                .filter((Subtask subtask) -> subtask.getEpicId() == epicId)
                .collect(Collectors.toList());
        if (subtasks.isEmpty()) {
            throw new NotFoundException("Информация не найдена.", "Список лодзадач пуст.");
        }
        return subtasks;
    }

    /**
     * Метод просмотра использованных задач
     *
     * @return - возвращает список использованных объектов
     */
    @Override
    public List<Task> getHistory() throws NotFoundException {
        if (viewHistory.getHistory().isEmpty()) {
            throw new NotFoundException("Информация не найдена.", "История отсутствует.");
        }
        return viewHistory.getHistory();
    }

    // очистка всех задач и эпиков
    public void clear() {
        if (!taskList.isEmpty()) {
            removeAllTasks();
        }
        if (!epicList.isEmpty()) {
            removeAllEpics();
        }
        if (!tasksSortedByTime.isEmpty()) {
            tasksSortedByTime.clear();
        }
        idMain = 1;
    }

    /**
     * Пересчет идентификатора задач после загрузки данных из файла
     */
    public void resetMainId() {
        int maxId = 1;
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
    public List<Task> getPrioritizedTasks()  throws NotFoundException {

        if (tasksSortedByTime.isEmpty()) {
            throw new NotFoundException("Информация не найдена", "список задач пуст" );
        }
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
        tasksSortedByTime.put(task, curentTime.format(Task.DATE_TIME_FORMATTER));
    }

    /**
     * Удаление задачи из отсортированного списка
     * @param task
     */
    private void removeFromSortedList(Task task) {
        tasksSortedByTime.remove(task);
    }

    /**
     * Проверка пересечения времени выполнения задачи с временами задач в отсортированном списке
     * @param newTask - проверяемая задача
     * @return - число пересечений по времени с существующими задачами
     */
    private int getTaskIntersectionsNum(Task newTask) {
        // Проверяем пересечение времени добавляемой задачи с существующими задачами
        if (tasksSortedByTime.isEmpty()) {
            return 0;
        }
        List<Task> intersections = getPrioritizedTasks().stream()
                .filter((Task existsTask) -> !checkTimeFree(newTask, existsTask))
                .collect(Collectors.toList());

        return intersections.size();
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
            return true; // пересечение с собой не учитываем (update)
        }
        LocalDateTime task1Start = task1.getStartTime();
        LocalDateTime task1End = task1.getEndTime();
        LocalDateTime task2Start = task2.getStartTime();
        LocalDateTime task2End = task2.getEndTime();

        return (task2Start.isBefore(task1Start) && task2End.isBefore(task1Start)) ||
                task2Start.isAfter(task1End);
    }

}