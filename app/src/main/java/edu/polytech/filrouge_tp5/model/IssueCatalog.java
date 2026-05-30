package edu.polytech.filrouge_tp5.model;

import java.util.ArrayList;

public final class IssueCatalog {
    private IssueCatalog() {
    }

    public static ArrayList<Issue> createDefaultIssues() {
        ArrayList<Issue> issueList = new ArrayList<>();
        issueList.add(new HighwayIssue("Accident - A8", "Deux vehicules, voie de droite bloquee", Issue.Priority.CRITICAL, Issue.Status.ON_SITE, "A8 km 12.3", 43.7034, 7.2663));
        issueList.add(new UrbanIssue("Accident velo - Antibes", "Collision velo-voiture pres du boulevard Wilson", Issue.Priority.HIGH, Issue.Status.CONFIRMED, "Antibes, Bd Wilson", 43.5808, 7.1228));
        issueList.add(new HighwayIssue("Vehicule a contresens", "Signalement sur rocade Sud sortie 12", Issue.Priority.CRITICAL, Issue.Status.REPORTED, "Rocade Sud", 43.2783, 5.3934));
        issueList.add(new UrbanIssue("Obstacle sur chaussee", "Palettes tombees sur la voie centrale", Issue.Priority.HIGH, Issue.Status.CONFIRMED, "Avenue Jean Jaures", 43.5538, 7.0177));
        issueList.add(new HighwayIssue("Bouchon massif", "Ralentissement de 5 km apres retrecissement", Issue.Priority.HIGH, Issue.Status.REPORTED, "A7 nord", 44.1367, 4.8098));
        issueList.add(new UrbanIssue("Travaux de nuit", "Circulation sur une seule voie", Issue.Priority.MEDIUM, Issue.Status.ON_SITE, "Centre-ville Nice", 43.6963, 7.2710));
        issueList.add(new HighwayIssue("Brouillard givrant", "Visibilite inferieure a 50 metres", Issue.Priority.MEDIUM, Issue.Status.CONFIRMED, "A8 secteur forestier", 43.4048, 5.9000));
        issueList.add(new UrbanIssue("Inondation chaussee", "Forte pluie, risque d'aquaplaning", Issue.Priority.MEDIUM, Issue.Status.REPORTED, "Frejus bretelle", 43.4329, 6.7368));
        issueList.add(new HighwayIssue("Verglas sur pont", "Pont glissant, saleuse en route", Issue.Priority.HIGH, Issue.Status.ON_SITE, "Pont suspendu A57", 43.1239, 5.9275));
        issueList.add(new UrbanIssue("Vehicule en panne", "Voiture arretee, triangle pose", Issue.Priority.LOW, Issue.Status.CLEARING, "D10 Grasse", 43.6600, 6.9200));
        return issueList;
    }
}
