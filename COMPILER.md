# bTicket — application Android native (étape 1)

Application 100 % native (Kotlin + Jetpack Compose), sans Capacitor ni Node.
Package : `com.bticket.app` — même base Firebase que le site (`bticket-burundi`).

## Contenu de l'étape 1
- Animation du logo au démarrage (touche l'écran pour la passer)
- 3 onglets (Événements, Mes tickets, Compte) : **glisse du doigt** à gauche/droite pour changer
- Barre du bas **masquée quand tu descends**, réaffichée quand tu remontes
- Liste des événements (recherche, catégories, prix « dès », fiche rapide)
- Connexion / inscription acheteur par e-mail (le bouton Google est retiré)
- Mes tickets (liste), Compte (thème clair/sombre, déconnexion)
- Bulle Safi : ouvre https://cinetv257.github.io/CINETV/assistant.html dans une fenêtre intégrée

## Compiler (Android Studio, sans écrire de code)
1. Dézipper le dossier `bticket-android`.
2. Android Studio → **Open** → choisir le dossier `bticket-android` (celui qui contient `settings.gradle.kts`).
3. Attendre la synchronisation Gradle (connexion internet nécessaire la première fois : Gradle 9.6, SDK 36, Build Tools 36).
   Si Android Studio propose d'installer le SDK Platform 36 ou les Build Tools : cliquer **Install**.
   Si une fenêtre propose de « mettre à jour AGP/Gradle » : cliquer **Don't remind me** / ignorer.
4. Brancher le téléphone (Options développeur → Débogage USB) puis ▶ **Run**,
   ou menu **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
   L'APK se trouve dans `app/build/outputs/apk/debug/app-debug.apk`.

Si la synchronisation échoue : onglet **Build** en bas → copier le message d'erreur et me l'envoyer.
Si Android Studio signale un problème de JDK : *File → Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK* = **jbr** (JDK intégré).

## À vérifier sur le téléphone
1. L'animation se joue au lancement, puis l'accueil apparaît en fondu.
2. Les événements s'affichent (sinon : voir « Dépannage »).
3. Glisser vers la gauche/droite change d'onglet ; la barre du bas se cache en faisant défiler vers le bas.
4. Création de compte puis connexion par e-mail.
5. La bulle Safi ouvre la page de l'assistant.

## Dépannage
- **Les événements ne se chargent pas / « permission denied »** : dans la console Firebase, *App Check → APIs → Cloud Firestore*
  doit être en mode non appliqué pour l'instant (l'app Android n'envoie pas encore de jeton App Check).
- **Mes tickets vide** : normal tant qu'aucun ticket n'est lié à ton compte (achat dans l'app = étape 2).
- Police : Montserrat n'est pas encore intégrée (police système utilisée).
