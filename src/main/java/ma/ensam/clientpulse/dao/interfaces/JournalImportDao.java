package ma.ensam.clientpulse.dao.interfaces;

import ma.ensam.clientpulse.domain.entity.JournalImport;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Accès au journal des imports CSV. */
public interface JournalImportDao {

    JournalImport save(JournalImport journal);

    Page<JournalImport> findAll(Pageable pageable);
}
