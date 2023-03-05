package com.kgromov.service;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;

@Component
public class ChangelogReader {

    @SneakyThrows
    public Document readChangelog(Path changelogFile) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(changelogFile.toFile());
    }
}
