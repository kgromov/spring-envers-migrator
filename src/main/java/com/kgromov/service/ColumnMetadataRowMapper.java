package com.kgromov.service;

import com.kgromov.model.ColumnMetadata;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;

@Component
public class ColumnMetadataRowMapper implements RowMapper<ColumnMetadata> {

    @Override
    public ColumnMetadata mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            boolean isPK = ofNullable(resultSet.getString("COLUMN_KEY")).map(key -> key.equals("PRI")).orElse(false);
            String columnName = resultSet.getString("COLUMN_NAME");
            String datatype = resultSet.getString("DATA_TYPE");
            String columnType = resultSet.getString("COLUMN_TYPE");
            String isNullable = resultSet.getString("IS_NULLABLE");

            ColumnMetadata baseColumn = ColumnMetadata.builder()
                    .name(columnName)
                    .type(columnType)
                    .primaryKey(isPK)
                    .build();
         /*   columns.add(baseColumn);

            if (!isPK) {
                ColumnMetadata auditColumn = ColumnMetadata.builder()
                        .name(columnName.replace("_id", "") + "Modified")
                        .type("boolean")
                        .modifiedColumn(true)
                        .build();
                columns.add(auditColumn);
            }*/
        return baseColumn;
    }
}
