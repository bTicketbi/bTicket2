# Compiler bTicket en ligne et télécharger l'APK

Ce projet contient un workflow GitHub Actions pour compiler l'application Android sans installer Android Studio et sans utiliser de terminal sur votre téléphone. Le code Kotlin de l'application n'est pas modifié par cet ajout.

## 1. Créer un dépôt GitHub

1. Ouvrez https://github.com et connectez-vous (ou créez un compte).
2. Appuyez sur **New repository**.
3. Donnez-lui un nom, par exemple `bticket-android`.
4. Choisissez **Private** si vous souhaitez garder le code privé, puis créez le dépôt.

## 2. Envoyer les fichiers du projet

Décompressez `bticket-android-etape1-github-actions.zip` si nécessaire. Dans le dépôt GitHub, utilisez **Add file → Upload files** et envoyez le contenu du dossier `bticket-android` — pas le dossier parent lui-même.

Vérifiez que les fichiers `settings.gradle.kts`, `build.gradle.kts`, `gradlew` et le dossier `app` apparaissent à la racine du dépôt. Le fichier `.github/workflows/android-apk.yml` doit aussi être présent. Les fichiers dont le nom commence par un point peuvent être cachés par le gestionnaire de fichiers Android : si `.github` manque, il faut l'ajouter en utilisant l'interface web GitHub ou un navigateur affichant les fichiers cachés.

Validez l'envoi avec **Commit changes** sur la branche `main`.

## 3. Lancer la compilation

1. Dans le dépôt, ouvrez l'onglet **Actions**.
2. Choisissez **Compiler bTicket APK** dans la liste des workflows.
3. Appuyez sur **Run workflow**, choisissez `main`, puis confirmez avec **Run workflow**.
4. Ouvrez l'exécution apparue et attendez que les étapes se terminent. Une première compilation peut prendre plusieurs minutes.

Le workflow se lance aussi automatiquement lors d'un nouvel envoi de modifications sur `main` ou `master`.

## 4. Télécharger le fichier APK

Lorsque la compilation réussit, ouvrez la page de cette exécution GitHub Actions. En bas, dans **Artifacts**, appuyez sur `bticket-debug-apk`. GitHub télécharge un ZIP contenant `app-debug.apk`. Décompressez-le pour récupérer le fichier APK.

Sur Android, ouvrez le fichier APK et autorisez temporairement l'installation depuis cette source si le téléphone le demande. N'installez que les APK que vous avez compilés ou dont vous connaissez la provenance.

## À savoir

- Le fichier généré est un **APK debug**, adapté aux tests et à l'installation manuelle. Il n'est pas signé pour une publication officielle sur Google Play.
- La compilation nécessite une connexion Internet et dépend de la disponibilité des versions de Gradle, du plugin Android, de Kotlin, des dépendances et du SDK utilisés par le projet.
- Si le workflow échoue, ouvrez l'étape rouge dans Actions et copiez le premier message d'erreur pertinent. Certaines versions de plugins/de dépendances ou certains réglages du SDK peuvent nécessiter une correction de compatibilité.
- `app/google-services.json` est nécessaire à la configuration Firebase. Ne publiez pas de clés privées, mots de passe ou fichiers de signature dans un dépôt public.
