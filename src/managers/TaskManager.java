package managers;

import exceptions.NotFoundException;
import exceptions.TimeIntersectionException;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.util.List;

public interface TaskManager {
    // Метод добавления новой задачи
    int addNewTask(Task newTask) throws TimeIntersectionException;

    // Метод добавления нового эпика
    int addNewEpic(Epic newEpic);

    // Метод добавления новоq подзадачи
    int addNewSubtask(Subtask newSubtask) throws TimeIntersectionException;

    // Метод получения задачи по индексу
    Task getTask(Integer id) throws NotFoundException;

    // Метод получения эпика по индексу
    Epic getEpic(Integer id) throws NotFoundException;

    // Метод получения подзадачи по индексу
    Subtask getSubtask(Integer id) throws NotFoundException;

    // Метод обновления задачи
    int updateTask(Task task) throws TimeIntersectionException;

    // Метод обновления эпика
    int updateEpic(Epic newEpic);

    int updateSubtask(Subtask newSubtask) throws TimeIntersectionException;

    void removeTask(Integer taskId) throws NotFoundException;

    void removeEpic(Integer epicId) throws NotFoundException;

    void removeSubtask(Integer subtaskId) throws NotFoundException;

    List<Task> getTaskList() throws NotFoundException;

    List<Epic> getEpicList() throws NotFoundException;

    List<Subtask> getSubtaskList() throws NotFoundException;

    // Удаление всех объектов класса Task
    void removeAllTasks();

    // Удаление всех объектов класса Epic
    void removeAllEpics();

    // Удаление всех объектов класса Subtask
    void removeAllSubtasks();

    List<Subtask> getSubtasksByEpic(Integer epicId);

    // определение общего числа задач всех типов в менеджере
    int getNumberOfObjects();

    // просмотр использованных задач
    List<Task> getHistory() throws NotFoundException;

    List<Task> getPrioritizedTasks() throws NotFoundException;
}
