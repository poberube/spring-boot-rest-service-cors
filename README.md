Projet de service exemple pour la création du pipeline de déploiement dans Kubernetes

La première étape consiste à créer un compte de service dans GKE

  ---insérer commande gcloud

Récupérer le fichier de clé privée, le renommer et l'ajouter au projet

service-account-creds.json

Utiliser le namespace cible pour toutes les commandes subséquentes

kubectl config set-context $(kubectl config current-context) --namespace=lacapitaletemplate

Créer un secret dans kubernetes avec le fichier de credentials

kubectl create secret generic service-account-creds --from-file=service-account-creds.json

builder le projet

mvn install

Cette commande build le projet et obtient la définition du cloud endpoint qui est copié dans le fichier openapi-swagger.json

Ajouter les entrees specifiques a google:

  firebase:
    type: oauth2
    authorizationUrl: 'https://cloud.lacapitale.com/sso/login'
    flow: implicit
    x-google-issuer: "https://securetoken.google.com/lacapitalepilotage"
    x-google-jwks_uri: "https://www.googleapis.com/service_accounts/v1/metadata/x509/securetoken@system.gserviceaccount.com"
    x-google-audiences: "lacapitalepilotage"
    scopes:
      greet: allowGreet

Deployer le cloud endpoint

gcloud endpoints services deploy openapi-swagger.yaml

retrouver le client_id

gcloud endpoints configs list --service=greeting-api.endpoints.lacapitalepilotage.cloud.goog

Deployer un certificat dans kubernetes

kubectl create secret generic nginx-ssl --from-file=./nginx.crt --from-file=./nginx.key

Deployer une configuration custom de nginx pour tls et cors

kubectl create configmap nginx-config-oauth --from-file=nginx.conf

Deployer le backend

kubectl create -f Deployment-oauth.yaml

