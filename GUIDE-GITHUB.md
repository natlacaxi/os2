# Mettre MiniOS sur GitHub — de A à Z (débutant)

## Partie 1 — Le site (GitHub Pages), sans rien installer
1. Va sur https://github.com et clique **Sign up** (crée un compte gratuit, confirme ton e-mail).
2. En haut à droite : **+** → **New repository**.
3. Nom du dépôt : `minios`. Laisse **Public**. Coche **Add a README file**. Clique **Create repository**.
4. Dézippe `MiniOS-10-repo.zip` sur ton ordinateur. Ouvre le dossier `minios-repo`.
5. Dans ton dépôt GitHub : **Add file** → **Upload files**.
6. Glisse **tout le contenu** du dossier `minios-repo` (les dossiers `docs`, `app`, et les fichiers) dans la page. Attends la fin du chargement.
7. En bas : **Commit changes** (bouton vert).
8. **Settings** (onglet du dépôt) → **Pages** (menu de gauche).
9. Source : **Deploy from a branch**. Branch : `main`, dossier `/docs`. **Save**.
10. Attends 1 à 2 minutes, recharge : l'adresse apparaît en haut :
    `https://TON-PSEUDO.github.io/minios/`
11. Ouvre-la sur ton téléphone/tablette → menu Chrome → **Installer l'application** : MiniOS s'ouvre en plein écran.

## Partie 2 — Fabriquer l'APK Android (le « vrai » écran d'accueil)
Le dossier `.github` (point devant) est caché sur certains ordinateurs et peut ne pas se téléverser. Vérifie :
1. Dans ton dépôt, regarde si `.github/workflows/apk.yml` existe.
2. Sinon : **Add file** → **Create new file**. Dans le nom, tape exactement `.github/workflows/apk.yml`
   (les `/` créent les dossiers). Colle le contenu du fichier `apk.yml` fourni, puis **Commit changes**.
3. Onglet **Actions**. Si GitHub demande d'activer les workflows, clique le bouton vert.
4. Clique **Build APK** → **Run workflow** (ou il démarre seul après chaque modification). Compte ~5 minutes.
5. Quand c'est vert ✅ : clique sur l'exécution → en bas **Artifacts** → **MiniOS-apk** (zip). Dézippe : `app-debug.apk`.
6. Envoie l'APK sur ton téléphone, ouvre-le (autorise « sources inconnues » si Android le demande).
7. Ouvre MiniOS → **Réglages → Général → Choisir MiniOS comme écran d'accueil**. Appuie sur « Accueil » : MiniOS est ton écran d'accueil.
   Pour revenir à l'ancien : mêmes réglages Android.

Si le build devient rouge ❌ : clique dessus, copie les dernières lignes d'erreur et envoie-les moi.

## Partie 3 — Modifier plus tard
- Une modification de `docs/index.html` : ouvre le fichier sur GitHub → crayon ✏️ → **Commit changes**. Le site se met à jour en ~1 minute.
- Quand tes clients achèteront leur nom de domaine : **Settings → Pages → Custom domain**, puis suis l'aide DNS de GitHub.

## Mots utiles
- **Dépôt (repository)** : le dossier de ton projet sur GitHub. **Commit** : enregistrer une modification.
- **Pages** : l'hébergement gratuit du site. **Actions** : les robots qui construisent l'APK pour toi.
