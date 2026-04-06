package infrastructure.cache;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EcoIndCodeCache {
    Map<String ,Long> indCache = new ConcurrentHashMap<>();
    
    public void put(String indCode,Long id) {
        indCache.put(indCode,id);
    }
    public Long getId(String indCode) {
        return indCache.get(indCode);
    }
    
}
