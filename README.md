# Minimal OS

Minimal OS est un launcher Android sobre destiné aux téléphones modestes. La V7 affiche huit raccourcis, leurs notifications, un bouton Internet et une copie bidirectionnelle chiffrée des photos et vidéos par **Bluetooth direct** ou **Wi-Fi Direct**. Aucun Wi-Fi commun ni hotspot n'est nécessaire. La V7 corrige le changement de récepteur Bluetooth/Wi-Fi Direct et accepte un code simple de 5 chiffres et ajoute un test de liaison sans copie ; Panic exige une réponse authentifiée avant les confirmations Android ; l'audit et les profils de V2 restent disponibles.

## Télécharger

La version courante est [`build/V7/Minimal-OS-V7-debug.apk`](build/V7/Minimal-OS-V7-debug.apk). **Installez V7 sur les deux téléphones** ; elle ne synchronise pas avec V6. Les versions précédentes restent dans [`build/`](build/README.md). Consultez [le guide A↔B](docs/PHONE_PAIRING.md) avant d'utiliser Panic.

## Construire

Prérequis : JDK 17 ou 21, Android SDK Platform 35 et Build Tools 35.0.0. Définir `ANDROID_HOME` ou créer un `local.properties` non suivi contenant `sdk.dir=/chemin/du/sdk`.

```sh
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

L'APK est produit dans `app-launcher/build/outputs/apk/debug/`. Installez-le uniquement sur un appareil déjà configuré, puis utilisez « Choisir le launcher HOME » : Android affiche son sélecteur officiel. Minimal OS ne modifie jamais le launcher constructeur.

Consultez [l'architecture](docs/ARCHITECTURE.md), [la compatibilité](docs/COMPATIBILITY.md), [les règles de sécurité](docs/SAFETY.md), [la récupération](docs/RECOVERY.md), [le protocole de transfert](docs/TRANSFER_PROTOCOL.md) et [le plan de test](docs/TEST_PLAN.md). Les transports radio et Panic n'ont pas encore été validés sur deux appareils physiques.
