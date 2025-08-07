# Auction Management System

# Docker

# GCloud

## To add the local image to the GCloud Artifact Registry

1) Authenticate:

        gcloud auth configure-docker HOSTNAME-LIST (us-east1-docker.pkg.dev, gcr.io, etc)

    For example, gcloud auth configure-docker us-east1-docker.pkg.dev,asia-northeast1-docker.pkg.dev 

2) Tag the local image with the repository name:

        docker tag SOURCE-IMAGE HOSTNAME-LIST/PROJECT-ID/REPO-NAME/TARGET-IMAGE:TAG
   For example, docker tag vrishab.me/spring/auction:1.0 us-east1-docker.pkg.dev/cloud-23831/docker/auction

3) Push the tagged image with the command:

        docker push HOSTNAME-LIST/PROJECT-ID/REPO-NAME/TARGET-IMAGE:TAG
    For example, docker push us-east1-docker.pkg.dev/cloud-23831/docker/auction:latest

 **Note: If you normally run Docker commands on Linux with sudo, Docker looks for Artifact Registry credentials in /root/.docker/config.json instead of $HOME/.docker/config.json**
