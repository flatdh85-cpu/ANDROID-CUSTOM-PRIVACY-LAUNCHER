# Minimal OS V4 — radios directes et bouton Internet

APK debug : [`Minimal-OS-V4-debug.apk`](Minimal-OS-V4-debug.apk). Version 0.4.0, Android 5.0+ (API 21), cible API 35. La V4 remplace l'adresse IP et le Wi-Fi/hotspot communs de V3 par Bluetooth classique RFCOMM et Wi-Fi Direct. Elle ajoute **Internet** sur l'accueil pour ouvrir le navigateur installé. Le protocole de copie chiffrée, la réception, l'intervalle 10/15/20/30 minutes et Panic restent disponibles sur le transport sélectionné.

SHA-256 de l'APK : `8d2f3acffd3b7814ab79c89ebdad78fef304f273827dd3996e1424c1de032f3d`. Il porte le même certificat debug que V3, ce qui permet une mise à jour Android si V3 a été installée depuis ce dépôt.

Compilation, tests unitaires et lint ont été exécutés dans l'environnement cloud. **Aucun test sur deux téléphones physiques n'a été réalisé ici.** Les permissions, la négociation P2P, les restrictions d'arrière-plan et le débit doivent être vérifiés sur les appareils cibles avant d'utiliser Panic. Voir [l'association A↔B](../../docs/PHONE_PAIRING.md), [le protocole](../../docs/TRANSFER_PROTOCOL.md) et [le plan de test](../../docs/TEST_PLAN.md).
