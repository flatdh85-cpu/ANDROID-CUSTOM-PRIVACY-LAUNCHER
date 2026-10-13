# Minimal OS V6 — Panic, Wi-Fi Direct et notifications

APK debug : [`Minimal-OS-V6-debug.apk`](Minimal-OS-V6-debug.apk). Version 0.6.0, Android 5.0+ (API 21), cible API 35. **Installer V6 sur les deux téléphones** : son protocole radio ne fonctionne pas avec V5.

SHA-256 : `22735d7b9169c031fa53473440e4c9d074c923b8af82085dcf19ccbe690594b7`. Le certificat debug est identique à celui de V5 pour une mise à jour Android depuis ce dépôt.

Panic effectue une sonde Bluetooth authentifiée avec une réponse explicite du récepteur et plusieurs tentatives. Elle ne lit pas la galerie et ne déclenche aucune désinstallation si le pair n'est pas authentifié. La découverte Wi-Fi Direct relit les appareils déjà connus, réessaie et détecte un groupe déjà actif lorsque Android renvoie l'erreur générique `0`.

Le launcher affiche les notifications des huit applications de la grille en liste et en nombre sur leurs raccourcis. Il faut accorder **une fois** à Minimal OS l'accès spécial Android aux notifications ; aucun consentement par message n'est demandé. Les résumés restent en mémoire, sans journal de notifications sur disque.

Compilation, tests unitaires et lint exécutés dans le cloud. Les connexions Bluetooth/Wi-Fi Direct et les notifications exigent encore une validation sur les deux téléphones cibles. Voir [le guide d'association](../../docs/PHONE_PAIRING.md) et [le plan de test](../../docs/TEST_PLAN.md).
