# Minimal OS

Launcher Android natif, noir et blanc. La **V9** garde **LocalSend, BitChat et SimpleX**, les notifications et pastilles, des raccourcis photo/vidéo/galerie/réseau/Internet, le choix HOME et les diagnostics.

**[Télécharger l’APK V9](https://github.com/flatdh85-cpu/ANDROID-CUSTOM-PRIVACY-LAUNCHER/raw/refs/heads/main/build/V9/Minimal-OS-V9-debug.apk)** · [Versions précédentes](build/README.md)

Panic supprime les applications ciblées installées, la galerie publique accessible, le contenu de Téléchargements du stockage principal, puis Minimal OS et ses copies privées. **Il ne contacte aucun pair et ne fait aucune sauvegarde.** Le launcher constructeur reste disponible. Une auto-validation des dialogues connus peut être activée une fois dans les paramètres d’accessibilité Android : après cette configuration, un appui long lance Panic directement. Les dialogues système peuvent apparaître brièvement ; une fenêtre inconnue ou protégée reste à valider manuellement. [Configuration et limites](docs/PANIC.md).

**LocalSend s’utilise seul**, depuis son raccourci. Aucune association, synchronisation planifiée ou réception réseau ne tourne dans Minimal OS. L’ancien écran « Copie entre téléphones » est supprimé. LocalSend nécessite une liaison IP et ne transporte pas les fichiers par Bluetooth ; son fonctionnement sur un groupe Wi-Fi Direct dépend des téléphones. [Guide](docs/PHONE_PAIRING.md).

**Android 6** : le launcher conserve `minSdk 21` et les API récentes sont conditionnelles. LocalSend 1.17.0 déclare API 21 minimum. Les sources actuelles de **BitChat et SimpleX exigent API 26 / Android 8** : leur compatibilité Android 6 ne peut pas être garantie par le launcher et cette limite est affichée sur les raccourcis absents. [Compatibilité et sources](docs/COMPATIBILITY.md).

Au premier lancement, les accès localisation, galerie et fichiers sont demandés ; la configuration Panic reste séparée. Un [assistant](docs/INSTALLATION.md) télécharge et vérifie les APK officielles compatibles, puis lance leurs installations avec les confirmations Android. Accessible aussi par **Applications et autorisations**.

## Construire

JDK 17/21, Android SDK Platform 35 et Build Tools 35.0.0. Définir `ANDROID_HOME` ou `sdk.dir` dans un `local.properties` non suivi.

```sh
./gradlew test testDebugUnitTest lintDebug assembleDebug
```

APK produit dans `app-launcher/build/outputs/apk/debug/`. Android 5.0+ pour le launcher ; compatibilité des trois applications tierces à vérifier séparément. La signature debug V9 permet une mise à jour depuis V7.

[Architecture](docs/ARCHITECTURE.md) · [LocalSend](docs/LOCALSEND.md) · [Limites](docs/SAFETY.md) · [Tests](docs/TEST_PLAN.md) · [Récupération](docs/RECOVERY.md).
