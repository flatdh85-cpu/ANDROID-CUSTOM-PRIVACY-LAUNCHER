# Plan de test

## Automatisé

Les modules Java vérifient l'inventaire, les profils exacts, la protection des packages critiques, le journal persistant, la simulation de debloat et les décisions de réplication. Les tests de socket locaux couvrent la copie A↔B de quatre contenus, la récupération d'une copie supprimée localement sans propagation, le refus d'un mauvais code et la reprise d'une vidéo à une frontière de bloc. La compilation Android et lint vérifient les ressources, le manifeste et les appels d'API. Ces tests ne représentent pas des téléphones réels.

## Obligatoire sur deux téléphones avant usage réel

1. Installer le même APK V3 sur A et B ; vérifier les huit raccourcis, photo, vidéo, galerie, les permissions média complètes, le thème, HOME et le launcher constructeur.
2. Faire l'essai A1/A2 sur A et B1/B2 sur B décrit dans [PHONE_PAIRING.md](PHONE_PAIRING.md). Vérifier lecture et SHA-256 identiques sur les deux téléphones, originaux toujours présents.
3. Tester Wi-Fi partagé puis hotspot, changement d'adresse IP, perte du réseau au milieu d'une vidéo, reprise, stockage plein, mauvais code et permissions refusées. Aucun échec ne doit afficher « copie vérifiée ».
4. Tester les quatre intervalles 10/15/20/30 minutes, téléphone verrouillé, redémarrage, restrictions batterie et fermeture forcée. Mesurer les retards réels ; la cadence Android n'est pas garantie.
5. Sur un téléphone secondaire rempli de médias de test uniquement, lancer Panic : confirmer chaque désinstallation, la copie, la suppression de galerie et le retour au launcher natif. Tester l'annulation de chaque étape, le pair absent, une galerie modifiée pendant la copie et la reprise après interruption. Vérifier que l'autre téléphone conserve les copies.
6. Répéter selon les versions disponibles : Android 5/6/7, Android 10 EMUI, Android 11+, Samsung et Xiaomi. Mesurer PSS, batterie et durée des vidéos volumineuses. Vérifier appels, SMS, réseau mobile, Wi-Fi, Bluetooth, GPS, clavier et notifications avant/après.

Aucun essai physique n'a été exécuté dans l'environnement cloud de cette livraison.
