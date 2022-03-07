package nbbang.com.nbbang.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class LogoutService {
    private final RedisTemplate<String, String> redisTemplate;
    private String key = "blocked";

    public void invalidate(String token) {
        SetOperations<String, String> setOperations = redisTemplate.opsForSet();
        setOperations.add(key, token);
    }

    public boolean isInvalid(String token) {
        SetOperations<String, String> setOperations = redisTemplate.opsForSet();
        Set<String> members = setOperations.members(key);
        return members != null && members.contains(token);
    }
}
