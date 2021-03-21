package io.github.agentrkid.nqueue.api.jedis;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Getter
@Accessors(chain = true)
public class ChainableMap {
    private final Map<String, Object> map;

    public ChainableMap() {
        this.map = new HashMap<>();
    }

    public ChainableMap append(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

    public static ChainableMap create() {
        return new ChainableMap();
    }
}
