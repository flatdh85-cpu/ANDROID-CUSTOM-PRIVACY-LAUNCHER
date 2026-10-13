# Minimal OS — Cahier des charges pour agent Codex

> **Mission** : créer un environnement Android de communication ultra-léger, avec debloat constructeur contrôlé et un bouton **Red Button** qui retire l'environnement Minimal OS et revient au launcher natif, **sans réinitialiser Android**. Le debloat constructeur peut rester appliqué.
>
> **Statut** : spécification de développement, pas une promesse de compatibilité universelle. Produire un projet compilable, des tests, une documentation et des artefacts APK lorsque l'environnement de build le permet.

## 1. Contexte et objectifs

Téléphones cibles : appareils Android anciens et récents, notamment **Wiko Lenny 3** (version Android à détecter, ne pas présumer Android 5/6), **Huawei P30 Lite MAR-LX1B** (EMUI/version à détecter), Samsung et Xiaomi. Certains appareils ont 1 Go de RAM et un SoC limité. Un **Synology DS118 sous DSM 6** peut servir de dépôt local HTTP(S) pour les APK.

**Objectifs prioritaires** :
1. Préserver appels, SMS, SIM, données mobiles/APN, Wi-Fi, Bluetooth, localisation, clavier, notifications, permissions, paramètres, écran de verrouillage, stockage, installateur APK, mises à jour critiques et sécurité.
2. Détecter précisément `Build.MANUFACTURER`, `Build.MODEL`, `Build.VERSION.SDK_INT`, version constructeur et liste des packages.
3. Proposer un debloat **fondé sur des profils vérifiés**, jamais sur une règle générique « supprimer tout ce qui n'est pas dans une whitelist ».
4. Lancer automatiquement un **launcher HOME** ultra-léger après choix explicite par l'utilisateur.
5. Exposer six applications : **Telegram, Canta, Shizuku, SimpleX Chat, Briar, LocalSend** ; n'installer que des versions compatibles avec l'API Android et l'architecture CPU.
6. Implémenter un **Red Button** : sortir de Minimal OS, restaurer/activer le launcher natif, retirer les applications installées exclusivement par Minimal OS avec confirmation utilisateur, supprimer Minimal OS en dernier. **Ne pas restaurer le bloatware constructeur**.
7. Ne jamais prétendre qu'une désinstallation supprime les sauvegardes, traces système, fichiers partagés ou données distantes. Ne pas promettre un effacement sans trace.

## 2. Contraintes incontournables

- **Aucun root requis** ; aucune modification de `/system`, `/vendor`, bootloader ou ROM ; aucun contournement FRP/activation Android.
- Un appareil encore bloqué dans l'assistant de configuration/FRP **ne peut pas utiliser ce launcher** tant que sa configuration n'est pas légitimement terminée.
- Ne jamais désactiver/supprimer les composants système essentiels, le launcher natif, les applications d'administration ou de restauration, le service de gestion des utilisateurs, la pile téléphonie/IMS, les fournisseurs de contacts/SMS, le clavier actif, le système de permissions, le framework réseau, l'installateur de packages, les services de certificats, WebView si requis, SystemUI, ni les composants de sécurité.
- Sur Android non-rooté, désactivation / `pm uninstall --user 0` **n'efface pas physiquement** les APK de la partition système. Éviter `pm uninstall --user 0` dans le profil prudent ; préférer `pm disable-user --user 0` après vérification de la disponibilité et de la réversibilité.
- **Shizuku n'est pas disponible sur toutes les versions Android**. Prévoir un moteur Legacy par ADB depuis PC (outil CLI séparé) ; ne jamais feindre que l'app peut exécuter des commandes privilégiées seule.
- Une application normale ne peut pas désinstaller silencieusement arbitrairement d'autres apps, se désinstaller sans confirmation, imposer HOME ni capter globalement toutes les touches matérielles. Prévoir UI de confirmation, sélecteur HOME, et limites explicites.
- Le Red Button n'est **pas** une clé USB Live : l'isolation est partielle ; l'environnement est installé dans Android. Pas de garantie « zéro trace », « zéro soft-brick » ou rollback atomique.
- Ne pas supprimer les données d'applications préexistantes ; n'effacer des données propres à Minimal OS qu'avec consentement. Les applications de messagerie peuvent avoir des données sensibles : avertissement et confirmation explicites.

## 3. Architecture du dépôt

Proposer un monorepo Gradle avec modules découplés :

