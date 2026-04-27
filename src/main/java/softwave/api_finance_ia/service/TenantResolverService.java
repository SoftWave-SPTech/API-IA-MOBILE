package softwave.api_finance_ia.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TenantResolverService {

    private final Long defaultTenantId;
    private final Map<Long, Long> userTenantMap;

    public TenantResolverService(
            @Value("${features.default-tenant-id:}") String defaultTenantIdRaw,
            @Value("${features.user-tenant-map:}") String userTenantMapRaw
    ) {
        this.defaultTenantId = parseNullableLong(defaultTenantIdRaw);
        this.userTenantMap = parseUserTenantMap(userTenantMapRaw);
    }

    public Long resolveTenantId(Long userId, Long tenantIdFromRequest) {
        if (tenantIdFromRequest != null) return tenantIdFromRequest;
        if (userId != null && userTenantMap.containsKey(userId)) {
            return userTenantMap.get(userId);
        }
        return defaultTenantId;
    }

    private static Long parseNullableLong(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Map<Long, Long> parseUserTenantMap(String raw) {
        Map<Long, Long> map = new HashMap<>();
        if (raw == null || raw.isBlank()) return map;
        String[] pairs = raw.split(",");
        for (String pair : pairs) {
            String[] kv = pair.trim().split(":");
            if (kv.length != 2) continue;
            try {
                Long userId = Long.parseLong(kv[0].trim());
                Long tenantId = Long.parseLong(kv[1].trim());
                map.put(userId, tenantId);
            } catch (NumberFormatException ignored) {
                // ignora par invalido
            }
        }
        return map;
    }
}
