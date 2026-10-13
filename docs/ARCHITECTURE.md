# Architecture

## V3 — launcher, copie locale et Panic

`app-launcher` reste un APK HOME natif Android 5+ avec huit raccourcis, diagnostics, thème noir et blanc et accès aux applications système photo/vidéo/galerie. Le même APK installé sur deux téléphones contient un récepteur au premier plan, un client de synchronisation, un lecteur des copies reçues et une procédure Panic guidée. `core-device`, `core-profiles`, `core-journal`, `core-debloat` et `core-recovery` gardent le diagnostic et la simulation de débloat de V2. Aucun profil constructeur vérifié ni backend ADB/Shizuku de débloat n'est livré.

`core-replication` réalise l'authentification du pair, le chiffrement de session, l'échange de manifestes, la copie A↔B par blocs, la reprise, la vérification SHA-256 et le reçu authentifié. `GalleryRepository` expose les images et vidéos de MediaStore ainsi que les copies reçues dans le stockage privé de l'application. Seules les copies terminées et vérifiées figurent dans le manifeste. Les suppressions locales ne sont jamais propagées à l'autre téléphone.

La planification Android `JobScheduler` demande une tentative après 10, 15, 20 ou 30 minutes, sans garantie d'exécution exacte. Le service de réception est lancé par l'utilisateur ; Android tente de le redémarrer après reboot si l'option est active. Le réseau est un Wi-Fi local ou hotspot déjà établi ; l'utilisateur saisit l'adresse IPv4 privée et le code partagé. Aucun canal radio, groupe Wi-Fi Direct ou pair Bluetooth n'est choisi par l'application.

Panic propose successivement la désinstallation des applications de communication reconnues, une synchronisation complète, l'effacement de la galerie via les confirmations Android, la désinstallation de Shizuku/Canta, puis celle de Minimal OS. La galerie n'est proposée à l'effacement qu'après preuve que chaque contenu encore présent existe sur le pair. Les refus et les erreurs arrêtent la procédure. Les données internes des messageries ne sont pas couvertes par la copie. Le launcher constructeur et les composants système ne sont jamais visés.

La V3 est compilée et testée automatiquement, mais les parcours Android réels dépendent des permissions, du firmware et du réseau : voir [PHONE_PAIRING.md](PHONE_PAIRING.md) et [TEST_PLAN.md](TEST_PLAN.md).