```
minimal-os/
  README.md
  docs/ARCHITECTURE.md
  docs/COMPATIBILITY.md
  docs/SAFETY.md
  docs/RECOVERY.md
  docs/TEST_PLAN.md
  app-launcher/             # APK launcher HOME, Android 5+ si réalisable
  core-device/              # identification device, package inventory
  core-profiles/            # profils versionnés et validés
  core-journal/             # état initial + journal persistant
  core-debloat/             # moteur de planification + validation
  integration-shizuku/      # module optionnel, versions compatibles uniquement
  integration-apk-repo/     # manifeste et installateur APK
  recovery-companion/       # compagnon de restauration séparé
  tools/adb-legacy/         # script PC Linux/macOS/Windows + documentation
  profiles/                 # JSON/YAML par modèle et firmware
  server/synology-dsm6/     # exemple manifest.json et script de publication
  tests/
```

**Contrainte Legacy** : si un APK unique ne peut raisonnablement couvrir Android 5–15 avec dépendances modernes, produire deux variantes partageant le schéma de profils et le protocole de journal : `legacy` et `modern`. Prioriser une interface Android native sans WebView, animations coûteuses, tracking, services permanents ni réseau en tâche de fond. Mesurer PSS et temps de démarrage sur matériel réel ; cible indicative launcher <15 Mo PSS au repos sur appareils compatibles, **non garantie**.

## 4. Launcher

