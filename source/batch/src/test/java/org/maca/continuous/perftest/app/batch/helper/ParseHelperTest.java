package org.maca.continuous.perftest.app.batch.helper;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.maca.continuous.perftest.app.model.JUnitTestSuites;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(classes = ParseHelper.class)
class ParseHelperTest {

    @Autowired
    ParseHelper parseHelper;

    String criteria1 = "p90 of >0.0005s for 10s";
    String criteria2 = "failures of <50% for 60s";

    @Test
    void calculateResult() {
    }

    @Nested
    @ExtendWith(SpringExtension.class)
    @Tag("UnitTest")
    public class calculateCriteriaTest0 {
        @Test
        public void normalPassFailTest() {
            Resource resource = new ClassPathResource("artifacts/pass-fail/testcase0.xml");
            Resource[] results = {resource};
            List<JUnitTestSuites> testSuiteList = parseHelper.parseXml(results, JUnitTestSuites.class);

            Map<String, Long> errorList = parseHelper.calculateCriteria(testSuiteList);
            assertThat(errorList.get("Failed: response time"), is(2L));

        }
        @Test
        public void normalPassFailTest1() {
            Resource resource = new ClassPathResource("artifacts/pass-fail/testcase1.xml");
            Resource[] results = {resource};
            List<JUnitTestSuites> testSuiteList = parseHelper.parseXml(results, JUnitTestSuites.class);

            Map<String, Long> errorList = parseHelper.calculateCriteria(testSuiteList);
            assertThat(errorList.get("Failed: response time"), is(1L));

        }
        @Test
        public void normalPassFailTest2() {
            Resource resource = new ClassPathResource("artifacts/pass-fail/testcase2.xml");
            Resource[] results = {resource};
            List<JUnitTestSuites> testSuiteList = parseHelper.parseXml(results, JUnitTestSuites.class);

            Map<String, Long> errorList = parseHelper.calculateCriteria(testSuiteList);
            assertThat(errorList.isEmpty(), is(true));
        }
        @Test
        public void normalPassFailTest3() {
            Resource resource = new ClassPathResource("artifacts/pass-fail/testcase3.xml");
            Resource[] results = {resource};
            List<JUnitTestSuites> testSuiteList = parseHelper.parseXml(results, JUnitTestSuites.class);

            Map<String, Long> errorList = parseHelper.calculateCriteria(testSuiteList);
            assertThat(errorList.isEmpty(), is(true));
        }

    }

    @Nested
    @ExtendWith(SpringExtension.class)
    @Tag("UnitTest")
    public class parseXmlTest {
        @Test
        public void normalPassFailTest() {
            Resource resource = new ClassPathResource("artifacts/pass-fail/testcase0.xml");
            Resource[] results = {resource};
            List<JUnitTestSuites> testSuiteList = parseHelper.parseXml(results, JUnitTestSuites.class);
            assertThat(testSuiteList.get(0).getTestSuites().get(0).getTestCases().get(0).getName(), is(criteria1));
            assertThat(testSuiteList.get(0).getTestSuites().get(0).getTestCases().get(1).getName(), is(criteria2));
        }
        @Test
        public void abnormalPassFailTest1() {
            Resource resource = new ClassPathResource("artifacts/pass-fail/testcase1.xml");
            Resource[] results = {resource};
            List<JUnitTestSuites> testSuiteList = parseHelper.parseXml(results, JUnitTestSuites.class);
            assertThat(testSuiteList.get(0).getTestSuites().get(0).getTestCases().get(0).getError(), notNullValue());
            assertThat(testSuiteList.get(0).getTestSuites().get(0).getTestCases().get(1).getError(), nullValue());
        }
        @Test
        public void abnormalPassFailTest2() {
            Resource resource = new ClassPathResource("artifacts/pass-fail/testcase2.xml");
            Resource[] results = {resource};
            List<JUnitTestSuites> testSuiteList = parseHelper.parseXml(results, JUnitTestSuites.class);
            assertThat(testSuiteList.get(0).getTestSuites().get(0).getTestCases().get(0).getError(), nullValue());
            assertThat(testSuiteList.get(0).getTestSuites().get(0).getTestCases().get(1).getError(), nullValue());
        }
        @Test
        public void abnormalPassFailTest3() {
            Resource resource = new ClassPathResource("artifacts/pass-fail/testcase3.xml");
            Resource[] results = {resource};
            List<JUnitTestSuites> testSuiteList = parseHelper.parseXml(results, JUnitTestSuites.class);
            assertThat(testSuiteList.get(0).getTestSuites().get(0).getTestCases(), nullValue());
        }

    }
}