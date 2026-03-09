package com.example.demo.analystics.domain.domain.key;

public interface DataKey<SELF extends DataKey<SELF>> {
    boolean equals(SELF self);
}
