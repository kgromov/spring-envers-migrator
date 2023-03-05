package com.kgromov.service;

import com.kgromov.config.DataSourceSettings;
import com.kgromov.model.ColumnMetadata;
import com.kgromov.model.TableMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;

@Component
@Slf4j
@RequiredArgsConstructor
public class TableMetadataExtractor {
    private static final String COLUMNS_QUERY =
            """
                SELECT * FROM information_schema.COLUMNS\s
                WHERE table_schema=?\s
                AND table_name=?
            """;

    private final DataSourceSettings settings;
    private final JdbcTemplate jdbcTemplate;
    private final ColumnMetadataRowMapper rowMapper;

    public TableMetadata getTableMetadata() {
        String tableName = settings.getTableName();
        List<ColumnMetadata> columnsMetadata = jdbcTemplate.query(COLUMNS_QUERY, rowMapper, settings.getDbName(), tableName);
        return TableMetadata.builder()
                .name(tableName +  "Audit")
                .sourceName(tableName)
                .columns(columnsMetadata)
                .build();
    }

}
