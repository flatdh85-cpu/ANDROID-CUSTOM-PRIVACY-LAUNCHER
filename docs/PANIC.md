# Panic V8

## Activer l’auto-validation une fois

1. Sur l’accueil, maintenir **Panic** pour ouvrir sa configuration.
2. Toucher **Configurer l’auto-validation Panic**, lire et accepter le périmètre : LocalSend, BitChat, SimpleX, toutes les photos/vidéos accessibles de la galerie, puis Minimal OS et ses anciennes copies privées. Aucune sauvegarde n’est lancée.
3. Dans les paramètres d’accessibilité Android, activer **Panic — auto-validation** pour Minimal OS. Cette autorisation doit être accordée manuellement. Certains Android récents bloquent les services d’une APK installée hors magasin : ouvrir les informations de l’application et, si Android le propose, autoriser ses paramètres restreints avant de revenir à l’accessibilité.
4. Autoriser l’accès **complet** aux photos/vidéos dans les permissions Minimal OS. Sur Android 5–10, l’accès stockage peut aussi être requis.
5. Revenir à l’accueil. **Le prochain appui long sur Panic déclenche la procédure directement**, sans confirmation supplémentaire dans Minimal OS. Ne pas le tester sur une galerie à conserver.

La suppression commence sans connexion ni recherche d’un autre téléphone. Les applications absentes sont ignorées. Le launcher constructeur est ouvert avant la dernière demande de désinstallation. Android peut proposer un sélecteur HOME si plusieurs launchers restent disponibles.

Pour modifier/désactiver l’automatisation sans lancer Panic, ouvrir les paramètres de ce service depuis l’accessibilité Android (bouton paramètres si disponible), ou désactiver le service dans Android puis rouvrir Panic. Le bouton **Désactiver l’auto-validation** retire aussi l’autorisation interne.

## Ce que fait l’automatisation

Les dialogues Android existent toujours et peuvent apparaître brièvement. Le service essaie de cliquer uniquement sur le bouton positif d’une confirmation attendue pendant Panic, dans le package système vérifié. Il contrôle aussi le nom de l’application concernée et le texte du dialogue. Seuls les libellés français/anglais et les identifiants standards actuellement documentés sont reconnus.

Un dialogue constructeur différent, une fenêtre protégée contre l’accessibilité, un clic refusé ou l’expiration de 20 secondes laisse la procédure en attente de validation manuelle. Aucun succès n’est annoncé à la place d’Android. Ce mécanisme n’est ni root, ni une suppression silencieuse privilégiée. Il ne peut pas activer sa propre autorisation d’accessibilité ni contourner une protection Android.

## Périmètre de suppression

- LocalSend `org.localsend.localsend_app`, SimpleX `chat.simplex.app`, BitChat `com.bitchat.droid` (repli connu `com.bitchat.android`), si présents et non système ; leurs données privées disparaissent selon la désinstallation Android.
- Photos/vidéos accessibles via MediaStore sur les volumes externes montés, dont carte SD et corbeille/éléments en attente visibles sur Android 11+. Les éléments inaccessibles à l’application, les coffres tiers, comptes distants et sauvegardes cloud ne sont pas effacés. Un refus ou une erreur est affiché.
- Minimal OS en dernier, avec son stockage privé et ses anciennes copies V3–V7.

Il n’y a aucun effacement sécurisé du support, aucune suppression des autres applications système ou utilisateur et aucun reset. Interrompre/refuser Panic peut laisser une suppression partielle ; rouvrir la procédure refait l’inventaire actuel. Les copies déjà présentes sur un autre téléphone ne sont pas touchées.

## V9 : contenu de Téléchargements

Panic efface aussi **tout le contenu du dossier public Download/Téléchargements du stockage principal**, après la galerie et avant Minimal OS : APK, ZIP, PDF, documents, fichiers cachés et sous-dossiers. Le dossier racine est conservé. Il ne parcourt pas les autres dossiers du gestionnaire de fichiers, ni les téléchargements enregistrés ailleurs ou sur une carte SD. Les liens symboliques ne sont pas suivis.

Sur Android 6–10, l’autorisation de stockage est utilisée. Sur Android 11+, ouvrir Panic puis **Autoriser l’effacement des téléchargements** et accorder une fois **Accès à tous les fichiers** à Minimal OS dans Android. Cette permission n’est pas auto-accordée ; son absence est détectée avant toute désinstallation. Bien qu’elle soit large, le code ne supprime que le dossier Téléchargements fixé ci-dessus et les médias du périmètre galerie.

Chaque suppression est vérifiée, puis le dossier est relu. En cas de refus, d’accès perdu ou de fichiers restants, Panic s’arrête avec un message et conserve Minimal OS. Un téléchargement tiers encore en cours peut recréer un fichier après le contrôle. Les entrées d’historique du gestionnaire Android appartenant à d’autres applications peuvent rester visibles, même si leur fichier a été supprimé ; leur historique privé n’est pas accessible à Minimal OS.
