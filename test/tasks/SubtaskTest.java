package tasks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

    @Test
    void setEpicId() {
        int epicId = 5;
        Subtask subtask = new Subtask(epicId, "тест 3", "Тестируем подзадачу");
        int subtaskId = 6;
        subtask.setId(subtaskId);

        // присваиваем идентификатор подзадачи вместо эпика
        subtask.setEpicId(subtaskId);

        assertEquals(epicId, subtask.getEpicId(),
                "Некорректное использование собственного идентификатора.");
    }
}