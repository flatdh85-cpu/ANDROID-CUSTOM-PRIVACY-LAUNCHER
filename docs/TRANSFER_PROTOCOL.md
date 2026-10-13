# Protocole de copie locale V3

V3 utilise un réseau Wi-Fi local ou un hotspot Android existant. Un appareil écoute sur TCP 38742 et l'autre lance une session vers son adresse IPv4 privée. Une session fonctionne dans les deux sens : elle compare les manifestes, envoie les médias absents de B vers A et ceux absents de A vers B. Aucune suppression n'est propagée. Les copies reçues sont stockées dans l'espace privé de l'APK et consultables depuis **Voir les copies reçues**.

Le code partagé aléatoire contient 128 bits. Chaque côté prouve sa possession avec HMAC-SHA-256 et des défis aléatoires distincts. Une clé de session est dérivée de ces défis ; les manifestes et blocs de 64 Kio sont chiffrés et authentifiés par AES-GCM. Une interruption conserve les blocs complets et la session suivante reprend au dernier bloc de 64 Kio. Le récepteur n'envoie un reçu authentifié qu'après vérification de la taille, du SHA-256 du fichier entier, synchronisation du fichier et publication de la copie. Le code donne accès à la galerie du pair : ne le partagez qu'entre vos propres appareils et changez-le si un tiers l'a vu.

Une réception ne prouve pas une disponibilité permanente : stockage plein, batterie, arrêt forcé de l'app, changement d'adresse IP, hotspot indisponible et restrictions du fabricant peuvent interrompre les sessions. Le job de 10/15/20/30 minutes est une **tentative différée**, sans cadence exacte. Le récepteur doit être démarré et sa notification doit rester active.

## Radio dans une foule dense

Aucun canal Wi-Fi ne peut être garanti libre au milieu de 50 000 personnes. Une application Android ordinaire ne peut généralement pas imposer le canal du Wi-Fi Direct ou d'un hotspot. Quand le matériel le permet, le 5 GHz non-DFS peut réduire les interférences par rapport au 2,4 GHz, mais Android et le firmware choisissent souvent la bande et le canal. V3 ne sélectionne aucun canal, ne découvre pas les pairs par Bluetooth et ne crée pas de groupe Wi-Fi Direct : il utilise l'adresse locale du Wi-Fi ou du hotspot déjà établi. Il faut mesurer le débit et les coupures sur les deux téléphones cibles avant un usage sur le terrain.

Le transport actuel n'implémente pas l'appairage QR, la découverte BLE ni le Wi-Fi Direct automatique. L'association repose sur la saisie manuelle du code et de l'adresse. Ces fonctions restent des améliorations possibles après les essais physiques. Les tests automatiques vérifient la copie A↔B, le rejet d'un mauvais code, la reprise et l'absence de propagation des suppressions.
