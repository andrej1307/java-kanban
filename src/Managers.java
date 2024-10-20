public final class Managers {
    // объект утилитарного класса не должен создаваться!
    // конструктор объявлен с модификатором "private"
    private Managers() {
    }

    // определение объекта меджера задач
    public static TaskManager getDefault() {
/*        FileBackedTaskManager manager =
                FileBackedTaskManager.loadFromFile(new File (".\\data\\tasks.csv"));

        manager.setSaveFileName(".\\data\\taskSave.csv"); */

        InMemoryTaskManager manager = new InMemoryTaskManager();
        return manager;
    }

    // определение объекта журнала событий
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
