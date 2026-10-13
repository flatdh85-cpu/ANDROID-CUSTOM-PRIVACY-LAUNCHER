# Minimal OS V9 — configuration et téléchargements

[**Télécharger l’APK V9**](https://github.com/flatdh85-cpu/ANDROID-CUSTOM-PRIVACY-LAUNCHER/raw/refs/heads/main/build/V9/Minimal-OS-V9-debug.apk)

Version 0.9.0 / code 9, minSdk 21, cible API 35. Même signature debug que V8, mise à jour possible sans désinstallation préalable.

- Premier lancement : demandes localisation et galerie/stockage ; accès spécial aux fichiers Android 11+ sur un écran système séparé. Refus affichés, reprise par **Applications**. Configuration Panic séparée et conservée.
- Assistant d’installation de LocalSend 1.17.0, BitChat 2.0.1 et SimpleX 7.0.3 : téléchargement des APK officielles, filtrage SDK/ABI, vérification SHA-256/package/certificat, confirmation dans l’installateur Android, applications déjà présentes ignorées. Sur Android 6, seul LocalSend parmi ces versions est proposé.
- Panic efface aussi les APK, documents et sous-dossiers du dossier public Téléchargements **du stockage principal**, après la galerie et avant Minimal OS. Accès contrôlé avant les désinstallations, refus/échecs visibles, arrêt avant auto-désinstallation si des fichiers restent. Les liens ne sont pas suivis. Les dossiers extérieurs et l’historique privé des autres gestionnaires ne sont pas effacés.

- Accueil fixe sans défilement : grille de 14 raccourcis adaptée aux dimensions réellement disponibles, portrait/paysage ; aperçu compact des notifications ouvrant leur liste. Icônes et textes ajustés selon les cellules.
- Raccourci **Thème sombre** et proposition unique lorsque Minimal OS est HOME par défaut et que le système est clair. Le réglage global doit être activé par l’utilisateur et les applications doivent le prendre en charge ; pas de thème imposé aux autres applications ni de garantie de thème système Android 6.

Validation cloud : **62 exécutions de tests, zéro échec/erreur/ignoré**, lint sans problème, assemblage debug. Les six APK officielles référencées ont été téléchargées et leurs signatures, SDK/ABI et hashes vérifiés ; elles ne sont pas redistribuées dans ce dépôt. Le parcours d’autorisations/installations/suppressions doit encore être essayé physiquement sur Android 6/S22/Huawei.

SHA-256 APK : `bd7cb08e43b991fd30579b49da5590cf3eb9c0ea261c180d5f6084765b962fad`.

Certificat SHA-256 : `22880be22a7f06d7a9c7e0c0c2984ae9a04d7702eb241aa4843beddd334306f9`.

[Assistant et permissions](../../docs/INSTALLATION.md) · [Panic](../../docs/PANIC.md) · [Tests](../../docs/TEST_PLAN.md).
