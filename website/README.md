# Website

This website is built using [Docusaurus 2](https://docusaurus.io/), a modern
static website generator.

## Doc versioning

Doc versions are generated from git release tags at build time — nothing is
committed to the repository:

- The site root serves the docs from the **latest release tag**, so the
  published docs always match the latest release.
- `/next/` serves the working `docs/` directory (docs for unreleased changes).
- Older majors are served at `/11.x/`, `/10.x/`, etc. — one version per major,
  taken from the latest release tag of that major.

`scripts/generate-versions.mjs` produces `versions.json`, `versioned_docs/` and
`versioned_sidebars/` (all gitignored) and is run by CI before the site build.
Releasing works as usual: tag a commit and CI regenerates the site — a new
major tag automatically becomes the root version. To retire a version from the
selector, raise `MIN_MAJOR` in the script.

To preview the versioned site locally, run `yarn generate-versions` (requires
git and node on the host) before building. Without it, the working `docs/` are
served at the root as before.

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
