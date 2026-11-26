package ma.ensam.clientpulse.dao.impl;

import ma.ensam.clientpulse.dao.interfaces.JournalImportDao;
import ma.ensam.clientpulse.dao.jpa.JournalImportJpaRepository;
import ma.ensam.clientpulse.domain.entity.JournalImport;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class JournalImportDaoImpl implements JournalImportDao {

    private final JournalImportJpaRepository repository;

    public JournalImportDaoImpl(JournalImportJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public JournalImport save(JournalImport journal) {
        return repository.save(journal);
    }

    @Override
    public Page<JournalImport> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }
}
