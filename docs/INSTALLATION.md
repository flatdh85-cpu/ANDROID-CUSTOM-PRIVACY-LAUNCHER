# Installation des applications — V9

Au premier lancement de V9, l’assistant propose **Installer / reprendre**. Le même écran reste accessible sur l’accueil par **Applications**.

Le parcours télécharge successivement les APK officielles compatibles, vérifie leur SHA-256, le package et l’empreinte du certificat, puis ouvre l’installateur Android. Chaque installation doit être confirmée dans Android ; l’auto-validation Panic ne s’applique jamais à l’installation ni à l’octroi de permissions. Android 8+ exige une autorisation de source pour Minimal OS ; Android 6 utilise son réglage global de sources inconnues. Minimal OS ne peut pas déclencher ces installations au seul moment où sa propre APK est installée : il faut ouvrir l’application.

Les applications présentes sont ignorées, sans mise à jour forcée. Un refus/échec suspend la séquence ; « Installer / reprendre » permet de réessayer. DownloadManager conserve un téléchargement quand l’assistant passe en arrière-plan ; la vérification et l’installation reprennent lorsqu’il est visible. Les fichiers temporaires sont dans le dossier externe privé de Minimal OS, supprimés après installation ou annulation. Panic annule son téléchargement connu et supprime ce stockage privé lors de la désinstallation finale.

Versions explicitement épinglées (pas de recherche aveugle de la dernière APK) :

| Application | Version | Android minimum | Processeurs proposés |
| --- | --- | --- | --- |
| LocalSend | 1.17.0 | 5 / API 21 | ARM64, ARMv7, x86_64 |
| BitChat | 2.0.1 | 8 / API 26 | APK universelle ARM64, ARMv7, x86_64, x86 |
| SimpleX | 7.0.3 | 8 / API 26 | ARM64, ARMv7 |

Les six APK référencées ont été téléchargées depuis les releases officielles et contrôlées avec `aapt dump badging` et `apksigner verify --print-certs` pour cette livraison. Les URLs, hashes et certificats sont embarqués dans `AppInstallCatalog.java`. Les APK tierces ne sont pas redistribuées dans le dépôt. Une modification des fichiers hébergés provoquera un rejet du hash ; l’actualisation des versions impose de vérifier à nouveau les artefacts et de mettre à jour le catalogue.

Internet est requis. Environ 160 Mo sont téléchargés pour les trois applications sur ARM64. Les contraintes constructeur, l’espace libre et les refus Android peuvent empêcher une installation. Le minimum SDK/ABI est vérifié avant téléchargement ; cela ne remplace pas un essai physique, notamment sous Android 6.

## Autorisations au premier lancement

L’assistant demande dès son premier affichage les permissions Android de localisation et de galerie/stockage manquantes. Sur Android 11+, il ouvre ensuite les réglages d’accès à tous les fichiers pour permettre le nettoyage de Téléchargements. Les refus ne bloquent pas l’accueil ni les raccourcis ; l’état accordé/non accordé est affiché et le bouton **Accorder / vérifier les autorisations** permet de réessayer. Un refus permanent peut nécessiter les informations de l’application dans Android. Les demandes ne sont pas répétées automatiquement à chaque lancement.

La localisation est demandée conformément au choix utilisateur, mais le launcher ne lit ni ne transmet de position. Ces permissions ne sont pas accordées aux applications tierces, qui ont chacune leur propre parcours Android. L’accessibilité Panic et les confirmations de sa configuration restent inchangées et séparées. L’accès aux notifications se configure toujours depuis l’accueil. L’autorisation d’installer les APK n’est demandée qu’au lancement de l’installation.

L’écran est accessible à nouveau par **Applications** sur l’accueil.
