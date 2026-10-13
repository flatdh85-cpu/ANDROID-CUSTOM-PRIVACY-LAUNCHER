# Architecture

## Périmètre actuel — Phase 2

L'application est un APK HOME natif, sans WebView, télémétrie, service permanent ni accès réseau. Elle affiche une horloge, l'état batterie/réseau, six applications de communication, des raccourcis vers les réglages officiels et l'accès aux applications système photo, vidéo et galerie. Une application absente reste visible et produit un message explicite.

## Modules

- `app-launcher` : activité HOME, UI à faible coût, catalogue des six applications et diagnostics Android.
- `core-device` : contrats `DeviceDetector` et `PackageInventory`, modèles sans dépendance Android.
- `core-profiles` : schéma versionné, parseur validant et correspondance exacte fabricant/modèle/API/version/fingerprint. Aucun profil constructeur non vérifié n'est livré.
- `core-journal` : journal persistant par trames, SHA-256, synchronisation disque, reprise après dernière écriture interrompue et export texte.
- `core-debloat` : simulation depuis un profil exact, filtre indépendant des packages critiques et exécuteur audit-only.
- `core-recovery` : préflight Panic qui bloque tant que launcher natif, journal, provenance des installations et réplication ne sont pas tous vérifiés.
- `core-replication` : comparaison bidirectionnelle des manifestes, reçus taille/SHA-256/écriture durable et sélection abstraite du transport. Aucun accès radio n'est encore connecté.

Les modules métier sont en Java pur afin de partager ultérieurement leurs tests entre variantes legacy et modern. L'APK actuel a `minSdk 21` (Android 5.0), `targetSdk 35` et emploie uniquement les API du framework.

## Flux du launcher

1. Android lance `MainActivity` après choix explicite de l'utilisateur dans le sélecteur HOME.
2. `AndroidDeviceDetector` lit fabricant, modèle, API et fingerprint ; `AndroidPackageInventory` interroge uniquement les six packages déclarés dans `<queries>`.
3. Les tuiles disponibles lancent l'intent public du package. Les tuiles absentes affichent « non installée » ; aucune source APK n'est devinée.
4. Les diagnostics inventorient les packages visibles, cherchent un profil exact, simulent un plan et écrivent un événement durable exportable.
5. L'exécuteur et le Panic restent inactifs. Leur activation nécessite les phases dédiées, une provenance complète et les confirmations Android.

## Décisions de sécurité

La Phase 2 ne demande ni root, ni Shizuku, ni permission de modification de packages. Elle ne possède aucun backend ADB et ne modifie aucun package. `QUERY_ALL_PACKAGES` sert uniquement à l'inventaire local ; cette permission limite une éventuelle publication Play Store et devra être justifiée ou remplacée par un flux ADB/Shizuku. La sélection HOME, photo, vidéo, galerie et réglages passent par des intents Android officiels.

## Extensions prévues

Les modules d'intégration Shizuku, dépôt APK, compagnon de récupération, caméra privée, coffre et réplication seront ajoutés dans leurs phases respectives. Le transport préparé pour les foules denses est décrit dans [TRANSFER_PROTOCOL.md](TRANSFER_PROTOCOL.md). Ces composants ne doivent pas rendre le launcher indispensable au stockage des médias ni contourner les confirmations du système.
