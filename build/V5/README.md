# Minimal OS V5 — correction Bluetooth

APK debug : [`Minimal-OS-V5-debug.apk`](Minimal-OS-V5-debug.apk). Version 0.5.0, Android 5.0+ (API 21), cible API 35. Installer **V5 sur les deux téléphones** : le protocole n'est pas compatible avec V4.

SHA-256 : `efd4b3550459798a72b393fc9dbfebbd6f328b228430995b17d68437ef298eee`. Le certificat debug est identique à celui de V4 pour une mise à jour Android depuis l'APK de ce dépôt.

La V5 supprime la demande de notification Android, utilise une liaison Bluetooth RFCOMM sans nouveau dialogue d'appairage après l'association initiale, et conserve l'authentification mutuelle et le chiffrement AES-GCM de l'application. Elle affiche une erreur explicite si les codes diffèrent ou si le récepteur ne peut pas lire sa galerie. Ouvrez **Copie entre téléphones** sur le récepteur pour voir son état et l'adresse réellement connectée. Wi-Fi Direct reste disponible, avec les éventuelles confirmations de groupe imposées par Android.

Compilation, tests unitaires et lint exécutés dans le cloud. **Aucun essai radio sur deux téléphones physiques n'a été réalisé ici.** Suivez [le guide A↔B](../../docs/PHONE_PAIRING.md) et [le plan de test](../../docs/TEST_PLAN.md) avant Panic.
