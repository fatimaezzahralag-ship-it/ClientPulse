package ma.ensam.clientpulse.dao.interfaces;
import ma.ensam.clientpulse.domain.entity.SequenceNumerotation;
import ma.ensam.clientpulse.domain.enums.TypeDocument;
public class SequenceNumerotationDao {
    SequenceNumerotation verrouiller(TypeDocument type, int annee);
    SequenceNumerotation save(SequenceNumerotation sequence);
}
