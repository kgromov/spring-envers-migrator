package com.kgromov.config;

import lombok.Builder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@Builder
@ConfigurationProperties(prefix = "spring.source")
public class DataSourceSettings {
    private String dbName;
    private String tableName;
}
