// Generates Docusaurus doc versions from git release tags at build time.
//
// For each major release version (>= MIN_MAJOR), the docs from the latest
// release tag of that major are extracted into versioned_docs/, so the site
// root always serves the docs matching the latest release, while the working
// docs/ directory is published at /next/. Nothing this script produces is
// committed — versions.json, versioned_docs/ and versioned_sidebars/ are
// regenerated on every build (see website/.gitignore).
//
// Requires git (with the release tags fetched) and node; no npm dependencies.

import {execFileSync} from 'node:child_process';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import {pathToFileURL, fileURLToPath} from 'node:url';

// Docs from tags below this major predate the Docusaurus 3 migration and no
// longer compile; raise it to retire a version from the selector.
const MIN_MAJOR = 10;

const websiteDir = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const repoDir = path.resolve(websiteDir, '..');

function git(...args) {
    return execFileSync('git', args, {cwd: repoDir, encoding: 'utf-8'});
}

const releaseTags = git('tag', '--list')
    .split('\n')
    .filter((tag) => /^\d+\.\d+\.\d+$/.test(tag))
    .map((tag) => ({tag, parts: tag.split('.').map(Number)}));

const latestTagByMajor = new Map();
for (const release of releaseTags) {
    const [major] = release.parts;
    if (major < MIN_MAJOR) {
        continue;
    }
    const current = latestTagByMajor.get(major);
    const isNewer = !current
        || release.parts[1] > current.parts[1]
        || (release.parts[1] === current.parts[1] && release.parts[2] > current.parts[2]);
    if (isNewer) {
        latestTagByMajor.set(major, release);
    }
}

const majors = [...latestTagByMajor.keys()].sort((a, b) => b - a);
if (majors.length === 0) {
    console.error(`No release tags with major >= ${MIN_MAJOR} found; are tags fetched?`);
    process.exit(1);
}

const versionedDocsDir = path.join(websiteDir, 'versioned_docs');
const versionedSidebarsDir = path.join(websiteDir, 'versioned_sidebars');
fs.rmSync(versionedDocsDir, {recursive: true, force: true});
fs.rmSync(versionedSidebarsDir, {recursive: true, force: true});
fs.mkdirSync(versionedSidebarsDir, {recursive: true});

function extractDocs(tag, destination) {
    fs.mkdirSync(destination, {recursive: true});
    const archive = execFileSync('git', ['archive', tag, 'website/docs'], {
        cwd: repoDir,
        maxBuffer: 256 * 1024 * 1024,
    });
    execFileSync('tar', ['-x', '--strip-components=2', '-C', destination], {input: archive});
}

// The working docs' index.md renders the install snippet with the release
// version fetched from the GitHub API at build time (the github-latest-release
// plugin). A snapshot must not do that — it would advertise the newest release
// in old docs — so bake the snapshot's own release version in as plain text.
function freezeReleaseVersion(indexFile, tag) {
    const original = fs.readFileSync(indexFile, 'utf-8');
    const frozen = original
        .replace(/^import CodeBlock from .*\n/m, '')
        .replace(/^import latestRelease from .*\n/m, '')
        .replaceAll('{latestRelease.tag_name}', tag)
        .replace(
            /<CodeBlock language="kotlin">([\s\S]*?)<\/CodeBlock>/,
            (_, code) => '```kotlin\n' + code.trim() + '\n```',
        );
    if (frozen.includes('latestRelease') || frozen.includes('CodeBlock')) {
        throw new Error(`Failed to freeze the release version in ${indexFile}`);
    }
    fs.writeFileSync(indexFile, frozen);
}

// Versioned sidebars are JSON files with the same shape as the sidebars.js
// export, so evaluate the tag's sidebars.js and serialise it.
async function loadSidebars(tag) {
    const source = git('show', `${tag}:website/sidebars.js`);
    const tempFile = path.join(fs.mkdtempSync(path.join(os.tmpdir(), 'orbit-sidebars-')), 'sidebars.mjs');
    fs.writeFileSync(tempFile, source);
    try {
        return (await import(pathToFileURL(tempFile))).default;
    } finally {
        fs.rmSync(path.dirname(tempFile), {recursive: true, force: true});
    }
}

// Dokka output is only generated for the working tree, so every version links
// to the latest API docs; relabel the category to say so.
function relabelDokka(sidebars) {
    for (const item of Object.values(sidebars).flat()) {
        if (item?.type === 'category' && item.label === 'Dokka') {
            item.label = 'Dokka (latest)';
        }
    }
}

for (const major of majors) {
    const {tag} = latestTagByMajor.get(major);
    const label = `${major}.x`;
    const docsDir = path.join(versionedDocsDir, `version-${label}`);

    extractDocs(tag, docsDir);
    freezeReleaseVersion(path.join(docsDir, 'index.md'), tag);

    const sidebars = await loadSidebars(tag);
    relabelDokka(sidebars);
    fs.writeFileSync(
        path.join(versionedSidebarsDir, `version-${label}-sidebars.json`),
        JSON.stringify(sidebars, null, 2) + '\n',
    );

    console.log(`Generated docs version ${label} from tag ${tag}`);
}

fs.writeFileSync(
    path.join(websiteDir, 'versions.json'),
    JSON.stringify(majors.map((major) => `${major}.x`), null, 2) + '\n',
);
