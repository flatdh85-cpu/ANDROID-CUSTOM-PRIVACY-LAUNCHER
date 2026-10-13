# Versions APK

| Version | État | Fonction principale |
| --- | --- | --- |
| [V1](V1/README.md) | Disponible | Launcher MVP |
| [V2](V2/README.md) | Disponible | Audit et profils en simulation |
| [V3](V3/README.md) | Disponible | Copie sur réseau local partagé et Panic guidé |
| [V4](V4/README.md) | Disponible, tests physiques à faire | Bluetooth direct, Wi-Fi Direct et Internet |
| [V5](V5/README.md) | Disponible, tests physiques à faire | Correction des copies Bluetooth et diagnostic du récepteur |
| [V6](V6/README.md) | Disponible, tests physiques à faire | Sonde Panic, découverte Wi-Fi Direct et notifications |
| [V7](V7/README.md) | Disponible, tests physiques à faire | Réception reconfigurée, test de liaison et diagnostic d’authentification |

| [V8](V8/README.md) | Disponible, essais physiques requis | Trois applications, LocalSend autonome, Android 6 et Panic avec auto-validation facultative |
| [V9](V9/README.md) | Disponible, essais physiques requis | Permissions initiales, installation des applications et Panic étendu aux téléchargements |

La V8 remplace le transport radio maison par LocalSend autonome et découple Panic des sauvegardes. [Guide V8](../docs/PHONE_PAIRING.md), [Panic V8](../docs/PANIC.md).

Historique : la V7 devait être installée sur les deux appareils. Elle conserve l'association Bluetooth enregistrée mais utilise une nouvelle version du protocole. Les APK sont des builds debug ; vérifiez la signature et testez sur appareils secondaires avant usage réel.
