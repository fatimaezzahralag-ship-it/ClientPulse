package ma.ensam.clientpulse.dao.jpa.specification;

import ma.ensam.clientpulse.domain.entity.Client;
import ma.ensam.clientpulse.domain.enums.StatutClient;
import ma.ensam.clientpulse.domain.enums.TypeClient;

import org.springframework.data.jpa.domain.Specification;

public final class ClientSpecifications {

    private ClientSpecifications() {}

    public static Specification<Client> search(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }

            String pattern = "%" + keyword.trim().toLowerCase() + "%";

            return cb.or(
                cb.like(cb.lower(root.get("raisonSociale")), pattern),
                cb.like(cb.lower(root.get("code")), pattern),
                cb.like(cb.lower(root.get("ville")), pattern),
                cb.like(cb.lower(root.get("email")), pattern)
            );
        };
    }

    public static Specification<Client> hasStatut(
            StatutClient statut) {
        return (root, query, cb) ->
            statut == null
                ? cb.conjunction()
                : cb.equal(root.get("statut"), statut);
    }

    public static Specification<Client> hasType(TypeClient type) {
        return (root, query, cb) ->
            type == null
                ? cb.conjunction()
                : cb.equal(root.get("type"), type);
    }

    public static Specification<Client> hasVille(String ville) {
        return (root, query, cb) ->
            ville == null || ville.isBlank()
                ? cb.conjunction()
                : cb.like(
                    cb.lower(root.get("ville")),
                    "%" + ville.trim().toLowerCase() + "%"
                );
    }

    public static Specification<Client> hasCommercial(Long commercialId) {
        return (root, query, cb) ->
            commercialId == null
                ? cb.conjunction()
                : cb.equal(
                    root.get("commercial").get("id"),
                    commercialId
                );
    }
}
