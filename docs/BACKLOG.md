# Backlog priorisé

## Réalisé dans V1–V5

- Launcher HOME Android 5+, huit raccourcis, thème noir et blanc, diagnostics, photo/vidéo/galerie système.
- Profils exacts, audit complet, simulation de debloat, journal persistant et export.
- Copie A↔B chiffrée sur Bluetooth classique ou Wi-Fi Direct, avec reçu SHA-256, reprise par blocs et copies privées consultables. V3 conserve l'ancien mode Wi-Fi local/hotspot.
- Tentatives périodiques configurables 10/15/20/30 minutes, réception au premier plan et Panic guidé.

## Priorité immédiate

- Exécuter tous les essais [sur deux téléphones](TEST_PLAN.md), corriger les écarts de permissions et de services selon les modèles.
- Rendre l'identité du pair Wi-Fi Direct robuste à la randomisation d'adresse radio : QR cryptographique, reconnexion et journal visible des dernières copies.
- Valider les transports Wi-Fi Direct et Bluetooth classique sur deux appareils ; évaluer BLE pour une découverte moins coûteuse, sans promesse de canal garanti.
- Permettre un export explicite des copies privées et tester la lecture des formats vidéo utilisés sur les appareils cibles.

## Extensions distinctes

- Caméra et coffre privés chiffrés, indépendants du launcher, après validation du parcours de restauration.
- Outil ADB legacy puis Shizuku optionnel pour debloat contrôlé sur profils constructeur vérifiés, avec tests de récupération.
- Dépôt APK Synology signé, contrôle de compatibilité, intégrité et certificats.
