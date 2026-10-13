# Compatibilité

## Launcher

| Plateforme | État V3 | Limites à vérifier sur appareil |
| --- | --- | --- |
| Android 5–7 | Compilable, API minimale 21 | Sélecteur HOME, stockage partagé, permissions et réception au premier plan |
| Android 8–10 | Compilable | Restrictions EMUI/MIUI, suppression MediaStore Android 10, service après redémarrage |
| Android 11–15 | Compilable | Autorisation complète des médias, confirmations de suppression et politiques de batterie |

La compilation prouve la compatibilité des API, pas le fonctionnement sur chaque firmware. Wiko Lenny 3, Huawei P30 Lite MAR-LX1B, Samsung One UI et Xiaomi MIUI/HyperOS exigent des tests physiques. La cible de moins de 15 Mo PSS au repos n'est pas encore mesurée. Les intents caméra/vidéo/galerie dépendent d'une application système compatible.

L'inventaire Phase 2 déclare `QUERY_ALL_PACKAGES`. Il convient à une distribution directe de cet outil d'audit, mais Google Play restreint cette permission. Une publication Play devra confirmer l'éligibilité ou utiliser un inventaire fourni par ADB/Shizuku dans une phase ultérieure.

## Applications affichées

Le launcher affiche Telegram, Canta, Shizuku, SimpleX Chat, Briar, LocalSend, Bridgefy et BitChat. Pour Bridgefy et BitChat, il cherche aussi l'application installée par son nom affiché lorsque l'identifiant du package diffère. Il ne garantit ni leur disponibilité, ni la compatibilité de leur version courante avec Android 5, ni leur provenance. Une absence est signalée sans proposer de binaire arbitraire.

Shizuku et Canta restent facultatifs. Shizuku n'accorde aucun privilège au launcher. Briar est lancé comme application indépendante ; aucune API privée ou injection de contenu n'est supposée. Les photos/vidéos peuvent être copiées sur Wi-Fi local sans ces applications.

## Variantes futures

Si les dépendances des phases ultérieures ne peuvent plus maintenir API 21, une variante `legacy` conservera le protocole de profils et de journal partagé avec une variante `modern`. Cette séparation n'est pas utile tant que le MVP reste basé sur le framework Android.
