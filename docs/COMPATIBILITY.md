# Compatibilité

## Launcher

| Plateforme | État Phase 1 | Limites à vérifier sur appareil |
| --- | --- | --- |
| Android 5–7 | Compilable, API minimale 21 | Sélecteur HOME constructeur, taille écran, mémoire, disponibilité réelle des six apps |
| Android 8–10 | Compilable | Restrictions EMUI/MIUI, intents de réglages et comportement après redémarrage |
| Android 11–15 | Compilable avec visibilité explicite des six packages | Sélecteur HOME et politiques constructeur |

La compilation prouve la compatibilité des API, pas le fonctionnement sur chaque firmware. Wiko Lenny 3, Huawei P30 Lite MAR-LX1B, Samsung One UI et Xiaomi MIUI/HyperOS exigent des tests physiques. La cible de moins de 15 Mo PSS au repos n'est pas encore mesurée.

## Applications affichées

Le launcher reconnaît les identifiants configurés pour Telegram, Canta, Shizuku, SimpleX Chat, Briar et LocalSend. Il ne garantit ni leur disponibilité, ni la compatibilité de leur version courante avec Android 5, ni leur provenance. Une absence est signalée sans proposer de binaire arbitraire.

Shizuku et Canta restent facultatifs. Shizuku n'accorde aucun privilège au launcher Phase 1. Briar est lancé comme application indépendante ; aucune API privée ou injection de contenu n'est supposée.

## Variantes futures

Si les dépendances des phases ultérieures ne peuvent plus maintenir API 21, une variante `legacy` conservera le protocole de profils et de journal partagé avec une variante `modern`. Cette séparation n'est pas utile tant que le MVP reste basé sur le framework Android.
