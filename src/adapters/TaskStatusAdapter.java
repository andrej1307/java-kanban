package adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import tasks.TaskStatus;

import java.io.IOException;

public class TaskStatusAdapter extends TypeAdapter<TaskStatus> {
    @Override
    public void write(final JsonWriter jsonWriter, final TaskStatus taskStatus) throws IOException {
        if (taskStatus == null) {
            jsonWriter.value(TaskStatus.NEW.toString());
            return;
        }
        jsonWriter.value(taskStatus.toString());
    }

    @Override
    public TaskStatus read(final JsonReader jsonReader) throws IOException {
        TaskStatus taskStatus = TaskStatus.valueOf(jsonReader.nextString());
        if (taskStatus == null) {
            return TaskStatus.NEW;
        }
        return taskStatus;
    }

}
