# Associer deux téléphones en V7

Installez **le même APK V7** sur A et B, en mise à jour pour conserver les réglages et les copies privées. V7/MOS8 ne synchronise pas avec V6/MOS7. Aucun Wi-Fi commun, hotspot ni Internet n’est nécessaire. Les copies ne demandent pas de confirmation dans Minimal OS après configuration ; les autorisations initiales et les confirmations de groupe éventuelles restent gérées par Android.

## Vérifier la liaison avant de copier

1. Dans **Copie entre téléphones**, comparez le **Repère du code enregistré** sur A et B. Il doit être identique. Le code est conservé lors d’une mise à jour ; pour le changer, recopiez celui d’un téléphone sur l’autre, puis touchez **Enregistrer le code partagé**. Les espaces, tirets et majuscules sont acceptés. Le repère aide à vérifier les réglages ; il ne remplace pas l’authentification et le code reste privé. Vous pouvez saisir **5 chiffres**, ou toucher **Créer un code de 5 chiffres** sur un seul téléphone puis recopier ce code sur l’autre. Les anciens codes longs restent acceptés. Un code court offre seulement 100 000 possibilités ; le chiffrement ne compense pas cette faible résistance aux essais de code.
2. Choisissez le même transport sur les deux téléphones, puis sélectionnez le pair sur celui qui lancera la copie. Touchez **Démarrer la réception directe** sur les deux. La V7 reconfigure le service si le transport a changé et affiche son état effectif en bas de l’écran. Le S22 peut démarrer sa réception même si sa liste d’appareils est vide : seul le téléphone qui lance la copie doit sélectionner le pair.
3. Touchez **Tester la liaison (sans copier)** depuis le téléphone de départ. Cette sonde ne lit pas la galerie et ne déclenche aucune suppression. Son succès prouve que le pair a répondu au code ; il ne prouve pas que la galerie est accessible ou copiée.
4. Accordez l’accès complet aux photos/vidéos, puis essayez **Copier maintenant A ↔ B** avec une photo et une courte vidéo sur chaque appareil. Vérifiez leur présence et leur lecture dans **Voir les copies reçues** avant d’utiliser Panic.

## Bluetooth : Huawei sous EMUI 10 vers S22

Les téléphones doivent être associés une fois dans les réglages Bluetooth Android. Sur le S22 récepteur, attendez **Bluetooth : en écoute**, puis lancez le test de liaison sur le Huawei. Si les repères correspondent mais que la liaison échoue, relevez **le message complet du Huawei et l’état du récepteur S22**.

- « Le récepteur a refusé le code » : le serveur a explicitement rejeté la preuve. Comparer les codes enregistrés, pas seulement le texte encore en cours de saisie.
- « Liaison interrompue » : la connexion s’est fermée avant une réponse exploitable ; cela ne démontre pas un mauvais code. L’état du récepteur aide à identifier l’arrêt ou l’erreur.
- « Versions Minimal OS différentes » : vérifier la V7 sur les deux appareils.

Le test retente les coupures, mais ne contourne jamais un refus d’authentification. Les vidéos volumineuses peuvent prendre du temps en Bluetooth.

## Wi-Fi Direct

Activez Wi-Fi et les permissions radio Android, recherchez et sélectionnez le pair, puis démarrez la réception sur les deux appareils. L’erreur de découverte `0` est générique ; elle ne prouve pas que la localisation est éteinte. La recherche consulte aussi les pairs connus et les groupes déjà formés.

Après formation du groupe, un téléphone affiche **Wi-Fi Direct : en écoute sur … ; lancez la copie depuis l’autre téléphone**. L’autre affiche **groupe connecté ; lancez la copie depuis ce téléphone** : utilisez ce dernier pour tester puis copier. La session copie dans les deux sens. L’application indique le rôle effectif ; ne supposez pas que le rôle chef/client reste fixe.

Si le groupe est connecté mais la réception inaccessible, relevez l’état de celui qui doit écouter. La V7 retente l’ouverture du port si Android configure encore l’interface. Un refus de connexion et un délai dépassé sont affichés avec leur cause technique ; ils ne sont pas assimilés à une erreur de code. Android peut demander confirmation de création du groupe P2P, y compris lors d’une reconnexion.

## Planification et notifications

Planifiez 10, 15, 20 ou 30 minutes sur **un seul** téléphone ; l’autre garde sa réception active. Une session copie dans les deux sens. Android peut différer ces tentatives. Si cela marche écran allumé mais échoue écran verrouillé, vérifier les restrictions de batterie et de lancement en arrière-plan de Minimal OS sur EMUI et One UI. Les cadences et le maintien du service doivent être mesurés sur les deux appareils.

Pour les messages de « Communication essentielle », touchez **Activer les notifications dans Android** sur l’accueil et accordez une fois l’accès spécial à Minimal OS. Les notifications apparaissent en liste et en compteur. Si le réglage d’un APK installé hors magasin est grisé, Android peut exiger d’autoriser les « paramètres restreints » dans la fiche de l’application. Les résumés restent en mémoire.
