import tasks.*;

import java.io.*;

/**
 * класс менеджера задач с поддержной сохранения данных в файл и загрузки
 */
public class FileBackedTaskManager extends InMemoryTaskManager {
    final private String fileName;
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

    /**
     * подготовка информации о задаче для записи в файл
     *
     * @param task - задача
     * @return - строка информации о задаче.
     */
    private String toString(Task task) {
        return String.format("%s;#type#;%s;%s;%s;",
                task.getId(), task.getTitle(),
                task.getStatus(), task.getDescription());
    }

    /**
     * Сохранение информации о задачах в файл
     *
     * @throws ManagerSaveException - исключение при ошибках работы с файлом
     */
    public void save() throws ManagerSaveException {
        // при загрузке данных из файла ничего не пишем.
        if (loadInprogres) {
            return;
        }

        try (FileWriter fileWriter = new FileWriter(fileName)) {
            // В первую строку файла записываем наименования полей.
            fileWriter.write("id;type;name;status;description;epic\n");

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
            throw new ManagerSaveException("Ошибка сохранения в файл:"
                    + e.getMessage(), fileName);
        }
    }

    /**
     * Создание нового экземпляра менеджера задач на основе файла данных
     *
     * @param file - файл с описанием задач
     * @return - ссылка на объект менеджера задач
     */
    static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager;
        manager = new FileBackedTaskManager(file.getAbsolutePath());

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            manager.setLoadFlag(true);
            String line = bufferedReader.readLine();
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.isBlank()) {
                    String[] tokens = line.split(";");
                    if (tokens.length < 5) {
                        continue;
                    }
                    if (tokens[1].equals(TaskType.TASK.toString())) {
                        Task task = new Task(tokens[2], tokens[4]);
                        task.setId(Integer.decode(tokens[0]));
                        task.setStatus(TaskStatus.valueOf(tokens[3]));
                        manager.updateTask(task);
                    } else if (tokens[1].equals(TaskType.EPIC.toString())) {
                        Epic epic = new Epic(tokens[2], tokens[4]);
                        epic.setId(Integer.decode(tokens[0]));
                        epic.setStatus(TaskStatus.valueOf(tokens[3]));
                        manager.updateEpic(epic);
                    } else if (tokens[1].equals(TaskType.SUBTASK.toString())) {
                        int subtaskId = Integer.decode(tokens[0]);
                        int epicId = Integer.decode(tokens[5]);
                        Epic epic = manager.getEpic(epicId);
                        epic.addSubtask(subtaskId);
                        Subtask subtask = new Subtask(epicId, tokens[2], tokens[4]);
                        subtask.setId(subtaskId);
                        subtask.setStatus(TaskStatus.valueOf(tokens[3]));
                        manager.updateSubtask(subtask);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            manager.setLoadFlag(false); // сбрасываем признак выполнения загрузки
            manager.resetMainId();      // пересчитываем идентификатор задач в менеджере.
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

}
