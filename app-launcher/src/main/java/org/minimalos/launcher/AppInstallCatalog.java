package org.minimalos.launcher;
import java.util.Arrays;
/** Immutable official release artifacts inspected with aapt and apksigner before publication. */
final class AppInstallCatalog {
    static final class Asset {
        final String key, label, packageName, url, sha256, certificate;
        final int minimumSdk; final String[] abis;
        Asset(String key, String label, String packageName, int minimumSdk, String[] abis, String url, String sha256, String certificate) {
            this.key=key; this.label=label; this.packageName=packageName; this.minimumSdk=minimumSdk;
            this.abis=abis; this.url=url; this.sha256=sha256; this.certificate=certificate;
        }
    }
    static final Asset[] ASSETS = {
        new Asset("localsend-arm64", "LocalSend", "org.localsend.localsend_app", 21, new String[]{"arm64-v8a"}, "https://github.com/localsend/localsend/releases/download/v1.17.0/LocalSend-1.17.0-android-arm64v8.apk", "2c7f5fd4872da25115bb8e5e62f92de94dda47b0f249ff387ac667b13871dc3e", "3220c353a73cfbd0c2f3052471c445324cf452bcba26de1c473a52fe5c44e1d6"),
        new Asset("localsend-arm32", "LocalSend", "org.localsend.localsend_app", 21, new String[]{"armeabi-v7a"}, "https://github.com/localsend/localsend/releases/download/v1.17.0/LocalSend-1.17.0-android-arm32v7.apk", "69559800fcbd27d85929725492cfe5dba3f9627dec196f53312bbb3a0691466c", "3220c353a73cfbd0c2f3052471c445324cf452bcba26de1c473a52fe5c44e1d6"),
        new Asset("localsend-x64", "LocalSend", "org.localsend.localsend_app", 21, new String[]{"x86_64"}, "https://github.com/localsend/localsend/releases/download/v1.17.0/LocalSend-1.17.0-android-x64.apk", "265dbb699fe13796a8c341fabff4ad92cc1289f47b2025f16e27d7787e88222b", "3220c353a73cfbd0c2f3052471c445324cf452bcba26de1c473a52fe5c44e1d6"),
        new Asset("bitchat", "BitChat", "com.bitchat.droid", 26, new String[]{"arm64-v8a","armeabi-v7a","x86_64","x86"}, "https://github.com/permissionlesstech/bitchat-android/releases/download/v2.0.1/bitchat-android-universal.apk", "fc0c833616fbc2a7bf4b85710f21b9d77d0cdfc87d3dfe6a85e3c919406eff85", "3b03fa66a5451321100792f5b55a7b4966d5c8dc10c6daa40aa95ea489531bca"),
        new Asset("simplex-arm64", "SimpleX", "chat.simplex.app", 26, new String[]{"arm64-v8a"}, "https://github.com/simplex-chat/simplex-chat/releases/download/v7.0.3/simplex-aarch64.apk", "d535e5a8986d4e11b9abd5656a3c0e1be5a08e06b00a7b20d6b7587ed0c3457f", "3c52c4fd3cad1c07c9b00a7080e358fab9fefcb8af5aec147765f16d0f21ad85"),
        new Asset("simplex-arm32", "SimpleX", "chat.simplex.app", 26, new String[]{"armeabi-v7a"}, "https://github.com/simplex-chat/simplex-chat/releases/download/v7.0.3/simplex-armv7a.apk", "3a89d2f825ce1a30ff51b30487c1b0b58de6eba03426d1500112b7417e659fdc", "3c52c4fd3cad1c07c9b00a7080e358fab9fefcb8af5aec147765f16d0f21ad85")
    };
    static Asset select(String packageName, int sdk, String[] abis) {
        for (String abi : abis) for (Asset asset : ASSETS)
            if (asset.packageName.equals(packageName) && sdk >= asset.minimumSdk && Arrays.asList(asset.abis).contains(abi)) return asset;
        return null;
    }
    static Asset find(String key) { for (Asset asset : ASSETS) if (asset.key.equals(key)) return asset; return null; }
}
