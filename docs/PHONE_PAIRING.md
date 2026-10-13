# LocalSend autonome — V8

Ouvrir **LocalSend** depuis l’accueil et envoyer/recevoir directement dans cette application. Minimal OS ne demande aucun code de pairage et n’ajoute ni écran de copie, ni service récepteur, ni transfert périodique.

LocalSend a besoin d’une liaison IP locale. Il ne sait pas envoyer par Bluetooth direct. Sans Wi-Fi commun ni hotspot, les téléphones doivent pouvoir établir un groupe **Wi-Fi Direct dans les paramètres Android**, et exposer son interface IP aux applications. Cette possibilité varie selon les constructeurs ; aucune compatibilité S22/Huawei n’a été validée ici.

1. Installer une version officielle compatible de LocalSend sur les deux téléphones ; la version 1.17.0 déclare API 21 minimum, compatible en principe avec Android 6. Vérifier l’APK réellement installé.
2. Former le groupe Wi-Fi Direct si les deux appareils le proposent. Une association Bluetooth ne suffit pas.
3. Ouvrir LocalSend sur les deux ; essayer un petit fichier dans chaque sens. Si les pairs ne se voient pas ou que l’envoi échoue, Minimal OS ne peut pas corriger cette liaison.
4. Pour éviter une acceptation à chaque envoi, configurer une fois la réception automatique dans LocalSend, de préférence limitée aux favoris si cette version le permet. Choisir le dossier de réception/enregistrement dans la galerie.
5. Envoyer les photos/vidéos depuis LocalSend et vérifier leur présence sur l’autre téléphone. La copie toutes les 30 minutes n’est pas fournie dans cette version simplifiée.

Aucun canal ni débit n’est garanti parmi 50 000 personnes. Panic ne lance aucun transfert et n’attend pas LocalSend : [guide Panic](PANIC.md).
