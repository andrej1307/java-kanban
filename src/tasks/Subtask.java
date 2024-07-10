
/*
 * Класс описания подзадачи.
 * При создании обязательно нужно указать объект "эпик", которому привязан.
 */
package tasks;

public class Subtask extends Task  {
    private Epic epic;

    // При вызове конструктора указание объекта задачи "эпик" обязательно
    public Subtask(Epic epic, String title, String description) {
        super(title, description);
        this.epic = epic;
        epic.addSubtask(this);
    }

    public Subtask(Epic epic, String title) {
        super(title);
        this.epic = epic;
        epic.addSubtask(this);
    }

    @Override
    public String toString() {
        String classToString;
        classToString = "Subtask{ID=" + getID() + ", ";
        if (epic != null) {
            classToString += "epic_ID=" + epic.getID() + ", ";
        } else {
            classToString += "epic_ID=null, ";
        }
        classToString += ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                '}';
        return classToString;
    }

    /**
     * Метод изменения статуса выполнения подзадачи.
     * После изменения собственного статуса запускаем пересчет статуса эпика.
     */

     public void setStatus(TaskStatus newStatus) {
        super.setStatus(newStatus);
        if (epic != null) {
            epic.calculateStatus();
        }
    }

    // Метод разрыва соединения с задачей "эпик" - например при удалении эпика
    public void breakLinkToEpic() {
        this.epic = null;
    }

    // Метод привязки существующего объекта подзадачи к эпику
    public void setLinkToEpic(Epic newEpic) {
        this.epic = newEpic;
    }

    public Epic getEpic() {
        return epic;
    }
}
