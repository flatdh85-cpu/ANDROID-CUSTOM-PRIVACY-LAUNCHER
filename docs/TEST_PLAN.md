# Tests V8 simplifiée

Commande : `./gradlew test testDebugUnitTest lintDebug assembleDebug`.

## Automatique

- Catalogue limité aux trois packages, identifiants uniques et minima Android des versions tierces.
- Auto-validation Panic : désinstallation uniquement avec le nom de l’application attendue ; effacement média uniquement avec le nom Minimal OS et le verbe attendu ; rejet d’Annuler, d’une application différente, d’une permission sans rapport et d’une action inconnue.
- Tests des modules historiques conservés ; ceux du protocole de réplication ne décrivent aucune fonction réseau active de V8.
- Lint Android API 21–35, assemblage et vérification du manifeste et de la signature de l’APK. Les résultats effectifs sont inscrits dans `build/V8/README.md`.

## Essais physiques requis, non exécutés dans le cloud

1. Android 6/API 23 : installation, HOME, trois raccourcis, grande police/petit écran, photo/vidéo/galerie, Wi-Fi/Bluetooth/localisation/Internet, diagnostics et notifications/pastilles. Les applications absentes exigeant Android 8 doivent afficher cette limite. Tester une version LocalSend compatible installée séparément.
2. Mise à jour V7→V8 : pas d’écran « Copie entre téléphones », pas de service radio relancé au boot, ancien job annulé. Les copies privées historiques restent dans le stockage jusqu’au retrait de l’application.
3. Maintien Panic : relâcher avant trois secondes, glisser hors du bouton, quitter l’activité : aucun déclenchement. Maintien complet : ouverture guidée ou démarrage direct si auto-validation préalablement autorisée.
4. Sur galerie jetable : 0/1/2/3 applications présentes, hors réseau, aucune sonde/copie ; seules les cibles sont désinstallées, médias ensuite, Minimal OS dernier, retour HOME. Sans autre HOME actif : blocage avant modification.
5. Android 6 : permission stockage accordée/refusée ; suppression par ContentResolver, interruption et reprise. Android 10 : autorisations récupérables. Android 11+ : lots de plus de 100 médias, carte SD, corbeille, permission partielle, refus système. Ne pas annoncer un effacement si des médias restent.
6. Accessibilité Panic activée/désactivée sur Android 6, S22 et Huawei : dialogues FR/EN, attente manuelle sur fenêtres inconnues/protégées, refus de clic et expiration après 20 secondes ; aucun clic hors Panic ou sur une autre application. Auto-désinstallation en dernier. Les tests JVM de texte ne remplacent pas ce test système.
7. Arrêt de processus, rotation, refus d’uninstall et sortie de Panic : état partiel visible, reprise depuis l’inventaire actuel. Vérifier appels, SMS, clavier et launcher constructeur après.
8. LocalSend autonome : essai d’un fichier dans chaque sens sur un groupe Wi-Fi Direct Android sans infrastructure/hotspot. Ce test vérifie LocalSend et l’OS ; aucune planification 30 min ni compatibilité radio universelle n’est annoncée par Minimal OS.

## V9

Tests JVM supplémentaires : suppression d’APK/documents cachés/sous-dossiers avec préservation du dossier racine et des fichiers extérieurs ; liens symboliques non suivis, racine redirigée refusée, annulation sans suppression. Catalogue APK : Android 6 limité à LocalSend, choix d’ABI selon l’ordre de l’appareil, ABI non proposée rejetée, intégrité et sources officielles épinglées.

Essais physiques supplémentaires requis : permission de stockage Android 6 et accès spécial aux fichiers Android 11+, refus/révocation, dossier vide/manquant/non accessible, suppression partielle, téléchargements tiers concurrents, APK/ZIP/PDF, liens et fichiers extérieurs conservés. Vérifier qu’un échec d’effacement des téléchargements empêche l’auto-désinstallation.

Installer les applications : premier lancement, passage en arrière-plan, interruption de téléchargement/processus, stockage plein, source refusée, refus de chaque installation, reprise, APK altérée/signature incorrecte, applications déjà présentes, Android 6 et ARMv7/ARM64. Les signatures/minima des APK officielles ont été vérifiés dans le cloud ; aucune installation sur téléphone physique n’y a été réalisée.

Premier lancement : permissions runtime sur Android 6/13/14+, localisation approximative, médias sélectionnés seulement, accès spécial aux fichiers Android 11+, refus et refus permanent, retour des réglages, relance sans nouvelle sollicitation automatique. Vérifier que l’accueil reste disponible et que l’accessibilité Panic n’est jamais activée par cet assistant.
