# Minimal OS

Minimal OS est un launcher Android sobre destiné aux téléphones modestes. La Phase 2 fournit un écran HOME noir et blanc, six raccourcis, l'accès aux applications système photo/vidéo/galerie, un inventaire local, des profils exacts et un journal persistant. Elle ne désactive, ne désinstalle et ne télécharge aucune application.

## Télécharger

La version courante est [`build/V2/Minimal-OS-V2-debug.apk`](build/V2/Minimal-OS-V2-debug.apk). Son empreinte et ses limites sont documentées dans [`build/V2/README.md`](build/V2/README.md). La V1 reste disponible dans [`build/V1`](build/V1/README.md).

## Construire

Prérequis : JDK 17 ou 21, Android SDK Platform 35 et Build Tools 35.0.0. Définir `ANDROID_HOME` ou créer un `local.properties` non suivi contenant `sdk.dir=/chemin/du/sdk`.

```sh
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

L'APK est produit dans `app-launcher/build/outputs/apk/debug/`. Installez-le uniquement sur un appareil déjà configuré, puis utilisez « Choisir le launcher HOME » : Android affiche son sélecteur officiel. Minimal OS ne modifie jamais le launcher constructeur.

Consultez [l'architecture](docs/ARCHITECTURE.md), [la compatibilité](docs/COMPATIBILITY.md), [les règles de sécurité](docs/SAFETY.md), [la récupération](docs/RECOVERY.md), [le protocole de transfert](docs/TRANSFER_PROTOCOL.md) et [le plan de test](docs/TEST_PLAN.md).
