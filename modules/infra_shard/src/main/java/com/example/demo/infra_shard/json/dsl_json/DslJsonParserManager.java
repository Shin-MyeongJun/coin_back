package com.example.demo.infra_shard.json.dsl_json;

import com.dslplatform.json.DslJson;
import okio.ByteString;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;



@Component
public class DslJsonParserManager {

    private final Map<String, DslJsonParser<?>> registry = new HashMap<>();

    public <T> void register(String type, Class<T> clazz) {
        registry.put(type, new DslJsonParser<>(clazz));
    }

    public <T, U> void registerGeneric(String type, Class<T> wrapper, Class<U> inner) {
        Type typeRef = new ParameterizedType() {
            public Type[] getActualTypeArguments() {
                return new Type[]{inner};
            }

            public Type getRawType() {
                return wrapper;
            }

            public Type getOwnerType() {
                return null;
            }
        };
        registry.put(type, new DslJsonParser<>(typeRef));
    }

    public <T> T parse(String type, String json) {
        @SuppressWarnings("unchecked")
        DslJsonParser<T> parser = (DslJsonParser<T>) registry.get(type);
        if (parser == null) throw new RuntimeException("No parser registered for type: " + type);
        return parser.parse(json);
    }

    public <T> T parse(String type, ByteString bytes) {
        return parse(type, bytes.utf8());
    }

    public static class DslJsonParser<T> {
        private final DslJson<Object> dslJson = new DslJson<>();
        private final Type type;

        public DslJsonParser(Type type) {
            this.type = type;
        }

        @SuppressWarnings("unchecked")
        public T parse(String json) {
            try {
                return (T) dslJson.deserialize(
                        type,
                        new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)),
                        new byte[1024]
                );
            } catch (IOException e) {
                throw new RuntimeException("Failed to parse " + type.getTypeName(), e);
            }
        }

        public Type getType() {
            return type;
        }
    }
}


