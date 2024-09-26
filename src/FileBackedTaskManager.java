import exceptions.LoadException;
import exceptions.SaveException;
import tasks.*;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * класс менеджера задач с поддержной сохранения данных в файл и загрузки
 */
public class FileBackedTaskManager extends InMemoryTaskManager {
    private String fileName;
    private boolean loadInprogres;

    /**
     * Конструктор
     *
     * @param fileName - имя файла для сохранения информации о задачах
     */
    public FileBackedTaskManager(String fileName) {
        super();
        this.fileName = fileName;
        loadInprogres = false;
    }

    public FileBackedTaskManager() {
        super();
        this.fileName = "";
        loadInprogres = false;
    }

    /**
     * подготовка информации о задаче для записи в файл
     *
     * @param task - задача
     * @return - строка информации о задаче.
     */
    private String toString(Task task) {
        String row;
        String startTime;
        String duration;

        try {
            if (task.getStartTime() == null) {
                startTime = "null";
            } else {
                startTime = task.getStartTime().format(Task.DATE_TIME_FORMATTER);
            }
            if (task.getDuration() == null) {
                duration = "null";
            } else {
                duration = String.format("%d", task.getDuration().toMinutes());
            }

            row = String.format("%s;%s;%s;#type#;%s;%s;%s;",
                    task.getId(),
                    startTime,
                    duration,
                    task.getTitle(),
                    task.getStatus(),
                    task.getDescription());
        } catch (Exception e) {
            throw new SaveException("Ошибка сохранения в файл. "
                    + e.getMessage(), fileName);
        }
        return row;
    }

    /**
     * Сохранение информации о задачах в файл
     *
     * @throws SaveException - исключение при ошибках работы с файлом
     */
    public void save() throws SaveException {
        // при загрузке данных из файла ничего не пишем.
        if (loadInprogres) {
            return;
        }

        try (FileWriter fileWriter = new FileWriter(fileName)) {
            // В первую строку файла записываем наименования полей.
            fileWriter.write("id;DateTime;Duration(min);type;name;status;description;epic\n");

            // сохраняем задачи
            String taskType = TaskType.TASK.toString();
            for (Task task : getTaskList()) {
                fileWriter.write(toString(task).replaceFirst("#type#", taskType)
                        + "\n");
            }

            // сохраняем эпики
            taskType = TaskType.EPIC.toString();
            for (Epic epic : getEpicList()) {
                fileWriter.write(toString(epic).replaceFirst("#type#", taskType)
                        + "\n");
            }

            // сохраняем подзадачи
            taskType = TaskType.SUBTASK.toString();
            for (Subtask subtask : getSubtaskList()) {
                fileWriter.write(toString(subtask).replaceFirst("#type#", taskType)
                        + subtask.getEpicId() + "\n");
            }

            fileWriter.flush();

        } catch (IOException e) {
            throw new SaveException("Ошибка сохранения в файл. "
                    + e.getMessage(), fileName);
        }
    }

