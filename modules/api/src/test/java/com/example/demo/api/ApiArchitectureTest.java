package com.example.demo.api;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * api 모듈 의존 방향 가드.
 *
 * <ul>
 *   <li>허용: user(application port/in/domain), query/*, contracts, infra_shard, infra_heartbeat,
 *           java/jakarta/spring/lombok/fasterxml/reactor/micrometer</li>
 *   <li>금지: write-side 도메인 모듈 직접 import — market_data, meta_data, analytics(typo: analystics),
 *           ingestion(이전 오타 ingection 디렉터리 — 패키지명은 ingestion), economic_ind shard/crawling</li>
 * </ul>
 */
@AnalyzeClasses(
        packages = "com.example.demo.api",
        importOptions = {ImportOption.DoNotIncludeTests.class}
)
public class ApiArchitectureTest {

    @ArchTest
    static final ArchRule api_must_not_depend_on_write_side =
            noClasses().that().resideInAPackage("com.example.demo.api..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "com.example.demo.market_data..",
                            "com.example.demo.meta_data..",
                            "com.example.demo.analystics..",
                            "com.example.demo.ingestion.economic.economic_ind..",
                            "com.example.demo.economic.crawling..",
                            "com.example.demo.ingestion.exchange..",
                            "com.example.demo.ingestion.fx..");

    @ArchTest
    static final ArchRule api_must_not_depend_on_user_infrastructure =
            noClasses().that().resideInAPackage("com.example.demo.api..")
                    .should().dependOnClassesThat().resideInAPackage(
                            "com.example.demo.user.infrastructure..");
}
