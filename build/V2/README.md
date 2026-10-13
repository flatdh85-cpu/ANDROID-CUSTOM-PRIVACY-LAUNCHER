# Minimal OS V2 — Audit, thème nuit et réplication préparatoire

## Artefact

- Fichier : [`Minimal-OS-V2-debug.apk`](Minimal-OS-V2-debug.apk)
- Application ID : `org.minimalos.launcher`
- Version Android : `0.2.0` (`versionCode 2`)
- Compatibilité déclarée : Android 5.0/API 21 à Android 15/API 35
- SHA-256 : `091ea5904854b758ea5c5064e0a671ac241737a73002cd3526a1013beda1c010`
- Signature : clé debug Android, schémas APK v1 et v2 vérifiés

Cet APK est un artefact de démonstration. La signature debug n'est pas une clé de publication durable.

## Contenu

- thème noir et blanc à contraste élevé ; le gain énergétique dépend de l'écran, surtout OLED ;
- ouverture des applications système photo, vidéo et galerie, sans accès direct aux médias ;
- inventaire local des packages et export volontaire du journal ;
- schéma et parseur de profils stricts avec correspondance fabricant/modèle/API/version/fingerprint ;
- simulation excluant les packages critiques et toute exécution réelle ;
- journal durable avec SHA-256 et reprise après dernière écriture interrompue ;
- noyau de réplication : comparaison A↔B cumulative, aucun tombstone, reçus taille/hash/écriture durable et sélection abstraite du transport.

La permission `QUERY_ALL_PACKAGES` sert à l'audit local et peut empêcher une publication Google Play sans justification acceptée.

## Panic et transfert

Le bouton Panic est un préflight visible. Il ne supprime rien en V2. Il bloque sur la provenance inconnue des applications et l'absence de réplication média vérifiée. Le transfert radio Wi-Fi/Bluetooth, la caméra privée, le coffre et le receiver indépendant ne sont pas encore implémentés.

La conception radio pour les environnements très denses est décrite dans [`docs/TRANSFER_PROTOCOL.md`](../../docs/TRANSFER_PROTOCOL.md). Aucun canal Wi-Fi n'est présenté comme garanti dans une foule de 50 000 personnes.

## Validation cloud

Compilation propre, 21 scénarios de test distincts, Android Lint sans problème et signature APK vérifiée. Les fonctions caméra système, inventaire réel, journal exporté et comportement radio nécessitent encore des tests sur appareils physiques.
