# Compatibilité V8

| Composant | Minimum déclaré | Android 6 / API 23 |
| --- | --- | --- |
| Minimal OS V8 | API 21 / Android 5 | Code et manifeste compatibles ; essais physiques requis |
| LocalSend 1.17.0 | API 21 / Android 5 | Compatible selon le minimum déclaré ; installer séparément et tester |
| BitChat, source actuelle | API 26 / Android 8 | Non compatible |
| SimpleX, source stable actuelle | API 26 / Android 8 | Non compatible |

Les raccourcis des applications absentes affichent leur limite. Une ancienne version déjà installée peut être ouverte, sans garantie de compatibilité ou de sécurité de cette version. Minimal OS ne redistribue ni ne rétroporte ces applications.

Sources consultées pour cette livraison :

- [LocalSend v1.17.0 — minSdkVersion 21](https://github.com/localsend/localsend/blob/v1.17.0/app/android/app/build.gradle)
- [BitChat — minSdk 26](https://github.com/permissionlesstech/bitchat-android/blob/main/gradle/libs.versions.toml)
- [SimpleX — minSdk 26](https://github.com/simplex-chat/simplex-chat/blob/stable/apps/multiplatform/android/build.gradle.kts)

Android 6 dispose de l’accès stockage et du service d’accessibilité utilisés par Panic. L’accès complet aux médias et l’activation de l’accessibilité doivent être accordés une fois dans Android. Les dialogues constructeur peuvent empêcher l’auto-validation et rester manuels. Aucune suppression silencieuse privilégiée n’est promise.

Le S22 et le Huawei EMUI 10 ainsi qu’un appareil Android 6 doivent être testés physiquement. Le code conditionne les API récentes à la version Android ; lint n’est pas une preuve d’exécution sur appareil. Aucun profil constructeur non vérifié n’est appliqué. La compatibilité Wi-Fi Direct de LocalSend autonome dépend des appareils et ne peut pas être déduite du seul numéro Android.

## Catalogue installable V9 vérifié

Les APK LocalSend 1.17.0 (API 21), BitChat 2.0.1 (API 26) et SimpleX 7.0.3 (API 26) ont été téléchargées depuis leurs releases officielles et inspectées avec aapt/apksigner. L’assistant filtre le SDK et les ABI et n’installe pas de version incompatible connue. [Versions et architectures](INSTALLATION.md). Le code du launcher V9 conserve API 21 minimum.
