package managers;

public final class Managers {
    // объект утилитарного класса не должен создаваться!
    // конструктор объявлен с модификатором "private"
    private Managers() {
    }

    // определение объекта меджера задач
    public static TaskManager getDefault() {
        InMemoryTaskManager manager = new InMemoryTaskManager();
        return manager;
    }

    // определение объекта журнала событий
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
