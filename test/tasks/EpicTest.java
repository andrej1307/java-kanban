package tasks;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EpicTest {
    private static Epic epic;
    private static final int epicId = 1;
    private int expectedSubtaskListSize;

    @BeforeAll
    public static void beforeAll() {
        epic = new Epic("Тест 2", "Тестируем работу с подзадачами");
        epic.setId(epicId);
    }

    @Test
    public void addEpicIdToSubtaskList() {
        expectedSubtaskListSize = epic.getSubtasks().size();

        // добавляем собственный идентификатор в качкстве подзадачи
        epic.addSubtask(epicId);
        assertEquals(expectedSubtaskListSize, epic.getSubtasks().size(),
                "Некорректное использование идентификатора подзадачи");
    }

    @Test
    public void addSubtask() {
        expectedSubtaskListSize = epic.getSubtasks().size();
        epic.addSubtask(2);
        expectedSubtaskListSize++;
        assertEquals(expectedSubtaskListSize, epic.getSubtasks().size(),
                "Не верное число подзадач у эпика");
    }

    @Test
    public void setEpicStatus() {
        TaskStatus espectedStatus = TaskStatus.IN_PROGRESS;
        epic.setStatus(espectedStatus);
        assertEquals(espectedStatus, epic.getStatus(), "Статус не соответствует установленному");
    }
}