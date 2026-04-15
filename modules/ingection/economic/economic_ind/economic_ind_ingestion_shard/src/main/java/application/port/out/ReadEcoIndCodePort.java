package application.port.out;

import infrastructure.persistence.entity.EcoIndCodeEntity;

import java.util.List;

public interface ReadEcoIndCodePort {
    List<EcoIndCodeEntity> readAll();
}
