plugins { `java-library` }

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
dependencies {
    api(project(":core-device"))
    testImplementation("junit:junit:4.13.2")
}
