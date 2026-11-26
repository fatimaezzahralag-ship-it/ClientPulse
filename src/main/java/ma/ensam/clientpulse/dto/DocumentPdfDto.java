package ma.ensam.clientpulse.dto;

/** Un document PDF généré : nom de fichier proposé au téléchargement et contenu binaire. */
public record DocumentPdfDto(String nomFichier, byte[] contenu) {
}
