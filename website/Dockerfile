FROM node:16.5-alpine
LABEL maintainer="Mikolaj Leszczynski <mikolaj@herebedragons.pl>" \
      description="Docusaurus container for local development and production build on CI"

RUN apk add --no-cache \
    bash supervisor
    
#autoconf automake build-base libtool nasm

# Environments
ENV RUN_MODE='dev'

# Create Docusaurus directory and change working directory to that
RUN mkdir /docusaurus
WORKDIR /docusaurus

# Copy configuration files
ADD docker-docs-config/init.sh /
COPY docker-docs-config/supervisord.conf /etc/supervisor/conf.d/supervisord.conf

EXPOSE 3000
VOLUME [ "/docusaurus" ]

# Set files permission
RUN chmod a+x /init.sh
ENTRYPOINT [ "/init.sh" ]
