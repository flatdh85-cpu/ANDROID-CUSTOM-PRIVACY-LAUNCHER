# Protocole de réplication locale — conception

## État

Ce document prépare l'implémentation. La Phase 2 ne transfère encore aucun média et le préflight Panic refuse toute suppression tant qu'une réplication vérifiée et un composant receiver indépendant n'existent pas.

## Environnement très dense

Avec plus de 50 000 personnes, aucun canal Wi-Fi ne peut être garanti libre. Une application Android ordinaire ne peut généralement pas imposer le canal d'un groupe Wi-Fi Direct ; le système ou le propriétaire du groupe le choisit selon le matériel et le firmware. Fixer arbitrairement un canal 2,4 GHz risquerait donc d'aggraver les interférences.

La stratégie prévue est :

1. appairage explicite par QR code, avec identité cryptographique réciproque ;
2. Bluetooth Low Energy uniquement pour retrouver des pairs déjà autorisés, avec fenêtres courtes, temporisation aléatoire et backoff exponentiel ;
3. négociation d'un transport Wi-Fi local pour les médias : Wi-Fi Direct ou hotspot local, avec préférence 5 GHz lorsque les deux appareils et Android le permettent ;
4. Bluetooth classique réservé au contrôle ou à un petit fichier de secours, pas au transfert normal de vidéos ;
5. Wi-Fi Aware comme optimisation facultative sur Android 8+ et matériel compatible, jamais comme dépendance de base.

En 5 GHz, les canaux non-DFS sont préférables lorsqu'ils sont disponibles, car un événement radar peut interrompre un canal DFS. Ce choix reste une préférence transmise au système, pas une promesse. Le protocole doit supporter un changement de réseau, une coupure et la reprise sur un autre transport.

## Réduction du bruit radio

- Ne jamais annoncer ni diffuser un manifeste public à toute la foule.
- Ignorer les appareils non appairés avant toute négociation coûteuse.
- Utiliser un identifiant de découverte éphémère dérivé de l'identité du pair, sans nom lisible ni identifiant stable public.
- Limiter le nombre de connexions simultanées et appliquer une temporisation aléatoire avant les nouvelles tentatives.
- Échanger d'abord un résumé compact des fichiers ; demander ensuite seulement les blocs manquants.
- Suspendre les scans agressifs lorsqu'un transport Wi-Fi fonctionne déjà.

Ces mesures réduisent la charge radio mais ne garantissent ni débit ni disponibilité dans une foule dense. Des essais sur le lieu et sur les modèles cibles restent indispensables.

## Protocole de données

Le futur `core-replication` utilisera un protocole versionné et authentifié de bout en bout : identité de pair issue du QR code, protection anti-rejeu, négociation de capacités, manifeste signé/authentifié, identifiant stable du média, taille, SHA-256 et transfert par blocs reprenables. Le receiver n'accusera réception qu'après écriture durable, taille exacte et SHA-256 complet.

La réplication est bidirectionnelle et cumulative. Une suppression locale n'est jamais transmise et aucun original n'est supprimé après transfert. En cas de stockage insuffisant, la copie échoue explicitement et reste reprenable.

## Intégration Panic future

Le Panic Button de Phase 4 pourra placer les nouveaux médias en priorité et demander plusieurs pairs autorisés. Il ne devra pas attendre la fin du transfert pour rendre le contrôle au launcher natif, ni annoncer « sauvegardé » sans accusé vérifié. Le coffre et le receiver devront vivre dans des composants séparés qui ne sont pas désinstallés pendant cette procédure.

Avant activation : caméra/coffre privé, receiver indépendant, appairage, chiffrement, transfert A→B, réplication A↔B, reprise après reboot et tests sur deux appareils physiques doivent tous être validés.
