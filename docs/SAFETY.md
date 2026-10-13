# Sécurité et limites V8

Minimal OS est une application Android, pas une ROM ni un outil d’effacement sécurisé. Une désinstallation peut laisser des sauvegardes, des fichiers partagés et des journaux système. Aucun résultat « sans trace » n’est promis.

La demande utilisateur V8 autorise explicitement la suppression de LocalSend, BitChat, SimpleX, de la galerie accessible et de Minimal OS, même si ces applications étaient préexistantes. **Panic ne sauvegarde rien et ne vérifie aucun pair.** Les applications système et les autres applications utilisateur ne sont pas ciblées. Un autre HOME actif est vérifié avant la première désinstallation et à nouveau avant celle de Minimal OS.

La demande supplémentaire d’auto-acceptation est implémentée via un service d’accessibilité facultatif. L’utilisateur doit activer cet accès dans Android et accepter une fois le périmètre dans Minimal OS. Ensuite un appui long sur Panic peut lancer immédiatement l’effacement. Le service ne valide que l’opération système attendue, avec contrôle du package système, du bouton positif, du texte et du nom d’application. L’autorisation de cliquer expire après 20 secondes et disparaît à la mort du processus. Les fenêtres non reconnues ou protégées restent manuelles. Aucun clic d’activation de permission d’accessibilité n’est automatisé, aucun texte d’écran n’est journalisé ni envoyé. [Détails et désactivation](PANIC.md).

Les appels Android peuvent refuser une désinstallation ou un effacement. Refuser/interrompre Panic laisse potentiellement une suppression partielle et irrécupérable localement ; aucun rollback n’est promis. L’inventaire est refait à la reprise. Les comptes cloud, coffres d’autres applications, autres profils utilisateurs, médias non exposés à MediaStore et volumes non montés ne sont pas accessibles à cette procédure. Les médias accessibles sont contrôlés après suppression avant de proposer le retrait de Minimal OS.

LocalSend est autonome. Aucun transfert de galerie ne tourne dans Minimal OS. Les autorisations et confirmations de réception LocalSend sont gérées dans cette application. Panic ne vérifie pas qu’une copie existe avant de supprimer les médias. Le fonctionnement de LocalSend sur Wi-Fi Direct reste à vérifier sur les appareils réels.

L’accès spécial aux notifications est facultatif et distinct de l’accessibilité Panic. Les notifications des trois applications sont gardées uniquement en mémoire pour les pastilles et la liste de l’accueil. Aucune notification n’est copiée vers un pair.

Le debloat constructeur reste en simulation. Les permissions larges de lecture média, d’inventaire de packages et d’accessibilité nécessitent une revue des règles de distribution avant une éventuelle publication Play Store. Les APK de ce dépôt sont des builds debug destinés aux essais.

V9 étend Panic aux fichiers du dossier public Téléchargements principal ; l’accès spécial aux fichiers sur Android 11+ reste manuel et son usage est limité dans le code. L’effacement du dossier ne supprime pas nécessairement les anciennes entrées du gestionnaire de téléchargement d’autres applications. Voir le périmètre précis dans [Panic](PANIC.md).

L’assistant d’installation V9 utilise uniquement des APK officielles épinglées, dont le fichier, le package et le certificat sont contrôlés avant le passage à l’installateur Android. Pas d’installation silencieuse ni d’auto-validation des permissions. Aucun APK tiers n’est redistribué. L’autorisation d’installation de paquets et celle d’accès à tous les fichiers impliquent une revue des règles de distribution avant toute publication Play Store.

Le raccourci Thème sombre ouvre uniquement les réglages Android : aucun changement privilégié de la configuration système, aucune promesse de thème imposé à toutes les applications.
