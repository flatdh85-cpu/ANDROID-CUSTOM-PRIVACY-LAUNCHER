# Minimal OS V2 — Audit et profils

État : planifiée, aucun APK V2 n'est encore publié.

V2 ajoutera l'inventaire complet des packages, un schéma de profils versionné et une correspondance exacte fabricant/modèle/API/fingerprint. Un appareil inconnu restera en mode audit sans action. Le journal persistant enregistrera l'état initial et préparera une restauration avant toute évolution vers un backend privilégié.

Critères avant publication : parseur et validation de profils, protections des packages critiques, reprise du journal après crash, export de récupération et tests sur appareils secondaires.
