# Minimal OS V9 — configuration et téléchargements

[**Télécharger l’APK V9**](https://github.com/flatdh85-cpu/ANDROID-CUSTOM-PRIVACY-LAUNCHER/raw/refs/heads/main/build/V9/Minimal-OS-V9-debug.apk)

Version 0.9.0 / code 9, minSdk 21, cible API 35. Même signature debug que V8, mise à jour possible sans désinstallation préalable.

- Premier lancement : demandes localisation et galerie/stockage ; accès spécial aux fichiers Android 11+ sur un écran système séparé. Refus affichés, reprise par **Applications et autorisations**. Configuration Panic séparée et conservée.
- Assistant d’installation de LocalSend 1.17.0, BitChat 2.0.1 et SimpleX 7.0.3 : téléchargement des APK officielles, filtrage SDK/ABI, vérification SHA-256/package/certificat, confirmation dans l’installateur Android, applications déjà présentes ignorées. Sur Android 6, seul LocalSend parmi ces versions est proposé.
- Panic efface aussi les APK, documents et sous-dossiers du dossier public Téléchargements **du stockage principal**, après la galerie et avant Minimal OS. Accès contrôlé avant les désinstallations, refus/échecs visibles, arrêt avant auto-désinstallation si des fichiers restent. Les liens ne sont pas suivis. Les dossiers extérieurs et l’historique privé des autres gestionnaires ne sont pas effacés.

Validation cloud : **56 exécutions de tests, zéro échec/erreur/ignoré**, lint sans problème, assemblage debug. Les six APK officielles référencées ont été téléchargées et leurs signatures, SDK/ABI et hashes vérifiés ; elles ne sont pas redistribuées dans ce dépôt. Le parcours d’autorisations/installations/suppressions doit encore être essayé physiquement sur Android 6/S22/Huawei.

SHA-256 APK : `c331a6e156d11ecf6ef0e40d613a181000613045b46a73faeb1d1818edca1ee1`.

Certificat SHA-256 : `22880be22a7f06d7a9c7e0c0c2984ae9a04d7702eb241aa4843beddd334306f9`.

[Assistant et permissions](../../docs/INSTALLATION.md) · [Panic](../../docs/PANIC.md) · [Tests](../../docs/TEST_PLAN.md).
