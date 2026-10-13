# Plan de test V7

La compilation et les tests automatiques couvrent la copie A↔B sur sockets et sur flux comme Bluetooth, le rejet d'un mauvais code, la reprise à la limite d'un bloc, et la protection des identifiants de fichiers. Lint doit réussir. Les scénarios radio exigent deux téléphones physiques ; aucun résultat n'est présumé ici.

1. Installer l'APK V7 sur A et B ; vérifier les huit raccourcis, **Internet**, photo, vidéo, galerie, HOME, le thème, le launcher constructeur et les permissions médias complètes.
2. Associer A et B via Android Bluetooth. Activer la réception sur les deux, sélectionner le pair et le même code, puis créer A1/A2 sur A et B1/B2 sur B. Copier et vérifier que les quatre fichiers sont lisibles sur les deux et ont le même SHA-256. Répéter sans nouvelle demande d'appairage ou de permission par copie, puis avec une vidéo volumineuse ; mesurer le débit.
3. Refaire le test sur **Wi-Fi Direct**, sans WLAN commun ni hotspot. Vérifier la découverte, le code d'erreur Android `0`, les pairs déjà connus, le groupe déjà actif, la demande de connexion Android, le rôle chef/client et le cas où la copie doit être lancée depuis l'autre téléphone.
4. Tester les intervalles 10/15/20/30 minutes, la réception active, le redémarrage, l'écran verrouillé et les politiques de batterie EMUI/MIUI. Mesurer les délais réels ; ne pas attendre une cadence exacte.
5. Couper Bluetooth ou Wi-Fi Direct au milieu d'une vidéo, relancer et vérifier la reprise ; tester stockage saturé, mauvais code, permissions refusées sur le récepteur, mauvais appareil sélectionné, pair absent et un média modifié pendant le transfert. Vérifier les messages d'erreur sur les deux téléphones. Aucun échec ne doit afficher « copie vérifiée » ni effacer l'original.
6. Supprimer localement une copie reçue sur A puis synchroniser : B doit garder son fichier. Confirmer aussi qu'une suppression locale de B ne supprime rien sur A.
7. Sur appareils secondaires, tester Panic avec pair joignable puis absent, plusieurs connexions Bluetooth simultanées, mauvais code, désinstallation refusée, suppression galerie refusée et interruption à chaque étape. La sonde ne doit lire aucun média et aucune désinstallation ne commence sans réponse authentifiée. Les confirmations Android doivent apparaître ; ne jamais prétendre avoir effacé ce qui subsiste. Ne lancez pas Panic simultanément sur A et B.
8. Accorder puis retirer l'accès aux notifications. Vérifier les pastilles et la liste pour Telegram, SimpleX et les autres raccourcis installés ; vérifier la disparition après retrait et l'absence de duplication des résumés de groupe.
9. Répéter selon les versions disponibles : Android 5/6/7, Android 10 EMUI, Android 11+, Samsung et Xiaomi. Mesurer PSS, batterie et durée des vidéos. Vérifier appels, SMS, données mobiles, Bluetooth, Wi-Fi, GPS, clavier et notifications avant/après.

## Régressions V7

Tests JVM ajoutés : une coupure après le défi d’authentification doit signaler une liaison interrompue (pas un mauvais code) ; les présentations groupées et en majuscules du code donnent la même clé et le même repère ; le serveur TCP accepte une sonde valide après un mauvais code ; l’arrêt interrompt un client bloqué et libère le port pour réouverture ; un serveur arrêté avant son démarrage ne rouvre pas le port.

Essais physiques prioritaires : Samsung S22 ↔ Huawei sous EMUI 10, signalés par l’utilisateur. La version Android du S22 et le modèle précis du Huawei restent à relever. Ces essais ne sont pas réalisables dans le cloud.

- Passer de Bluetooth à Wi-Fi Direct pendant que la réception tourne, enregistrer et vérifier que l’état affiché correspond au nouveau transport sur les deux appareils.
- Comparer les repères des codes enregistrés, toucher « Tester la liaison (sans copier) », puis copier un petit média dans chaque sens. Un succès de la sonde ne prouve pas l’accès à la galerie ni la réussite d’une copie.
- Former un groupe P2P avant/après le démarrage du service ; vérifier l’état « en écoute » sur le chef, puis déclencher la copie depuis l’autre téléphone. Couper/reformer le groupe et vérifier la réouverture du port.
- Après tout échec, relever le message complet et l’état du récepteur sur les deux téléphones. Vérifier aussi écran allumé puis verrouillé, selon les réglages de batterie EMUI/One UI.

Le sens Bluetooth signalé est Huawei émetteur → S22 récepteur. Vérifier le démarrage de réception sur le S22 avec une liste de pairs vide, puis la sélection du S22 côté Huawei. Tester un code de 5 chiffres incluant un zéro initial, les anciens codes longs, les repères enregistrés et le changement de code entre sessions. Un vecteur PBKDF2 indépendant et un échange A↔B par code court font partie des tests JVM.

Le test de sonde refuse aussi une réponse annonçant une acceptation sans preuve HMAC valide : un simple octet positif ne peut pas autoriser Panic.
