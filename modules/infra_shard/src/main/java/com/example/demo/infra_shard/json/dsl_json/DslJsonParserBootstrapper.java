package com.example.demo.infra_shard.json.dsl_json;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
class DslJsonParserBootstrapper {
    private final DslJsonParserManager manager;
    private final List<DslJsonParserRegistrar> registrars;

    public DslJsonParserBootstrapper(DslJsonParserManager manager,List<DslJsonParserRegistrar> registrars ){
        this.manager = manager;
        this.registrars = registrars;
        registrars.forEach(rr -> rr.register(manager));
    }

}