    /**
     * Создание нового экземпляра менеджера задач на основе файла данных
     *
     * @param file - файл с описанием задач
     * @return - ссылка на объект менеджера задач
     */
    static FileBackedTaskManager loadFromFile(File file) throws LoadException {
        FileBackedTaskManager manager;
        manager = new FileBackedTaskManager(file.getAbsolutePath());

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            LocalDateTime startTime;
            Duration duration;

            manager.setLoadFlag(true);

            String line = bufferedReader.readLine();
            if (line == null) {
                manager.setLoadFlag(false);
                return manager;
            }
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.isBlank()) {
                    String[] tokens = line.split(";");
                    if (tokens.length < 7) {
                        continue;
                    }
                    int id = Integer.decode(tokens[0]);
                    try {
                        if (tokens[1].toLowerCase().equals("null")) {
                            startTime = null;
                        } else {
                            startTime = LocalDateTime.parse(tokens[1],
                                    Task.DATE_TIME_FORMATTER);
                        }
                    } catch (DateTimeParseException e) {
                        throw new LoadException("Ошибка чтения времени из файла. "
                                + e.getMessage(), file.getAbsolutePath().toString());
                    }
                    if (tokens[2].toLowerCase().equals("null")) {
                        duration = null;
                    } else {
                        duration = Duration.ofMinutes(Integer.decode(tokens[2]));
                    }
                    String taskType = tokens[3];
                    String title = tokens[4];
                    String status = tokens[5];
                    String description = tokens[6];

                    if (taskType.equals(TaskType.TASK.toString())) {
                        Task task = new Task(title, description);
                        task.setId(id);
                        task.setStatus(TaskStatus.valueOf(status));
                        task.setStartTime(startTime);
                        task.setDuration(duration);
                        manager.updateTask(task);
                    } else if (taskType.equals(TaskType.EPIC.toString())) {
                        Epic epic = new Epic(title, description);
                        epic.setId(id);
                        epic.setStatus(TaskStatus.valueOf(status));
                        epic.setStartTime(startTime);
                        epic.setDuration(duration);
                        manager.updateEpic(epic);
                    } else if (taskType.equals(TaskType.SUBTASK.toString())) {
                        int epicId = Integer.decode(tokens[7]);
                        Epic epic = manager.getEpic(epicId);
                        epic.addSubtask(id);
                        Subtask subtask = new Subtask(epicId, title, description);
                        subtask.setId(id);
                        subtask.setStatus(TaskStatus.valueOf(status));
                        subtask.setStartTime(startTime);
                        subtask.setDuration(duration);
                        manager.updateSubtask(subtask);
                    }
                }
            }
        } catch (IOException e) {
            throw new LoadException("Ошибка загрузки данных из файла. "
                    + e.getMessage(), file.getAbsolutePath().toString());
        } finally {
            manager.setLoadFlag(false); // сбрасываем признак выполнения загрузки
            if (manager.getNumberOfObjects() > 0) {
                manager.resetMainId();      // пересчитываем идентификатор задач в менеджере.
            }
        }
        return manager;
    }

    /**
     * Установка признака выполнения процесса загрузки данных из файла
     *
     * @param flag - признак загрузки (если true, то сохранение не выполняется)
     */
    public void setLoadFlag(boolean flag) {
        loadInprogres = flag;
    }

    @Override
    public int addNewTask(Task newTask) {
        int retId = super.addNewTask(newTask);
        save();
        return retId;
    }

    @Override
    public int addNewEpic(Epic newEpic) {
        int retId = super.addNewEpic(newEpic);
        save();
        return retId;
    }

    @Override
    public int addNewSubtask(Subtask newSubtask) {
        int retId = super.addNewSubtask(newSubtask);
        save();
        return retId;
    }

    @Override
    public int updateTask(Task task) {
        int retId = super.updateTask(task);
        save();
        return retId;
    }

    @Override
    public int updateEpic(Epic newEpic) {
        int retId = super.updateEpic(newEpic);
        save();
        return retId;
    }

    @Override
    public int updateSubtask(Subtask newSubtask) {
        int retId = super.updateSubtask(newSubtask);
        save();
        return retId;
    }

    @Override
    public void removeTask(Integer taskId) {
        super.removeTask(taskId);
        save();
    }

    @Override
    public void removeEpic(Integer epicId) {
        super.removeEpic(epicId);
        save();
    }

    @Override
    public void removeSubtask(Integer subtaskId) {
        super.removeSubtask(subtaskId);
        save();
    }

    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    @Override
    public void removeAllEpics() {
        super.removeAllEpics();
        save();
    }

    @Override
    public void removeAllSubtasks() {
        super.removeAllSubtasks();
        save();
    }

    public String getSaveFileName() {
        return fileName;
    }

    /**
     * Изменяем имя файла для сохранения данных
     *
     * @param fileName - имя файла
     */
    public void setSaveFileName(String fileName) {
        this.fileName = fileName;
    }

}
