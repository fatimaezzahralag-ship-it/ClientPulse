package ma.ensam.clientpulse.dao.jpa.specification;

import ma.ensam.clientpulse.domain.entity.Interaction;
import ma.ensam.clientpulse.domain.enums.StatutInteraction;
import ma.ensam.clientpulse.domain.enums.TypeInteraction;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class InteractionSpecifications {

    private InteractionSpecifications() {}

    public static Specification<Interaction> belongsToClient(
            Long clientId) {

        return (root, query, cb) ->
            clientId == null
                ? cb.conjunction()
                : cb.equal(root.get("client").get("id"), clientId);
    }

    public static Specification<Interaction> hasType(
            TypeInteraction type) {

        return (root, query, cb) ->
            type == null
                ? cb.conjunction()
                : cb.equal(root.get("type"), type);
    }

    public static Specification<Interaction> hasStatut(
            StatutInteraction statut) {

        return (root, query, cb) ->
            statut == null
                ? cb.conjunction()
                : cb.equal(root.get("statut"), statut);
    }

    public static Specification<Interaction> afterDate(
            LocalDateTime dateDebut) {

        return (root, query, cb) ->
            dateDebut == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("date"), dateDebut);
    }

    public static Specification<Interaction> beforeDate(
            LocalDateTime dateFin) {

        return (root, query, cb) ->
            dateFin == null
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("date"), dateFin);
    }
}
