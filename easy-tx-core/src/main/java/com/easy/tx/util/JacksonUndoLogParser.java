package com.easy.tx.util;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import com.easy.tx.store.undo.SagaUndoLog;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JacksonUndoLogParser  {

    public static final String NAME = "jackson";

    private static final Logger LOGGER = LoggerFactory.getLogger(JacksonUndoLogParser.class);


    private static final ObjectMapper MAPPER = new ObjectMapper();


    static {
        //在解析json的时候忽略字段名字不对应的会报错的情况  如usernamexxx字段映射到User实体类
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //为空的列不参与序列化   以免es更新的时候多更新了null的列
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 忽略 transient 修饰的属性
        MAPPER.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);
        //解决jackson2无法反序列化LocalDateTime的问题
//        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
        //只序列化字段，
        MAPPER.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        //映射下划线的
//        MAPPER.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        // 指定序列化输入的类型，类必须是非final修饰的，final修饰的类，比如String,Integer等会跑出异常  指定了类型会写入序列化类的类型，这样不能通用的反序列化
        MAPPER.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        simpleModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        simpleModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        MAPPER.registerModule(simpleModule);
    }


    public static byte[] encodeSaga(SagaUndoLog branchUndoLog) {
        try {
            return MAPPER.writeValueAsBytes(branchUndoLog);
        } catch (JsonProcessingException e) {
            LOGGER.error("json encode exception, {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


    public static SagaUndoLog decodeSaga(byte[] bytes) {
        try {
            SagaUndoLog branchUndoLog;
            if (Arrays.equals(bytes, "".getBytes())) {
                branchUndoLog =null;
            } else {
                branchUndoLog = MAPPER.readValue(bytes, SagaUndoLog.class);
            }
            return branchUndoLog;
        } catch (IOException e) {
            LOGGER.error("json decode exception, {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
