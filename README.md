# AppCoWeb Capacitor template

Capacitor wraps a website in a native Android shell. It does not make a fully native app.

1. Push this folder to a new GitHub/GitLab/Bitbucket repo (branch `main`).
2. In Codemagic: Add application -> pick the repo -> "codemagic.yaml" config.
3. Copy the application ID (from the app URL) and use workflow ID `android-build`.
4. Save the Codemagic API token, app ID and workflow ID in AppCoWeb.

The AAB is built unsigned (release signing needs your own keystore added in Codemagic).
The debug APK can be installed directly on a phone for testing.
