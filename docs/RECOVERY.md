# Récupération

En Phase 2, l'audit, le plan de débloat et le bouton Panic restent sans effet sur les applications. Le journal persistant conserve les observations utiles à une future restauration, mais aucune désactivation ni désinstallation n'est exécutée. Pour quitter Minimal OS, ouvrez « Choisir le launcher HOME » ou Paramètres Android, puis sélectionnez le launcher constructeur. Si le fabricant masque ce réglage, utilisez Paramètres > Applications > Applications par défaut > Application d'accueil.

Un appui long sur « Panic — vérification » affiche uniquement le résultat du contrôle préalable. La suppression reste bloquée tant que le profil de l'appareil n'est pas exact, que la provenance des applications n'est pas prouvée, qu'une copie restaurable n'est pas confirmée et que les outils indépendants de récupération ne sont pas disponibles. Telegram, Briar et toute application présente avant Minimal OS ne doivent jamais être supprimés par ce mécanisme.

Le compagnon séparé et le script ADB de secours appartiennent à la Phase 4. Ne désactivez jamais le launcher constructeur. En cas d'écran HOME inutilisable, redémarrez en mode sans échec selon la procédure officielle du fabricant et rétablissez l'application d'accueil depuis les réglages. Une récupération ADB future devra commencer en dry-run, afficher l'appareil détecté et exiger une confirmation explicite.
