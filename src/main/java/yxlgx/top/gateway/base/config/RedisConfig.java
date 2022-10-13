package yxlgx.top.gateway.base.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author yx
 * @date 2021/12/16
 **/
@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport {
    @Bean("redisTemplate")
    public RedisTemplate<Object, Object> initRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        // 设置连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // 定义 String 序列化器
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // 定义 Jackson 序列化器
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        //反序列化时智能识别变量名（识别没有按驼峰格式命名的变量名）
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        //反序列化识别对象类型
        //objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_ARRAY);
        //反序列化如果有多的属性，不抛出异常
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //反序列化如果碰到不识别的枚举值，是否作为空值解释，true:不会抛不识别的异常, 会赋空值，false:会抛不识别的异常
        objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        // 设置 Redis 的 key 以及 hash 结构的 field 使用 String 序列化器
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        // 设置 Redis 的 value 以及 hash 结构的 value 使用 Jackson 序列化器
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

    @Bean("reactiveRedisTemplate")
    public ReactiveRedisTemplate<String,Object> reactiveRedisTemplate(ReactiveRedisConnectionFactory connectionFactory) {
        RedisSerializationContext.SerializationPair<String> stringSerializationPair = RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer.UTF_8);
        RedisSerializationContext.SerializationPair<Object> objectSerializationPair = RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json());
        RedisSerializationContext.RedisSerializationContextBuilder<String,Object>  builder =
                RedisSerializationContext.newSerializationContext();
        builder.key(stringSerializationPair);

        builder.value(objectSerializationPair);

        builder.hashKey(stringSerializationPair);
        builder.hashValue(objectSerializationPair);

        builder.string(stringSerializationPair);

        RedisSerializationContext<String,Object> build = builder.build();

        return new ReactiveRedisTemplate<>(connectionFactory, build);
    }

}