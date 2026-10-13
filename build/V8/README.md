# Minimal OS V8 — simplifiée

[**Télécharger l’APK**](https://github.com/flatdh85-cpu/ANDROID-CUSTOM-PRIVACY-LAUNCHER/raw/refs/heads/main/build/V8/Minimal-OS-V8-debug.apk)

Version 0.8.0 / code 8. APK debug, minSdk 21 (Android 5+), cible 35 ; Android 6 est couvert par le code du launcher. Signature conservée pour mise à jour depuis V7.

- Trois applications : LocalSend, BitChat et SimpleX. Raccourcis photo/vidéo/galerie/réseau/Internet redessinés en noir et blanc, notifications/pastilles, choix HOME et diagnostics conservés.
- LocalSend autonome : aucun code, écran de copie, service radio ni tâche de transfert dans Minimal OS. La synchronisation périodique n’est pas fournie.
- Panic indépendant du réseau : applications ciblées présentes, galerie accessible puis Minimal OS, retour HOME. Maintien de trois secondes. Auto-validation facultative via accessibilité, à activer une fois dans Android ; fenêtres inconnues/protégées manuelles, compatibilité constructeur à tester.

**Android 6 :** LocalSend 1.17.0 déclare API 21 minimum. Les versions actuelles de BitChat et SimpleX exigent Android 8/API 26 ; le launcher affiche cette incompatibilité si elles sont absentes. Il ne peut pas rétroporter ces applications.

Validation cloud : **42 exécutions de tests**, zéro échec/erreur/ignoré ; **lint sans problème**, assemblage debug et signature vérifiés. Les variantes debug/release exécutent chacune les tests Android JVM. Aucun test physique d’effacement, d’accessibilité ou de transfert LocalSend réalisé sur Android 6/S22/Huawei.

SHA-256 APK : `cb21b68bc1912ca6fd07227975b506546790fcea38c7b59519183f76533898ef`.

Empreinte SHA-256 du certificat : `22880be22a7f06d7a9c7e0c0c2984ae9a04d7702eb241aa4843beddd334306f9`.

[Architecture](../../docs/ARCHITECTURE.md) · [Panic et activation](../../docs/PANIC.md) · [Compatibilité](../../docs/COMPATIBILITY.md) · [LocalSend](../../docs/PHONE_PAIRING.md) · [Essais physiques requis](../../docs/TEST_PLAN.md).
