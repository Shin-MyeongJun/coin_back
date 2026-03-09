package com.example.demo.infre_exchange.util;


import com.example.demo.infre_exchange.dto.stream.BinanceStreamFormat;

public class BinanceCustomUtil {

    private BinanceCustomUtil(){}
    public static <T> T getData(BinanceStreamFormat<T> data){
        return  data.data();
    }



}
