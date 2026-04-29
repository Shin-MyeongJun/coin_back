package com.example.demo.ingestion.economic.crawling.application.port.out;

import com.example.demo.ingestion.economic.crawling.domain.domain.GlobalIndex;

import java.util.List;

public interface FetchGlobalIndexPort {
    List<GlobalIndex> fetchAll();

    interface ForYahoo extends FetchGlobalIndexPort {}
    interface ForInvesting extends FetchGlobalIndexPort {}
}
