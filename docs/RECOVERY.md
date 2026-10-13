# Récupération

Pour quitter Minimal OS sans Panic, ouvrez « Choisir le launcher HOME » ou Paramètres Android > Applications > Applications par défaut > Application d'accueil, puis sélectionnez le launcher constructeur. Minimal OS ne désactive pas ce launcher.

Panic procède par étapes et n'est pas atomique. Si une désinstallation Android est refusée, si le pair est inaccessible ou si un reçu de copie manque, la suite s'arrête. Les applications déjà désinstallées ne reviennent pas automatiquement ; réinstallez-les depuis leurs sources officielles et restaurez leurs propres sauvegardes si elles existent. La copie Minimal OS ne couvre que les photos et vidéos visibles dans MediaStore ainsi que les copies reçues, pas les bases de données internes des messageries.

Si l'application est interrompue pendant un transfert, relancez la copie A↔B : les blocs complets sont conservés sur le récepteur. Si le téléphone source est réinitialisé ou Minimal OS est désinstallé, le contenu privé stocké dans cet APK sur ce téléphone disparaît. Vérifiez la lecture des copies sur l'autre téléphone avant un nettoyage volontaire.

Si l'écran HOME devient inutilisable, redémarrez selon la procédure officielle du fabricant en mode sans échec et rétablissez le launcher natif depuis les réglages. Le compagnon ADB indépendant et une restauration de debloat constructeur ne sont pas encore livrés.
