package com.example.demo.market_data.domain.buffer.base;

import java.util.List;

public interface PriceValueBuffer<DOMAIN> {


    void add(DOMAIN domain);
    List<DOMAIN> flush();

}
