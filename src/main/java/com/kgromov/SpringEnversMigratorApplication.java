package com.kgromov;

import com.kgromov.config.DataSourceSettings;
import com.kgromov.model.TableMetadata;
import com.kgromov.service.ChangelogReader;
import com.kgromov.service.ChangelogWriter;
import com.kgromov.service.LiquibaseService;
import com.kgromov.service.TableMetadataExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.ResourceUtils;
import org.w3c.dom.Document;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties(DataSourceSettings.class)
public class SpringEnversMigratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringEnversMigratorApplication.class, args);
	}

	@Bean
	ApplicationRunner applicationRunner(DataSourceSettings settings,
										TableMetadataExtractor tableMetadataExtractor,
										LiquibaseService liquibaseService ) {
		return args -> {
			Set<String> params = args.getOptionNames();
			params.forEach(param -> log.info("{} = {}", param, args.getOptionValues(param)));

			TableMetadata tableMetadata = tableMetadataExtractor.getTableMetadata();
			URL templateURI = ResourceUtils.getURL("src/main/resources/migration-template.xml");
			Path templatePath = Path.of(templateURI.toURI());
			String changeLogFileName = "add" + tableMetadata.getName().substring(0, 1).toUpperCase() + tableMetadata.getName().substring(1) + ".xml";
			Path changeLogPath = Paths.get(".").normalize()
					.resolve("output")
					.resolve("changelogs")
					.resolve(changeLogFileName);
			Files.createDirectories(changeLogPath.getParent());
			Files.copy(templatePath, changeLogPath, REPLACE_EXISTING);
			ChangelogWriter changelogWriter = new ChangelogWriter();
			Document changeLogDocument = new ChangelogReader().readChangelog(changeLogPath);
			changeLogDocument = liquibaseService.buildCreateAuditTableChangeSet(changeLogDocument, tableMetadata);
			changeLogDocument = liquibaseService.buildInitRevisionForAuditTableChangeSet(changeLogDocument, tableMetadata);
			changelogWriter.writeToChangelogFile(changeLogDocument, changeLogPath);
		};
	}

}
