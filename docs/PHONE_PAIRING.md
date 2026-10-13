# Essai de copie entre deux téléphones

Installez **le même APK V3** sur A et B. Reliez-les au même réseau Wi-Fi, ou activez le hotspot de A et connectez B à ce hotspot. Aucun accès Internet n'est nécessaire. Accordez l'accès **complet** aux photos et vidéos sur les deux appareils. Sur Android 14+, l'accès « photos sélectionnées » ne suffit pas à copier toute la galerie.

1. Sur A, ouvrez **Copie entre téléphones**, notez son adresse IPv4 locale et son code partagé de 32 caractères, puis touchez **Démarrer la réception**. L'adresse peut changer après une reconnexion.
2. Sur B, ouvrez le même écran, saisissez l'adresse de A et exactement le même code. Choisissez 10, 15, 20 ou 30 minutes, puis touchez **Enregistrer et planifier**.
3. Sur B, touchez **Copier maintenant A ↔ B**. Attendez le message « Copie vérifiée ». L'écran **Voir les copies reçues** sur chaque téléphone doit afficher les médias provenant de l'autre.
4. Créez une nouvelle photo et une nouvelle vidéo sur chaque appareil, relancez la copie, puis vérifiez les quatre contenus sur les deux téléphones. Vérifiez aussi que les originaux restent dans les galeries système.
5. Coupez le réseau au milieu d'une grosse vidéo, reconnectez-le et relancez la copie. Le transfert reprend au dernier bloc complet vérifié. Les fichiers privés partiels ne sont pas affichés comme reçus.

Pour les copies périodiques, laissez le récepteur actif sur A. Android planifie les tentatives de B après l'intervalle choisi, mais les restrictions de batterie et d'arrière-plan peuvent les retarder. La réception est un service au premier plan avec notification ; le démarrage après reboot est tenté, sans garantie si Android ou le fabricant le bloque. Si vous voulez aussi que A initie des sessions lorsque B est disponible, configurez B comme récepteur et saisissez l'adresse de B sur A. Pour un premier essai, un seul initiateur suffit : chaque session copie dans les **deux sens**.

Configurez et testez d'abord la copie manuelle avant Panic. Sur le téléphone à nettoyer, gardez l'adresse et le code du pair. Un appui long sur **Panic** ouvre la procédure, avec une confirmation initiale. Android demande ensuite de confirmer la désinstallation de chaque application de communication reconnue. Une session de copie A↔B démarre, puis Panic vérifie que tous les médias encore présents sur le téléphone existent sur le pair. Android demande l'effacement de la galerie ; l'application revérifie ensuite qu'elle est vide. Shizuku/Canta et Minimal OS sont proposés à la désinstallation en dernier. Un refus ou un échec arrête la suite.

Ne lancez pas Panic simultanément sur les deux téléphones : chacun pourrait supprimer les copies que l'autre vient d'y placer. Les données internes de Telegram, SimpleX, Briar, Bridgefy ou BitChat ne font pas partie de la galerie et ne sont **pas** copiées par Minimal OS. Android ne permet pas à cette application de désinstaller silencieusement les autres apps, de supprimer des médias sans autorisation système ni de garantir un effacement sans trace.

Les essais A↔B, le reboot, la perte Wi-Fi et les confirmations Android doivent encore être exécutés sur deux appareils physiques. La compilation cloud et les tests de sockets sur la machine ne prouvent pas le comportement d'un téléphone réel.
