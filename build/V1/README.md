# Minimal OS V1 — Launcher MVP

## Artefact

- Fichier : [`Minimal-OS-V1-debug.apk`](Minimal-OS-V1-debug.apk)
- Application ID : `org.minimalos.launcher`
- Version Android : `0.1.0` (`versionCode 1`)
- Compatibilité déclarée : Android 5.0/API 21 à Android 15/API 35
- SHA-256 : `92ef2d396898758f19a45f0d9dcacdefe752ec5f47decce41f69bcfdaf024a65`
- Signature : clé debug Android, schémas APK v1 et v2 vérifiés

Cet APK est un artefact de démonstration. La signature debug n'est pas une clé de publication durable et devra être remplacée avant une distribution de production.

## Architecture V1

V1 fournit un launcher HOME natif sans AndroidX, WebView, télémétrie, service permanent ou téléchargement d'APK. Il affiche Telegram, Canta, Shizuku, SimpleX Chat, Briar et LocalSend, indique si chaque application est disponible et ouvre les réglages Android en cas d'absence.

Les modules `core-device`, `core-profiles`, `core-journal`, `core-debloat` et `core-recovery` exposent les contrats des phases suivantes. Le moteur de debloat et le Red Button sont volontairement inactifs : V1 ne désactive et ne désinstalle aucun package.

La compilation, sept scénarios de test distincts et Android Lint ont réussi dans l'environnement cloud. Les tests physiques Wiko, Huawei, Samsung et Xiaomi restent à effectuer.
