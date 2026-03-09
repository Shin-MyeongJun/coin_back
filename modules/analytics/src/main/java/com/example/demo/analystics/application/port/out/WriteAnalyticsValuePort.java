package com.example.demo.analystics.application.port.out;

import java.util.List;

public interface WriteAnalyticsValuePort<DOMAIN> {

    void write(List<DOMAIN> domains);
}
