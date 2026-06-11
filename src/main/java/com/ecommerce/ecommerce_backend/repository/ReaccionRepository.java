package com.ecommerce.ecommerce_backend.repository;

import com.ecommerce.ecommerce_backend.entity.Reaccion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReaccionRepository extends MongoRepository<Reaccion, String> {
    List<Reaccion> findByResenaId(String resenaId);
    List<Reaccion> findByPublicacionId(String publicacionId);
    Optional<Reaccion> findByUsuarioIdAndResenaId(Integer usuarioId, String resenaId);
    Optional<Reaccion> findByUsuarioIdAndPublicacionId(Integer usuarioId, String publicacionId);
    long countByResenaIdAndTipo(String resenaId, String tipo);
    long countByPublicacionIdAndTipo(String publicacionId, String tipo);
}
