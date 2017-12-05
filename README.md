Projet de microservice sécurisé pour la création du pipeline de déploiement dans Kubernetes
------------------------------------------------------------------------

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

Cette commande build le projet et obtient la définition du cloud endpoint qui est copié dans le fichier 

    openapi-generated.yaml

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

retrouver le config_id

    gcloud endpoints configs list --service=greeting-api.endpoints.lacapitalepilotage.cloud.goog

Et ajuster le fichier Deployment-oauth.yaml avec la valeur

    image: gcr.io/endpoints-release/endpoints-runtime:1
    args: [
      "--http_port", "8080",
      "--ssl_port", "443",
      "--backend", "127.0.0.1:8081",
      "--service", "greeting-api.endpoints.lacapitalepilotage.cloud.goog",
      "--service_account_key", "/etc/nginx/creds/service-account-creds.json",
      "--version", "2017-12-03r1",
      "-n", "/etc/nginx/custom/nginx.conf"
    ]

Deployer un certificat dans kubernetes

    kubectl create secret generic nginx-ssl --from-file=./nginx.crt --from-file=./nginx.key

Deployer une configuration custom de nginx pour tls et cors

    kubectl create configmap nginx-config-oauth --from-file=nginx.conf

Créer l'image docker à partir du Dockerfile

    docker build -t vcrepin/greeting:0.1.9 .

Pousser l'image dans le registre

    docker push vcrepin/greeting:0.1.9

Deployer le backend

    kubectl create -f Deployment-oauth.yaml

La suite permet d'utiliser le API Gateway (Kong)

Deploy greeting svc

    kubectl create -f Deployment-greeting.yaml

Connect API to Kong API gateway

    curl -i -X POST \
      --url http://35.227.61.134:8001/apis/ \
      --data 'name=greeting-api' \
      --data 'hosts=greeting-api.com' \
      --data 'upstream_url=http://greeting-svc'

Set auth plugin to API 

    curl -i -X POST \
      --url http://35.227.61.134:8001/apis/greeting-api/plugins/ \
      --data 'name=key-auth'

Create user

    curl -i -X POST \
      --url http://35.227.61.134:8001/consumers/ \
      --data "username=Ionic-GKE"

Set user key

    curl -i -X POST \
      --url http://35.227.61.134:8001/consumers/Ionic-GKE/key-auth/ \
      --data 'key=qwerty01!'

Enable CORS

    curl -X POST http://35.227.61.134:8001/apis/greeting-api/plugins \
        --data "name=cors" \
        --data "config.origins=*" \
        --data "config.methods=GET, POST, PUT, PATCH" \
        --data "config.headers=Accept, Accept-Version, Content-Length, Content-MD5, Content-Type, Date, X-Auth-Token, apikey" \
        --data "config.exposed_headers=X-Auth-Token" \
        --data "config.credentials=false" \
        --data "config.max_age=3600"

Create jwt consumer

    curl -i -X POST \
           --url http://35.227.61.134:8001/consumers/ \
           --data "username=Ionic-GKE-JWT"

    curl -X POST http://35.227.61.134:8001/consumers/Ionic-GKE-JWT/jwt -H "Content-Type: application/x-www-form-urlencoded"

{"created_at":1512353113000,"id":"d2a8581a-4d20-4b2f-9a8a-e7e1a5c95797","algorithm":"HS256","key":"umfJOIMrNYIc9kmAF5aVBBE3QY8wH1YV","secret":"EXCTUzmOB1OJwMca8jdMS8vkz7z7GyNU","consumer_id":"9e06e3ac-42b7-4012-bd49-ba88e4d3b87c"}

Get the list of consumers

    curl -X GET http://35.227.61.134:8001/consumers/Ionic-GKE-JWT/jwt

{"total":1,"data":[{"created_at":1512353113000,"id":"d2a8581a-4d20-4b2f-9a8a-e7e1a5c95797","algorithm":"HS256","key":"umfJOIMrNYIc9kmAF5aVBBE3QY8wH1YV","secret":"EXCTUzmOB1OJwMca8jdMS8vkz7z7GyNU","consumer_id":"9e06e3ac-42b7-4012-bd49-ba88e4d3b87c"}]}

{
    "typ": "JWT",
    "alg": "HS256"
}

{
    "iss": "umfJOIMrNYIc9kmAF5aVBBE3QY8wH1YV"
}

secret: EXCTUzmOB1OJwMca8jdMS8vkz7z7GyNU

eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJ1bWZKT0lNck5ZSWM5a21BRjVhVkJCRTNRWTh3SDFZViJ9.KpZ2fN78VSSz_dlUOAiuFET8Paj1cY7xljOELPAL2gQ

curl -i -X POST \
      --url http://35.227.61.134:8001/apis/ \
      --data 'name=greeting-api-jwt' \
      --data 'hosts=greeting-api-jwt.com' \
      --data 'upstream_url=http://greeting-svc'

curl -X POST http://35.227.61.134:8001/apis/greeting-api-jwt/plugins \
    --data "name=jwt"

curl -X POST http://35.227.61.134:8001/apis/greeting-api-jwt/plugins \
        --data "name=cors" \
        --data "config.origins=*" \
        --data "config.methods=GET, POST, PUT, PATCH" \
        --data "config.headers=Accept, Accept-Version, Content-Length, Content-MD5, Content-Type, Date, X-Auth-Token, Authorization" \
        --data "config.exposed_headers=X-Auth-Token" \
        --data "config.credentials=false" \
        --data "config.max_age=3600"

OAuth

curl -i -X POST \
      --url http://35.227.61.134:8001/apis/ \
      --data 'name=greeting-api-oauth' \
      --data 'hosts=greeting-api-oauth.com' \
      --data 'upstream_url=http://greeting-svc'

curl -X POST http://35.227.61.134:8001/apis/greeting-api-oauth/plugins \
        --data "name=cors" \
        --data "config.origins=*" \
        --data "config.methods=GET, POST, PUT, PATCH" \
        --data "config.headers=Accept, Accept-Version, Content-Length, Content-MD5, Content-Type, Date, X-Auth-Token, Authorization" \
        --data "config.exposed_headers=X-Auth-Token" \
        --data "config.credentials=false" \
        --data "config.max_age=3600"

curl -X POST http://35.227.61.134:8001/apis/greeting-api-oauth/plugins \
    --data "name=oauth2" \
    --data "config.enable_implicit_grant=true" \
    --data "config.scopes=greet" \
    --data "config.mandatory_scope=false"

docker run --rm -p 8080:8080 pgbi/kong-dashboard start \
  --kong-url http://35.227.61.134:8001
  --basic-auth vcrepin=Vinnes.,