- Activité `HOME` / `DEFAULT`, sélection par dialogue système ; jamais désactiver le launcher constructeur.
- Grille fixe de six apps, horloge, batterie, état réseau, raccourcis Wi-Fi/Bluetooth/localisation/paramètres (ouvrir les panneaux ou paramètres système lorsque les API l'imposent).
- Gestion d'absence d'une app (afficher « non installée » et proposer APK compatible).
- Aucun service permanent inutile ; support écrans faibles résolutions et tactile lent.
- Accessibilité : contraste, tailles, navigation D-pad/tactile, langue française.
- Mode diagnostic accessible même si une ou plusieurs apps manquent.

## 5. Moteur de debloat

### 5.1 Profils

Chaque profil doit inclure : fabricant, modèle, version Android, build/fingerprint ou plages explicitement testées, identifiants exacts des packages, classification (`critical`, `protected`, `candidate`, `unknown`), justification, prérequis, niveau de risque, méthode supportée, stratégie de restauration, source/validation et tests effectués. **Par défaut `unknown` = ne pas toucher.**

### 5.2 Flux

1. Détecter l'appareil et inventorier packages + état enabled/installed pour utilisateur courant.
2. Appliquer uniquement un profil correspondant exactement ; sinon mode audit sans action.
3. Montrer le plan détaillé et les impacts possibles, avec cases de confirmation explicites.
4. Vérifier les invariants critiques et que le launcher natif + compagnon de récupération restent disponibles.
5. Sauvegarder l'état initial, les commandes envisagées et un script de restauration **avant** toute modification.
6. Exécuter une modification à la fois ; journaler résultat, code retour et horodatage ; arrêter si erreur.
7. Proposer tests après petits lots : appels, SMS, réseau mobile, Wi-Fi, Bluetooth, GPS, clavier, notifications, redémarrage.
8. Restaurer chaque package désactivé sur demande ; gérer états partiels et reprise après crash.

### 5.3 Moteurs d'exécution

- `AuditOnly`: aucune permission spéciale.
- `ShizukuBackend`: si API Android et service disponibles, avec vérification des permissions et compatibilités ; ne pas dépendre de Shizuku pour le lancement du launcher.
- `AdbLegacyBackend`: scripts PC, appareil autorisé via débogage USB ; commandes testées, affichage dry-run et sauvegarde du journal ; ne jamais exécuter de commandes sur un appareil inconnu sans confirmation.
- Aucune intégration non documentée aux interfaces privées de Canta. Canta peut rester une app facultative, pas une dépendance critique.

## 6. Red Button — spécification exacte

**Sémantique** : suppression de l'environnement Minimal OS uniquement ; le debloat constructeur demeure. Le Red Button n'est ni un effacement d'usine ni un effacement sécurisé des traces.

### Déclenchement

- Bouton rouge dans launcher, appui long 3 s + écran de confirmation.
- Raccourci physique configurable **uniquement si** l'OS permet légalement de capter les événements dans le contexte actif ; ne pas promettre un raccourci global avec écran verrouillé.
- Chemin de secours via compagnon séparé et script ADB sur PC.

### Étapes ordonnées

1. Charger journal et valider l'état de restauration.
2. Vérifier que le launcher natif est présent et activé ; si absent, **stopper** et proposer récupération assistée.
3. Quitter le rôle HOME via mécanisme officiel et demander à l'utilisateur de choisir le launcher constructeur ; ne pas tenter d'imposer silencieusement une préférence.
4. Lister les apps **installées par Minimal OS** ; distinguer strictement celles préexistantes. Pour Telegram, SimpleX, Briar, LocalSend, Shizuku et Canta, demander confirmation individuelle ou groupée ; signaler perte potentielle des données locales.
5. Déclencher les désinstallations via API système ou compagnon/ADB avec consentement ; suivre résultats et erreurs ; ne pas prétendre succès si confirmation en attente.
6. Nettoyer uniquement les fichiers propres à Minimal OS accessibles à l'application, puis désinstaller Minimal OS **en dernier**, avec confirmation Android.
7. Laisser le profil de debloat constructeur appliqué ; conserver une copie exportable du plan de restauration constructeur si l'utilisateur la souhaite.
8. Si Shizuku ou ADB est indisponible, afficher les étapes manuelles et préserver l'accès au launcher natif.

### Sécurité

- Jamais de suppression de la partition système, de reset, de désactivation du launcher natif ni de suppression automatique d'apps non installées par le projet.
- Conserver compagnon indépendant pour éviter qu'une app se supprime avant d'avoir terminé le retour.
- Tester interruption d'alimentation, redémarrage, absence de Shizuku, désinstallation partielle et permissions refusées.

## 7. Gestionnaire d'APK / Synology DS118 DSM 6

- Dépôt configuré par URL, par exemple `https://nas.example/apk/manifest.json` ; prise en charge LAN HTTP **uniquement sur consentement explicite** et avertissement intégrité.
- Manifeste versionné : nom, packageId, versionName, versionCode, minSdk, ABI, URL, SHA-256, empreinte de certificat de signature, source officielle, date.
- Installer seulement si compatible SDK/ABI et signature vérifiée ; ne pas confondre SHA-256 d'un manifeste non fiable avec authentification. Prévoir manifeste signé ou canal HTTPS de confiance et empreintes épinglées.
- Utiliser l'installateur Android avec confirmation ; ne jamais activer automatiquement « sources inconnues ».
- Support hors ligne pour APK déjà téléchargés, sans tracker.
- Ne pas intégrer d'APK tiers dans le dépôt source sans vérifier droits de redistribution ; proposer scripts de téléchargement depuis éditeurs officiels.

## 8. Compatibilité et matrice de test

- Android 5/6/7 : launcher legacy et ADB depuis PC ; vérifier minSdk des apps individuellement. Si Telegram/SimpleX/Briar/Shizuku/LocalSend/Canta actuels sont incompatibles, l'afficher clairement ; **ne pas inventer de compatibilité**.
- Android 8–10 : launcher et debloat ADB ; Shizuku uniquement si version prise en charge.
- Android 11+ : launcher + backend Shizuku selon disponibilité et permissions.
- Huawei EMUI, Wiko, Samsung One UI, Xiaomi MIUI/HyperOS : profils spécifiques **uniquement après validation sur appareils réels**.
- Tests fonctionnels : démarrage à froid, RAM/PSS, batterie, appel entrant/sortant, SMS, LTE/APN, Wi-Fi, Bluetooth/Briar, GPS, notifications, clavier, appareil verrouillé/déverrouillé, restauration après crash, Red Button et suppression partielle.
- Tests automatiques : parseur de profils, protection des packages critiques, planification, journal, rollback, manifeste APK, signatures, scénario `unknown device => audit only`.

## 9. Plan de livraison

**Phase 0 — Recherche technique** : confirmer contraintes API, minSdk, compatibilité des six applications et modèle de privilèges ; documenter les inconnues avant codage.

**Phase 1 — MVP launcher** : APK installable, grille de six apps, sélection HOME, diagnostics de base, tests Android ancien.

**Phase 2 — Audit & profils** : inventaire, profil exact, simulation debloat, journal et export de récupération ; aucune suppression effective.

**Phase 3 — Debloat contrôlé** : ADB Legacy et Shizuku optionnel, protections, confirmation, restauration, tests sur appareil secondaire.

**Phase 4 — Red Button** : compagnon de restauration, retour launcher natif, désinstallation guidée, tests de pannes.

**Phase 5 — Synology** : dépôt d'APK, manifeste sécurisé, compatibilité et installation guidée.

## 10. Définition de terminé (acceptance criteria)

- Un appareil compatible déjà configuré peut installer Minimal OS, le définir HOME et accéder aux six raccourcis sans perte des fonctions essentielles.
- Aucun package n'est modifié sur un appareil sans profil exact ou en l'absence de confirmation.
- Toutes les actions debloat sont journalisées, et leur restauration est documentée/testée quand l'API le permet.
- Red Button revient au launcher constructeur et propose la désinstallation de l'environnement sans réactiver le bloatware.
- Les refus de permissions et les échecs de désinstallation sont visibles, sans faux message de succès.
- Le projet compile avec des instructions reproductibles, tests exécutables et APKs de démonstration si environnement adapté.
- La documentation décrit les limites de compatibilité, les risques, le mode secours ADB et l'impossibilité de garantir « aucune trace » ou « jamais de soft-brick ».

## 11. Instructions immédiates à l'agent Codex

1. **Ne pas essayer de tout développer en une seule réponse.** Commencer par lire cette spécification, produire `docs/ARCHITECTURE.md`, `docs/COMPATIBILITY.md`, `docs/SAFETY.md` et un backlog priorisé.
2. Implémenter ensuite la **Phase 1** : vrai projet Gradle compilable, launcher minimaliste, icônes des six apps, fallback si absentes, choix HOME et diagnostics.
3. Préparer des interfaces `DeviceDetector`, `PackageInventory`, `DebloatPlanner`, `DebloatExecutor`, `RecoveryJournal`, `RedButtonCoordinator`, avec implémentations factices/audit-only pour le MVP.
4. Ajouter tests et instructions `./gradlew assembleDebug` ; rapporter les résultats réels, sans inventer de tests ou APK.
5. **Ne pas exécuter de debloat sur un téléphone réel sans demande et validation explicites.**
6. Signaler toute incompatibilité ou permission impossible, proposer un comportement dégradé sûr.

## 12. Minimal Camera / Minimal Vault / Minimal Receiver — réplication hors ligne

### 12.1 Objectif et principe non négociable

Ajouter une caméra et une galerie privées, ainsi qu'une **réplication bidirectionnelle par copie** entre téléphones de confiance, **sans Internet** quand les appareils sont à portée. Exemple : le téléphone A filme A1/A2, le téléphone B filme B1/B2. Après synchronisation, **A ET B possèdent chacun A1, A2, B1, B2**. Les originaux restent sur leur téléphone d'origine. Il ne s'agit **jamais** de déplacer les fichiers.

**Invariants obligatoires :**
- Ne **jamais** supprimer un original à la suite d'un transfert réussi, échoué ou interrompu.
- Ne **jamais** propager les suppressions : effacer A1 sur A n'efface pas la copie de A1 sur B, ni l'inverse. Ne pas implémenter de « tombstones » répliquées pour les médias.
- La synchronisation est **cumulative et incrémentale** : chaque appareil transmet uniquement les fichiers manquants, tout en conservant les siens et les copies déjà reçues.
- Une réplication n'est déclarée réussie qu'après **accusé de réception technique automatique**, vérification de la taille et de l'empreinte cryptographique du fichier ; **aucune validation manuelle** n'est demandée à chaque transfert.
- Ne jamais présenter « sauvegardé ailleurs » sans preuve de réception intégrale ; aucun transfert n'est possible sans appareil autorisé joignable ou Internet vers une destination configurée.

### 12.2 Modules proposés

- `app-camera` : interface caméra native Camera2/CameraX selon compatibilité ; photo et vidéo, enregistrement des originaux, horodatage et métadonnées utiles, sans retouche ni recompression automatique.
- `app-vault` : galerie privée, stockage **interne privé de l'application**, non publié dans MediaStore et hors dossiers publics DCIM/Pictures ; chiffrement au repos, accès authentifié, clé via Android Keystore si disponible. Éviter les copies temporaires dans le stockage partagé et contrôler les exports.
- `app-receiver` : composant indépendant du launcher qui découvre les pairs autorisés, compare les manifestes, transfère, reçoit et vérifie les fichiers ; fonctionne sur A comme sur B (pas de rôle permanent « émetteur » ou « récepteur »).
- `core-replication` : protocole versionné, index de fichiers, file d'attente persistante, état de transfert, reprise après interruption, journal d'erreurs et de copies vérifiées.

**Séparer le coffre et le service de réplication du launcher** pour que le retour à EMUI n'efface pas les médias ni n'arrête volontairement le service. Ne pas désinstaller `app-vault` ou `app-receiver` pendant une procédure Panic tant que la conservation et la reprise des fichiers ne sont pas assurées.

### 12.3 Association, découverte et transport

- Appairage initial explicite des appareils de confiance (QR code contenant une clé publique/identité cryptographique et vérification réciproque). **Pas de réception de fichiers d'inconnus**.
- Après appairage, découverte automatique à proximité via Bluetooth LE lorsque matériel, permissions et OS le permettent ; Bluetooth classique en repli si pertinent.
- Privilégier **Wi-Fi local / Wi-Fi Direct / hotspot local** pour les gros fichiers, avec négociation et autorisations Android requises ; Bluetooth pour découverte et éventuellement petits transferts.
- Transfert chiffré de bout en bout, authentification mutuelle, anti-rejeu, identifiants de fichiers stables (hash SHA-256 et identifiant unique), découpage en blocs, reprise des blocs manquants et limitation des doublons.
- Comparer les manifestes de A et B et transférer **dans les deux sens** les fichiers absents. En cas de stockage insuffisant, signaler l'échec, ne supprimer aucun original et ne jamais évincer silencieusement les anciennes copies.
- Prévoir un plafond de stockage configurable et une politique de conservation **locale uniquement, jamais propagée**, avec avertissement avant tout effacement manuel.

### 12.4 Déclencheurs de synchronisation

1. **Périodique** : tenter la découverte et la synchronisation environ toutes les **30 minutes**. Sur Android/EMUI, `WorkManager` et les restrictions d'arrière-plan ne garantissent pas une cadence exacte : documenter les retards et tester sur Huawei P30 Lite EMUI 12 / Android 10.
2. **À la reconnexion** : si un pair autorisé redevient joignable, reprendre les transferts manquants dès que possible.
3. **Panic Button** : déclencher immédiatement une synchronisation prioritaire, en commençant par les nouveaux médias non répliqués ; tenter plusieurs pairs autorisés si disponibles. **Ne pas attendre la fin pour pouvoir revenir à EMUI**, et ne pas promettre qu'un transfert survivra à l'extinction, à la perte du signal ou à la fermeture forcée du processus.
4. **Optionnel** : une sauvegarde distante chiffrée lorsque Internet revient, indépendante de la réplication locale et désactivable.

Aucune confirmation manuelle par transfert après appairage ; les demandes initiales d'autorisation Bluetooth, Wi-Fi Direct, notifications ou premier plan restent soumises à Android. Ne pas bloquer physiquement l'extinction du téléphone, ni empêcher l'accès aux appels d'urgence ; un `WakeLock` temporaire peut être utilisé pendant une opération active, sans garantie contre l'arrêt du système.

### 12.5 Briar : intégration facultative, sans hypothèse irréaliste

Briar peut rester disponible comme messagerie indépendante, mais **ne pas supposer une API publique permettant d'injecter automatiquement des vidéos et pièces jointes vers ses contacts**. Le transfert automatique et bidirectionnel des originaux doit être implémenté dans `app-receiver`. Un partage explicite vers Briar peut être proposé uniquement si les capacités réelles de la version installée le permettent. Ne pas dépendre de Telegram, d'un serveur cloud ou d'Internet pour le fonctionnement local de base.

### 12.6 Interaction avec le Red Button et protection des médias

- Le Panic Button doit demander une synchronisation prioritaire et proposer le retour à l'interface EMUI allégée.
- Les fichiers du coffre **ne doivent pas apparaître dans la galerie Huawei classique** ni dans les dossiers publics. Le stockage privé/chiffré n'est toutefois pas une garantie de disparition totale des traces, ni de résistance à une réinitialisation du téléphone.
- **Ne jamais supprimer le coffre, les originaux ou les copies locales automatiquement lors du Panic Button** ; distinguer clairement « quitter le launcher » de « effacer des données ».
- Le launcher ne doit pas se désinstaller avant d'avoir remis le contrôle au système et d'avoir remis les tâches de transfert à un composant indépendant. Les restrictions de fond d'EMUI doivent être affichées comme limites réelles.
- Aucun « mode blocage absolu » du téléphone : afficher un état de progression si l'utilisateur le souhaite, mais conserver un moyen de quitter l'écran et d'accéder aux fonctions d'urgence.

### 12.7 Tests d'acceptation obligatoires

- A crée A1/A2, B crée B1/B2 ; après échange, les **quatre fichiers** existent et sont lisibles **sur les deux appareils** ; originaux intacts et SHA-256 identiques.
- A supprime manuellement une copie locale : B conserve son exemplaire. B supprime une vidéo : A conserve la sienne. Aucune suppression ne se propage.
- Déconnexion en milieu de vidéo, coupure Wi-Fi, perte Bluetooth, reboot, absence de pair, stockage saturé, permissions refusées : reprise correcte, aucun faux succès, aucune perte d'original.
- Synchronisation incrémentale : pas de réémission intégrale des fichiers déjà vérifiés ; nouvelles vidéos uniquement, et rattrapage après plusieurs périodes manquées.
- Panic Button pendant un transfert : retour à EMUI possible, coffre préservé, état de reprise persistant ; comportement réel documenté sur EMUI 12.
- Photos/vidéos absentes de la galerie Huawei et de MediaStore ; ouverture possible uniquement depuis le coffre ou un export explicitement autorisé.
- Vérifier consommation batterie/RAM, taille de fichiers vidéo réalistes, autorisations et compatibilité Android anciennes ; ne pas annoncer une cadence de 30 minutes garantie.

### 12.8 Ordre de développement

Ajouter au backlog **après le MVP launcher et la restauration fiable** : (1) caméra + coffre privé, (2) transfert chiffré A→B, (3) réplication A↔B incrémentale, (4) déclencheur périodique ~30 min, (5) déclencheur Panic, (6) tests sur deux appareils physiques. Fournir protocole, schémas d'état, tests unitaires et intégration, et documentation des permissions avant toute promesse de fonctionnement autonome.


## 13. Directive utilisateur V8 — périmètre courant

Les dernières demandes explicites remplacent les points incompatibles précédents : accueil LocalSend/BitChat/SimpleX uniquement ; retrait des transferts maison et de la planification ; LocalSend autonome pour simplifier au maximum. Le launcher doit rester utilisable sous Android 6. Les sources actuelles de BitChat et SimpleX exigent Android 8 : afficher la limite, ne pas inventer leur compatibilité. LocalSend 1.17.0 déclare API 21 minimum.

Panic supprime les trois applications ciblées présentes, même préexistantes, la galerie accessible et Minimal OS en dernier, sans copie ni sonde. L’utilisateur demande l’auto-acceptation : service d’accessibilité facultatif après consentement et activation Android, limité aux dialogues attendus pendant Panic. Aucun clic générique ni contournement de protection système. Conserver les autres applications et le HOME constructeur ; fenêtres inconnues manuelles, aucune promesse de suppression silencieuse garantie.

Références : `docs/ARCHITECTURE.md`, `docs/PANIC.md`, `docs/COMPATIBILITY.md`, `docs/TEST_PLAN.md`. Distinguer tests cloud et essais Android 6/S22/Huawei.

## 14. Demandes utilisateur V9

Étendre Panic au contenu du dossier public Téléchargements principal, APK/documents/sous-dossiers compris, sans élargir arbitrairement à tous les dossiers du stockage. Respecter les permissions Android et arrêter avec un état explicite si l’effacement échoue. L’historique privé du gestionnaire de téléchargement n’est pas un fichier accessible.

Proposer au premier lancement un assistant d’installation de LocalSend, BitChat et SimpleX, avec APK officielles épinglées et vérifiées (SDK/ABI/hash/certificat), installateur Android et confirmations utilisateur. Applications déjà présentes ignorées. Android 6 reste pris en charge par le launcher ; seules les versions tierces compatibles sont proposées.

Au premier lancement, proposer les permissions de localisation, galerie et fichiers de Minimal OS ; afficher les refus et permettre une reprise. La position n’est pas lue/transmise. L’utilisateur souhaite conserver la configuration Panic séparée et inchangée. Ces autorisations ne valent pas pour les applications tierces.

L’accueil doit rester fixe sans défilement : grille mesurée adaptative portrait/paysage, aperçu des notifications ouvrant leur détail. Proposer le réglage sombre global quand Minimal OS devient HOME par défaut et garder un raccourci ; ne pas prétendre pouvoir imposer le thème à Android ou aux applications tierces. Android 6 peut ne pas proposer de thème système général.
