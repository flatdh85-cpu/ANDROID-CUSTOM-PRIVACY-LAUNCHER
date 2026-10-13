# Minimal OS

Minimal OS est un launcher Android sobre destiné aux téléphones modestes. La V3 affiche huit raccourcis et ajoute une copie bidirectionnelle chiffrée des photos et vidéos sur Wi-Fi local ou hotspot, ainsi qu'une procédure Panic guidée par les confirmations Android. L'audit et les profils de la V2 restent disponibles.

## Télécharger

La version courante est [`build/V3/Minimal-OS-V3-debug.apk`](build/V3/Minimal-OS-V3-debug.apk). Les V1 et V2 restent dans [`build/`](build/README.md). Consultez [le guide A↔B](docs/PHONE_PAIRING.md) avant d'utiliser Panic.

## Construire

Prérequis : JDK 17 ou 21, Android SDK Platform 35 et Build Tools 35.0.0. Définir `ANDROID_HOME` ou créer un `local.properties` non suivi contenant `sdk.dir=/chemin/du/sdk`.

```sh
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

L'APK est produit dans `app-launcher/build/outputs/apk/debug/`. Installez-le uniquement sur un appareil déjà configuré, puis utilisez « Choisir le launcher HOME » : Android affiche son sélecteur officiel. Minimal OS ne modifie jamais le launcher constructeur.

Consultez [l'architecture](docs/ARCHITECTURE.md), [la compatibilité](docs/COMPATIBILITY.md), [les règles de sécurité](docs/SAFETY.md), [la récupération](docs/RECOVERY.md), [le protocole de transfert](docs/TRANSFER_PROTOCOL.md) et [le plan de test](docs/TEST_PLAN.md). Les fonctions A↔B et Panic n'ont pas encore été validées sur deux appareils physiques.
