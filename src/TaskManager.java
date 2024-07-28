import tasks.*;

import java.util.List;
import java.util.ArrayList;

public interface TaskManager {
    // Метод добавления новой задачи
    int addNewTask(Task newTask);

    // Метод добавления нового эпика
    int addNewEpic(Epic newEpic);

    // Метод добавления новоq подзадачи
    int addNewSubtask(Subtask newSubtask);

    // Метод получения задачи по индексу
    Task getTask(Integer id);

    // Метод получения эпика по индексу
    Epic getEpic(Integer id);

    // Метод получения подзадачи по индексу
    Subtask getSubtasks(Integer id);

    // Метод обновления задачи
    int updateTask(Task task);

    // Метод обновления эпика
    int updateEpic(Epic newEpic);

    int updateSubtask(Subtask newSubtask);

    void removeTask(Integer taskId);

    void removeEpic(Integer epicId);

    void removeSubtask(Integer subtaskId);

    List<Task> getTaskList();

    List<Epic> getEpicList();

    List<Subtask> getSubtaskList();

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
    List<Task> getHistory();
}
