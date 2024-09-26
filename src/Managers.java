import java.io.File;

public final class Managers {
    // объект утилитарного класса не должен создаваться!
    // конструктор объявлен с модификатором "private"
    private Managers() {
    }

    // определение объекта меджера задач
    public static TaskManager getDefault() {
        return new FileBackedTaskManager();
        // return FileBackedTaskManager.loadFromFile(new File(".\\data\\tasks.csv"));
    }

    // определение объекта журнала событий
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
