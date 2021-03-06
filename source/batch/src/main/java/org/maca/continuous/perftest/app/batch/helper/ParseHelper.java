package org.maca.continuous.perftest.app.batch.helper;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.maca.continuous.perftest.app.model.*;
import org.maca.continuous.perftest.common.apinfra.exception.SystemException;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ParseHelper {
    public Result calculateResult (List<FinalStatus> finalStatusList) throws SystemException {
        List<Group> grpByLabel = finalStatusList.stream()
                .flatMap(g -> g.getGroupList().stream())
                .collect(Collectors.groupingBy(Group::getLabel)).get("");

        if (grpByLabel.isEmpty()) {
            throw new SystemException("500", "finalStatus list is empty");
        }

        return Result.builder()
                .testDuration(finalStatusList.stream()
                        .map(FinalStatus::getTestDuration)
                        .mapToDouble(v -> v)
                        .average().orElse(0.0))
                .throughput(grpByLabel.stream()
                        .map(Group::getThroughput)
                        .mapToLong(CountMetrics::getValue).sum())
                .concurrency(grpByLabel.stream()
                        .map(Group::getConcurrency)
                        .mapToLong(CountMetrics::getValue).sum())
                .success(grpByLabel.stream()
                        .map(Group::getSuccess)
                        .mapToLong(CountMetrics::getValue).sum())
                .fail(grpByLabel.stream()
                        .map(Group::getFail)
                        .mapToLong(CountMetrics::getValue).sum())
                .avgResponseTime(grpByLabel.stream()
                        .map(Group::getAvgResponseTime)
                        .mapToDouble(TimeMetrics::getValue)
                        .average().orElse(0.0))
                .perc90(grpByLabel.stream()
                        .map(Group::getPercentiles)
                        .flatMap(Collection::stream)
                        .filter(p -> p.getParam().equals("90.0"))
                        .collect(Collectors.averagingDouble(Percentile::getValue)))
                .build();
    }

    public Map<String, Long> calculateCriteria (List<JUnitTestSuites> testSuiteList) {
        return testSuiteList.stream()
                .flatMap(t -> t.getTestSuites().stream())
                .filter(t -> Objects.nonNull(t.getTestCases()))
                .flatMap(t -> t.getTestCases().stream())
                .map(JUnitTestCase::getError)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(JUnitError::getMessage, Collectors.counting()));
    }

    public <T> List<T> parseXml(Resource[] resources, Class<T> mapperClass) {
        XmlMapper xmlMapper = new XmlMapper();
        List<T> arrayList = new ArrayList<>();
        for (Resource resource : resources) {
            try (InputStream inputStream = resource.getInputStream()) {
                arrayList.add(xmlMapper.readValue(
                        IOUtils.toString(inputStream, StandardCharsets.UTF_8),
                        mapperClass
                ));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return arrayList;
    }

}
