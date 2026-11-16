package com.example.ddadang.domain.record.exercise.init;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * 운동 DB CSV(data/exercise.csv)를 읽는다.
 * 컬럼: 원본코드, 분류, 운동명(한글), MET, 30분 기준 소모(60kg, kcal), 영문명(원문), 일문명(원문).
 * "30분 기준 소모"는 MET에서 계산되는 참고값이라 읽지 않는다.
 */
@Component
public class ExerciseCsvReader {

    private static final String PATH = "data/exercise.csv";
    private static final int COLUMN_COUNT = 7;
    private static final char BOM = '﻿';

    public List<Row> read() {
        List<Row> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(new ClassPathResource(PATH).getInputStream(), StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // 헤더
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                List<String> columns = split(line);
                if (columns.size() != COLUMN_COUNT) {
                    throw new IllegalStateException(
                        "%s %d행: 컬럼이 %d개입니다(기대 %d개).".formatted(PATH, lineNumber, columns.size(), COLUMN_COUNT));
                }
                rows.add(new Row(
                    columns.get(0), columns.get(1), columns.get(2), new BigDecimal(columns.get(3)),
                    columns.get(5), columns.get(6)
                ));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(PATH + "을 읽지 못했습니다.", e);
        }
        return rows;
    }

    /**
     * 쉼표로 나누되 큰따옴표로 감싼 값 안의 쉼표와 ""(따옴표 이스케이프)를 처리한다.
     */
    private List<String> split(String line) {
        List<String> columns = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == BOM) {
                continue;
            }
            if (quoted) {
                if (c == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else if (c == '"') {
                    quoted = false;
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                columns.add(current.toString().strip());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        columns.add(current.toString().strip());
        return columns;
    }

    public record Row(String code, String category, String name, BigDecimal met, String nameEn, String nameJa) {
    }
}
