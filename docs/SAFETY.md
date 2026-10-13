# Sécurité et limites

Minimal OS est une application installée dans Android, pas une ROM, une clé USB live ou un effacement sécurisé. Elle ne garantit ni zéro trace, ni absence de soft-brick sur tous les firmwares. Elle ne contourne jamais FRP ou l'assistant de configuration.

La Phase 2 reste passive : elle inventorie, simule et journalise, mais ne désactive et ne désinstalle aucun package, ne modifie pas `/system` ou `/vendor`, n'impose pas le rôle HOME et n'utilise ni root, ni ADB, ni Shizuku. L'utilisateur choisit le HOME au moyen du dialogue système et peut ouvrir les réglages à tout moment.

Les phases de debloat devront refuser toute action sans profil exact et conserver comme protégés la téléphonie/IMS, SMS/contacts, SystemUI, paramètres, clavier actif, permissions, réseau, certificats, installateur, launcher natif, administration et récupération. `unknown` signifie toujours « ne pas toucher ».

Le futur bouton rouge ne constituera pas un effacement : il devra rendre la main au launcher natif avant toute désinstallation guidée, ne supprimer que les applications installées exclusivement par Minimal OS avec consentement, conserver le debloat constructeur et supprimer Minimal OS en dernier. Les médias du futur coffre et leur réplication resteront hors de cette procédure.

Le bouton Panic visible en Phase 2 est un préflight : il ne supprime rien. Supprimer Telegram, Briar ou une autre application préexistante sans provenance vérifiée détruirait potentiellement des données qui ne relèvent pas de Minimal OS et est explicitement interdit. Un transfert en attente ne prouve pas qu'un média existe ailleurs ; seul un accusé après taille et SHA-256 complets pourra le faire.
