package tasks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskTest {

    // Сравнение двух задач с одинаковыми идентификаторами
    @Test
    public void taskEquals() {
        Task task1 = new Task("тест 1", "Сравнить две задачи");
        Task task2 = new Task(task1); // создаем копию задачи
        task1.setId(1);
        task2.setId(1);

        assertEquals(task1, task2, "Задачи не равны");
    }

}