package edu.polytech.filrouge_tp5.model;

import java.util.ArrayList;

public final class IssueCatalog {
    private IssueCatalog() {
    }

    public static ArrayList<Issue> createDefaultIssues() {
        ArrayList<Issue> issueList = new ArrayList<>();
        issueList.add(attach(new HighwayIssue("Accident - A8", "Deux vehicules, voie de droite bloquee", Issue.Priority.CRITICAL, Issue.Status.ON_SITE, "A8 km 12.3", 43.6166, 7.0728)));
        issueList.add(attach(new UrbanIssue("Accident velo - Antibes", "Collision velo-voiture pres du boulevard Wilson", Issue.Priority.HIGH, Issue.Status.CONFIRMED, "Antibes, Bd Wilson", 43.6146, 7.0708)));
        issueList.add(attach(new HighwayIssue("Vehicule a contresens", "Signalement sur rocade Sud sortie 12", Issue.Priority.CRITICAL, Issue.Status.REPORTED, "Rocade Sud", 43.6186, 7.0768)));
        issueList.add(attach(new UrbanIssue("Obstacle sur chaussee", "Palettes tombees sur la voie centrale", Issue.Priority.HIGH, Issue.Status.CONFIRMED, "Avenue Jean Jaures", 43.6126, 7.0688)));
        issueList.add(attach(new HighwayIssue("Bouchon massif", "Ralentissement de 5 km apres retrecissement", Issue.Priority.HIGH, Issue.Status.REPORTED, "A7 nord", 43.6226, 7.0788)));
        issueList.add(attach(new UrbanIssue("Travaux de nuit", "Circulation sur une seule voie", Issue.Priority.MEDIUM, Issue.Status.ON_SITE, "Centre-ville", 43.6116, 7.0678)));
        issueList.add(attach(new HighwayIssue("Brouillard givrant", "Visibilite inferieure a 50 metres", Issue.Priority.MEDIUM, Issue.Status.CONFIRMED, "Secteur forestier", 43.6106, 7.0668)));
        issueList.add(attach(new UrbanIssue("Inondation chaussee", "Forte pluie, risque d'aquaplaning", Issue.Priority.MEDIUM, Issue.Status.REPORTED, "Bretelle d'acces", 43.6096, 7.0658)));
        issueList.add(attach(new HighwayIssue("Verglas sur pont", "Pont glissant, saleuse en route", Issue.Priority.HIGH, Issue.Status.ON_SITE, "Pont suspendu", 43.6086, 7.0648)));
        issueList.add(attach(new UrbanIssue("Vehicule en panne", "Voiture arretee, triangle pose", Issue.Priority.LOW, Issue.Status.CLEARING, "D10", 43.6076, 7.0638)));
        return issueList;
    }

    private static Issue attach(Issue issue) {
        issue.addObserver(EmergencyService.getInstance());
        return issue;
    }
}
