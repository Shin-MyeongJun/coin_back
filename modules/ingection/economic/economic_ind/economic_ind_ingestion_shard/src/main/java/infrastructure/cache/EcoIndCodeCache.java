package infrastructure.cache;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EcoIndCodeCache {
    Map<String ,Long> indCache = new HashMap<String, Long>();
    
    public void put(String indCode,Long id) {
        indCache.put(indCode,id);
    }
    public Long getId(String indCode) {
        return indCache.get(indCode);
    }
    
}
