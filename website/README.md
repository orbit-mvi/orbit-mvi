# Website

This website is built using [Docusaurus 2](https://docusaurus.io/), a modern
static website generator.

## Preparations

Before running any website commands, build the bundled docusaurus dockerfile and
copy the dokka static documentation.

```console
cd ..
./gradlew copyDokkaToWebsite
cd website
docker build -t orbit-web:latest .
```

When run, the image will install necessary node modules if the `node_modules`
folder is not present.

## Local Development

```console
docker run --rm -p 3000:3000 -v `pwd`:/docusaurus -e RUN_MODE='dev' orbit-web
```

This command starts a local development server. Most changes are reflected live
without having to restart the server. The container will be auto-removed after
it's stopped.

## Build

```console
docker run --rm -p 3000:3000 -v `pwd`:/docusaurus -e RUN_MODE='build' orbit-web
```

This command generates static content into the `build` directory that can be
served using any static contents hosting service.

## Update

```console
docker run --rm -p 3000:3000 -v `pwd`:/docusaurus -e RUN_MODE='update' orbit-web
```

Run this command after updating the docusaurus version in `package.json` to
update node modules.
