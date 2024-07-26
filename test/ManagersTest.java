import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void getDefault() {
        TaskManager manager = Managers.getDefault();
        assertNotNull(manager, "Менеджер задач не проинициализирован.");
    }

    @Test
    void getDefaultHistory() {
        HistoryManager viewHistory = Managers.getDefaultHistory();
        assertNotNull(viewHistory, "Менеджер журнала не проинициализирован.");
    }
}