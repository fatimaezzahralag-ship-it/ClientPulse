package ma.ensam.clientpulse.dao.jpa;

import ma.ensam.clientpulse.domain.entity.JournalImport;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalImportJpaRepository extends JpaRepository<JournalImport, Long> {
}
