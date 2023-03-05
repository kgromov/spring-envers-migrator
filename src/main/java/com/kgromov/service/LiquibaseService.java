package com.kgromov.service;

import com.kgromov.model.ColumnMetadata;
import com.kgromov.model.TableMetadata;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Optional;

import static java.util.stream.Collectors.joining;

@Service
@Slf4j
@RequiredArgsConstructor
public class LiquibaseService {
    private static final String INSERT_AS_MODIFIED_PLACEHOLDER = "%s, (%s IS NOT NULL) AS %s";

    @SneakyThrows
    public Document buildCreateAuditTableChangeSet(Document changeLog, TableMetadata auditTableMetadata) {
        Element databaseChangeLog = changeLog.getDocumentElement();
        Element changeSet = changeLog.createElement("changeSet");
        changeSet.setAttribute("id", "Create " + auditTableMetadata.getName() + " table");
        changeSet.setAttribute("author", "");

        Element createTable = changeLog.createElement("createTable");
        changeSet.setAttribute("name", auditTableMetadata.getName());

        auditTableMetadata.getColumns()
                .forEach(columnMetadata -> {
                    Element columnElement = createColumnElement(changeLog, columnMetadata);
                    createTable.appendChild(columnElement);
                    createColumnModifiedElement(changeLog, columnMetadata).ifPresent(createTable::appendChild);
                });

        changeSet.appendChild(createTable);
        databaseChangeLog.appendChild(changeSet);
        return changeLog;
    }

    public Document buildInitRevisionForAuditTableChangeSet(Document changeLog, TableMetadata auditTableMetadata) {
        Element databaseChangeLog = changeLog.getDocumentElement();

        Element changeSet = changeLog.createElement("changeSet");
        changeSet.setAttribute("id", "Add init revision for " + auditTableMetadata.getName() + " table");
        changeSet.setAttribute("author", "");

        Element sqlElement = changeLog.createElement("sql");
        String insertQuery = buildInsertQuery(auditTableMetadata).toString();
        log.info("Add init revision query = {}", insertQuery);
        sqlElement.setTextContent(insertQuery);
        changeSet.appendChild(sqlElement);
        databaseChangeLog.appendChild(changeSet);
        return changeLog;
    }

    public Document buildPKForAuditTableChangeSet(Document changeLog) {
        return changeLog;
    }

    private StringBuilder buildInsertQuery(TableMetadata auditTableMetadata) {
        return new StringBuilder()
                .append("INSERT INTO ")
                .append(auditTableMetadata.getName())
                .append('(')
                .append(auditTableMetadata.getColumns().stream().map(ColumnMetadata::getName).collect(joining(", ")))
                .append(')')
                .append(" SELECT ")
                .append(auditTableMetadata.getColumns()
                        .stream()
                        .filter(columnMetadata -> !columnMetadata.isModifiedColumn())
                        .map(column -> {
                            String columnName = column.getName();
                            if (column.isPrimaryKey()) {
                                return columnName;
                            }
                            String modifiedColumnName = columnName.replace("(_id)|(_.*Id)", "") + "Modified";
                            return String.format(INSERT_AS_MODIFIED_PLACEHOLDER, columnName, columnName, modifiedColumnName);
                        })
                        .collect(joining(", "))
                )
                .append(" FROM ").append(auditTableMetadata.getSourceName());
    }

    private Element createColumnElement(Document document, ColumnMetadata columnMetadata) {
        Element column = document.createElement("column");
        column.setAttribute("name", columnMetadata.getName());
        column.setAttribute("type", columnMetadata.getType());

        return column;
    }

    private Optional<Element> createColumnModifiedElement(Document document, ColumnMetadata columnMetadata) {
        if (columnMetadata.isPrimaryKey()) {
            return Optional.empty();
        }
        String modifiedColumnName = columnMetadata.getName().replace("(_id)|(_.*Id)", "") + "Modified";
        Element auditColumn = document.createElement("column");
        auditColumn.setAttribute("name", modifiedColumnName);
        auditColumn.setAttribute("type", "boolean");
        return Optional.of(auditColumn);
    }
}
