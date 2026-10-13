# Minimal OS V3 — copie locale et Panic guidé

État : APK debug V3 construit et testé automatiquement. Essais sur deux appareils Android encore nécessaires.

Version 0.3.0, Android 5.0+ (API 21), cible Android 15 (API 35). Le même APK sert de launcher et de récepteur sur les deux téléphones. La copie A↔B s'effectue sur un Wi-Fi local ou hotspot existant, avec code partagé, chiffrement, vérification SHA-256 et reprise par blocs. Les tentatives périodiques peuvent être réglées sur 10, 15, 20 ou 30 minutes ; Android peut les retarder. Les copies reçues sont visibles dans l'application.

Panic est une procédure guidée : confirmation initiale, demandes Android de désinstallation des applications de communication reconnues, copie et reçu vérifié, demande Android d'effacement de la galerie, puis demandes de désinstallation des utilitaires et de Minimal OS. Un refus ou un échec stoppe la suite. Les données internes des messageries ne sont pas transférées et un effacement sans trace n'est pas garanti.

Voir [l'essai sur deux téléphones](../../docs/PHONE_PAIRING.md), [le protocole](../../docs/TRANSFER_PROTOCOL.md) et [les limites](../../docs/SAFETY.md).

Empreinte SHA-256 de l'APK : `34e35986ee023752441a86ef3abc199aa327710f7083b2288efb05a8f28ac7fc`.
