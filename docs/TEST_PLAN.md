# Plan de test

## Automatisé en Phase 2

- catalogue : six entrées, identifiants de package uniques et non vides ;
- inventaire : état installé/activé représenté sans action système ;
- profil absent : correspondance non exacte ;
- planificateur audit-only : aucune modification proposée ;
- exécuteur audit-only : aucune commande exécutée et refus d'un plan contenant des actions ;
- journal mémoire : ordre et copie défensive des événements ;
- coordinateur Red Button MVP : indisponible sans effet de bord ;
- parseur de profil complet, rejet des schémas inconnus et correspondance exacte du fingerprint ;
- simulation limitée aux candidats présents et blocage des packages critiques/launchers ;
- journal disque relisible, exportable et tolérant une dernière trame interrompue ;
- préflight Panic sans suppression, même lorsque ses prérequis déclaratifs sont satisfaits ;
- plan de réplication A↔B, retransmission d'une copie supprimée localement, reçu durable et refus de Bluetooth seul pour une vidéo ;
- compilation, ressources Android et lint du launcher.

## À exécuter sur émulateur ou appareil

Installer l'APK, choisir HOME via Android, redémarrer et vérifier navigation tactile/D-pad, contraste noir et blanc, petites résolutions, horloge, batterie, réseau et ouverture des réglages. Tester chaque tuile avec application présente puis absente. Vérifier les boutons photo, vidéo et galerie, l'inventaire, l'export du journal et le refus du préflight Panic. Vérifier appel/SMS/LTE/APN/Wi-Fi/Bluetooth/GPS/clavier/notifications avant et après sélection HOME.

Mesurer démarrage à froid, PSS au repos et batterie sur Wiko Lenny 3 et Huawei P30 Lite. Tester les firmwares Samsung et Xiaomi séparément. Aucun de ces tests physiques n'est considéré comme exécuté par la compilation cloud.

## Phases ultérieures

Ajouter tests de profils exacts, packages critiques, journal durable, reprise après crash, rollback, manifeste/signatures APK, pannes Shizuku/ADB et Red Button interrompu. La caméra/réplication exigera deux appareils et vérifiera les SHA-256, la reprise, le stockage saturé et l'absence de propagation des suppressions.
