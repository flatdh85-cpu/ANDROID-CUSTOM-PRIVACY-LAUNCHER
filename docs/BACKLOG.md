# Backlog priorisé

## P0 — Phase 1

- Compiler l'APK HOME Android 5+ et ses tests.
- Afficher six tuiles avec état présent/absent et fallback sûr.
- Afficher diagnostics locaux et raccourcis vers réglages officiels.
- Valider sélection HOME, accessibilité, petite résolution et PSS sur appareils physiques.

## P1 — Audit et restauration fiable (Phase 2 en cours)

- Schéma versionné de profils avec correspondance exacte et `unknown = no-op` — implémenté.
- Inventaire complet, plan audit-only visible, journal persistant et export de récupération — implémenté, tests appareil requis.
- Protections explicites des packages critiques et tests de dernière écriture interrompue — implémentés.
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
- Noyau de comparaison cumulative A↔B, reçus et choix abstrait du transport — commencé en Phase 2, sans accès radio.
- Transfert chiffré A→B, puis connexion du noyau de réplication sans suppression propagée.
- Reprise, déclencheur périodique approximatif, Panic prioritaire et tests sur deux appareils.
