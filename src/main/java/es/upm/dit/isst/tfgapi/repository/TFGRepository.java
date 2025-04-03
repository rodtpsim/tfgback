package es.upm.dit.isst.tfgapi.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import es.upm.dit.isst.tfgapi.model.TFG;

public interface TFGRepository extends CrudRepository<TFG, String> {
    List<TFG> findByTutor(String tutor);
}
