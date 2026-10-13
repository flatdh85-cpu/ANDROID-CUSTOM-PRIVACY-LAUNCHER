# Architecture

## Périmètre de la Phase 1

Le MVP est un APK HOME natif, sans WebView, télémétrie, service permanent ni accès réseau. Il affiche une horloge, l'état batterie/réseau, six applications de communication et des raccourcis vers les réglages officiels. Une application absente reste visible et produit un message explicite.

## Modules

- `app-launcher` : activité HOME, UI à faible coût, catalogue des six applications et diagnostics Android.
- `core-device` : contrats `DeviceDetector` et `PackageInventory`, modèles sans dépendance Android.
- `core-profiles` : résultat de correspondance exacte d'un profil. Aucun profil constructeur n'est livré en Phase 1.
- `core-journal` : contrat `RecoveryJournal` et implémentation mémoire de test.
- `core-debloat` : contrats `DebloatPlanner`/`DebloatExecutor` et implémentations audit-only qui ne produisent aucune action.
- `core-recovery` : contrat `RedButtonCoordinator` et implémentation indisponible sûre pour le MVP.

Les modules métier sont en Java pur afin de partager ultérieurement leurs tests entre variantes legacy et modern. L'APK actuel a `minSdk 21` (Android 5.0), `targetSdk 35` et emploie uniquement les API du framework.

## Flux du launcher

1. Android lance `MainActivity` après choix explicite de l'utilisateur dans le sélecteur HOME.
2. `AndroidDeviceDetector` lit fabricant, modèle, API et fingerprint ; `AndroidPackageInventory` interroge uniquement les six packages déclarés dans `<queries>`.
3. Les tuiles disponibles lancent l'intent public du package. Les tuiles absentes affichent « non installée » ; aucune source APK n'est devinée.
4. Les diagnostics lisent batterie et connectivité et permettent d'ouvrir les réglages Android.
5. Les interfaces debloat et récupération restent audit-only/inactives. Leur activation nécessite les phases dédiées, profils exacts et confirmations.

## Décisions de sécurité

La Phase 1 ne demande ni root, ni Shizuku, ni permission de gestion de packages. Elle ne possède aucun backend ADB, ne modifie aucun package et n'écrit aucun journal de restauration durable. La sélection HOME et les réglages passent exclusivement par des intents Android officiels.

## Extensions prévues

Les modules d'intégration Shizuku, dépôt APK, compagnon de récupération, caméra, coffre et réplication seront ajoutés dans leurs phases respectives. Ils ne doivent pas rendre le launcher indispensable au stockage des médias ni contourner les confirmations du système.
