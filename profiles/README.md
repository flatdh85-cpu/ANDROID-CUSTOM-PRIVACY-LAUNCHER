# Profils d'appareil

Le schéma `profile.schema.v1.json` décrit les champs obligatoires d'un profil exact. Aucun profil constructeur n'est livré tant qu'il n'a pas été validé sur le modèle, l'API Android, la version constructeur et le fingerprint indiqués.

Une différence de fingerprint ou de version constructeur impose le mode audit. Un package absent du profil reçoit implicitement la classification `unknown` et ne doit jamais être modifié.
