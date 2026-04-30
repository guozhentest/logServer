package com.leantech.agent.task;

import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;

@Component
public class TrainingDataExporter {

    private static final Logger log = LoggerFactory.getLogger(TrainingDataExporter.class);
    private static final String EXPORT_PATH = "/training-data/weekly.jsonl";

    private final JdbcTemplate jdbcTemplate;

    public TrainingDataExporter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // @Scheduled(cron = "0 0 3 ? * MON")
    public void exportHighQualityData() {
        List<Map<String, Object>> data = jdbcTemplate.queryForList(
                "SELECT user_question, assistant_answer FROM agent_feedback WHERE rating >= 4 AND is_adopted = false"
        );
        if (data.isEmpty()) {
            return;
        }

        File file = new File(EXPORT_PATH);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            log.warn("创建训练数据目录失败: {}", parent.getAbsolutePath());
            return;
        }

        try (FileWriter writer = new FileWriter(file)) {
            for (Map<String, Object> row : data) {
                writer.write(JSON.toJSONString(row));
                writer.write("\n");
            }
        } catch (Exception e) {
            log.error("导出训练数据失败", e);
            return;
        }

        jdbcTemplate.update("UPDATE agent_feedback SET is_adopted = true WHERE rating >= 4 AND is_adopted = false");
        log.info("训练数据导出完成，共 {} 条", data.size());
    }
}
