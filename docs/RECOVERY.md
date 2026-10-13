# Récupération

En Phase 1, aucune opération système n'est effectuée et aucun rollback n'est nécessaire. Pour quitter Minimal OS, ouvrez « Choisir le launcher HOME » ou Paramètres Android, puis sélectionnez le launcher constructeur. Si le fabricant masque ce réglage, utilisez Paramètres > Applications > Applications par défaut > Application d'accueil.

Le `RedButtonCoordinator` fourni est volontairement indisponible : il ne prétend pas restaurer ou désinstaller quoi que ce soit. Le compagnon séparé et le script ADB de secours appartiennent à la Phase 4, après implémentation d'un journal persistant et validation sur appareils secondaires.

Ne désactivez jamais le launcher constructeur. En cas d'écran HOME inutilisable, redémarrez en mode sans échec selon la procédure officielle du fabricant et rétablissez l'application d'accueil depuis les réglages. Une récupération ADB future devra commencer en dry-run, afficher l'appareil détecté et exiger une confirmation explicite.
