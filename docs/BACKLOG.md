# Backlog priorisé

## P0 — Phase 1

- Compiler l'APK HOME Android 5+ et ses tests.
- Afficher six tuiles avec état présent/absent et fallback sûr.
- Afficher diagnostics locaux et raccourcis vers réglages officiels.
- Valider sélection HOME, accessibilité, petite résolution et PSS sur appareils physiques.

## P1 — Audit et restauration fiable

- Schéma versionné de profils avec correspondance exacte et `unknown = no-op`.
- Inventaire complet, plan audit-only visible, journal persistant et export de récupération.
- Protections explicites des packages critiques et tests de crash/reprise.
- Compagnon de récupération indépendant avant toute capacité de modification.

## P2 — Debloat contrôlé et Red Button

- Outil ADB legacy dry-run, puis backend Shizuku optionnel selon compatibilité.
- Modifications une par une, confirmations, tests par lots et rollback vérifié.
- Retour au HOME natif, désinstallations guidées et Minimal OS en dernier.

## P3 — Dépôt APK

- Manifeste signé, HTTPS, contrôle SDK/ABI/SHA-256/certificat et installation système confirmée.
- Publication Synology DSM 6 et mode LAN HTTP uniquement sur consentement explicite.

## P4 — Caméra, coffre et réplication

- Caméra et coffre privé chiffré après restauration fiable.
- Transfert chiffré A→B, puis réplication cumulative A↔B sans suppression propagée.
- Reprise, déclencheur périodique approximatif, Panic prioritaire et tests sur deux appareils.
