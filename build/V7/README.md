# Minimal OS V7 — connexions et diagnostic

APK debug : [`Minimal-OS-V7-debug.apk`](Minimal-OS-V7-debug.apk). Version 0.7.0, Android 5.0+ (API 21), cible API 35. **Installer V7 sur les deux téléphones** : le protocole MOS8 n’est pas compatible avec V6.

- Le récepteur applique le transport sélectionné même quand le service tourne déjà. L’arrêt ferme les sessions en cours sans arrêter le nouveau récepteur.
- Le serveur Wi-Fi Direct retente son ouverture lorsque l’interface du groupe devient disponible. L’écran affiche son état réel, son rôle et les erreurs de connexion. La réception peut démarrer sans pair sortant sélectionné.
- Un code simple de **5 chiffres** est accepté, avec protection limitée contre les essais de code ; les codes longs existants sont conservés. Le code peut être enregistré séparément ; un repère permet de comparer les codes enregistrés sans recopier leur contenu dans un diagnostic. Un refus du code et une coupure pendant l’authentification donnent désormais des messages distincts.
- « Tester la liaison (sans copier) » vérifie le pair et le code sans lire les médias ni lancer Panic. Les copies restent automatiques après la configuration et les autorisations initiales Android.

Le S22 et le Huawei sous EMUI 10 signalés par l’utilisateur nécessitent encore un essai physique. Voir [le guide](../../docs/PHONE_PAIRING.md) et [les tests](../../docs/TEST_PLAN.md). Un test de liaison réussi ne vaut pas reçu de copie.

SHA-256 : `d15ae023753bac917736ea1776c1cfd54992d4ad9821d9b106de3e48047b0b40`. Signature debug identique à V6 pour permettre une mise à jour.

Validation cloud : 36 exécutions de tests JVM (dont les variantes debug/release du test du catalogue), zéro échec/erreur/ignoré ; lint debug sans problème ; assemblage et signature APK vérifiés. Les radios et le cycle de vie Android nécessitent encore les essais physiques décrits dans le guide.
