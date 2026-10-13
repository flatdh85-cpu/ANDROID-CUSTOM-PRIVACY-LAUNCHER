# Architecture

## V6 — launcher et radios directes

`app-launcher` est un APK HOME natif Android 5+ avec huit raccourcis, un bouton Internet ouvrant le navigateur Android, les intents photo/vidéo/galerie, les diagnostics et le thème noir et blanc. Il contient également la procédure Panic guidée, le service de réception au premier plan, la tâche planifiée et l'interface d'association. Les modules `core-device`, `core-profiles`, `core-journal`, `core-debloat` et `core-recovery` conservent l'audit et la simulation de débloat de V2 ; aucun profil constructeur vérifié ni backend privilégié n'est livré.

`core-replication` travaille sur des flux d'entrée/sortie. La même session authentifie le pair, chiffre les échanges, compare les manifestes, copie A↔B par blocs, reprend les interruptions et vérifie SHA-256 avant un reçu authentifié. `GalleryRepository` expose MediaStore et les copies privées. Les suppressions locales ne sont jamais propagées.

`BluetoothLink` choisit un appareil déjà associé par Android et transporte le protocole sur RFCOMM sans nouveau dialogue d'appairage à chaque connexion. La confidentialité et l'authentification reposent alors sur le protocole de l'application, pas sur le lien radio. `WifiDirectLink` recherche l'adresse radio d'un pair, négocie un groupe P2P et livre au client l'adresse du chef ; `ReceiverService` n'écoute TCP 38742 que sur l'adresse de ce groupe lorsqu'il est chef. Aucune adresse IP n'est demandée à l'utilisateur et aucun WLAN/hotspot partagé n'est requis. Le code de 32 caractères doit être identique sur les deux appareils. Le choix du transport et du pair est sauvegardé localement.

La V6 distingue une sonde `probe` d'une copie `sync` après l'authentification. La sonde reçoit une réponse chiffrée sans lecture de galerie ; Panic peut ainsi vérifier le pair avant toute désinstallation. La copie transmet un état chiffré de la galerie avant les manifestes pour rendre visibles les erreurs du récepteur. Le protocole V6 exige V6 sur les deux téléphones.

`CommunicationNotificationService` lit, après accord spécial Android, les notifications actives des huit applications du launcher. `CommunicationNotifications` ne garde les résumés qu'en mémoire. `MainActivity` affiche une liste limitée à huit éléments et un compteur sur chaque raccourci ; cet accès n'est pas nécessaire aux transferts.

`SyncJobService` programme une tentative après 10, 15, 20 ou 30 minutes sans condition de réseau Internet. Android peut retarder le job et limiter les radios en arrière-plan. Le récepteur est activé volontairement par l'utilisateur et redémarré au boot si cette option reste active. Panic vérifie la joignabilité directe avant les désinstallations, puis exige un reçu pour chaque média avant de proposer l'effacement de la galerie. Ses désinstallations et suppressions passent par Android ; les données internes des messageries ne sont pas copiées. Le launcher constructeur et les composants système ne sont pas visés.

Les API et le protocole sont compilés et testés automatiquement. La connexion réelle entre deux téléphones, la négociation P2P, les permissions constructeur et les redémarrages restent à valider sur matériel : voir [PHONE_PAIRING.md](PHONE_PAIRING.md) et [TEST_PLAN.md](TEST_PLAN.md).
