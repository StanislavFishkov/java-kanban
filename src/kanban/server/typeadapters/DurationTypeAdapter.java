package kanban.server.typeadapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;

public final class DurationTypeAdapter extends TypeAdapter<Duration> {
    @Override
    public void write(final JsonWriter jsonWriter, final Duration duration) throws IOException {
        if (duration == null) {
            jsonWriter.nullValue();
        } else {
            jsonWriter.value(duration.toMinutes());
        }
    }

    @Override
    public Duration read(final JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }
        return Duration.ofMinutes(jsonReader.nextLong());
    }
}