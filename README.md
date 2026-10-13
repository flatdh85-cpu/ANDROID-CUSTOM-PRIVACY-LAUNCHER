# Minimal OS

Minimal OS est un launcher Android sobre destiné aux téléphones modestes. La Phase 1 fournit un écran HOME, six raccourcis, des diagnostics locaux et des contrats audit-only pour les phases suivantes. Elle ne désactive, ne désinstalle et ne télécharge aucune application.

## Construire

Prérequis : JDK 17 ou 21, Android SDK Platform 35 et Build Tools 35.0.0. Définir `ANDROID_HOME` ou créer un `local.properties` non suivi contenant `sdk.dir=/chemin/du/sdk`.

```sh
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

L'APK est produit dans `app-launcher/build/outputs/apk/debug/`. Installez-le uniquement sur un appareil déjà configuré, puis utilisez « Choisir le launcher HOME » : Android affiche son sélecteur officiel. Minimal OS ne modifie jamais le launcher constructeur.

Consultez [l'architecture](docs/ARCHITECTURE.md), [la compatibilité](docs/COMPATIBILITY.md), [les règles de sécurité](docs/SAFETY.md), [la récupération](docs/RECOVERY.md) et [le plan de test](docs/TEST_PLAN.md).
