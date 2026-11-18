package ma.ensam.clientpulse.dao.jpa;
import jakarta.persistence.LockModeType;
import ma.ensam.clientpulse.domain.entity.SequenceNumerotation;
import ma.ensam.clientpulse.domain.enums.TypeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SequenceNumerotationJpaRepository extends JpaRepository<SequenceNumerotation,Long>{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SequenceNumerotation s where s.type=:type and s.annee =:annee")
    Optional<SequenceNumerotation> chercherEtVerrouiller(@Param("type") TypeDocument type, @Param("annee") int annee);
}
