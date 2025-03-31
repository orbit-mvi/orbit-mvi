// @ts-check
// `@type` JSDoc annotations allow editor autocompletion and type checking
// (when paired with `@ts-check`).
// There are various equivalent ways to declare your Docusaurus config.
// See: https://docusaurus.io/docs/api/docusaurus-config

import {themes as prismThemes} from 'prism-react-renderer';

// This runs in Node.js - Don't use client-side code here (browser APIs, JSX...)

/** @type {import('@docusaurus/types').Config} */
const config = {
    title: 'Orbit',
    tagline: 'Just add MVI',
    favicon: 'img/orbit-favicon.ico',

    // Set the production url of your site here
    url: 'https://orbit-mvi.org',
    // Set the /<baseUrl>/ pathname under which your site is served
    // For GitHub pages deployment, it is often '/<projectName>/'
    baseUrl: '/',

    // GitHub pages deployment config.
    // If you aren't using GitHub pages, you don't need these.
    organizationName: 'orbit-mvi', // Usually your GitHub org/user name.
    projectName: 'orbit-mvi', // Usually your repo name.

    onBrokenLinks: 'throw',
    onBrokenMarkdownLinks: 'warn',

    // Even if you don't use internationalization, you can use this field to set
    // useful metadata like html lang. For example, if your site is Chinese, you
    // may want to replace "en" with "zh-Hans".
    i18n: {
        defaultLocale: 'en',
        locales: ['en'],
    },

    presets: [
        [
            'classic',
            /** @type {import('@docusaurus/preset-classic').Options} */
            ({
                docs: {
                    routeBasePath: '/',
                    sidebarPath: './sidebars.js',
                },
                theme: {
                    customCss: './src/css/custom.css',
                },
            }),
        ],
    ],

    themeConfig:
        /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
        ({
            navbar: {
                title: 'Orbit Multiplatform',
                logo: {
                    alt: 'Orbit Logo',
                    src: 'img/orbit.svg',
                },
                items: [
                    {
                        href: 'https://github.com/orbit-mvi/orbit-mvi',
                        label: 'GitHub',
                        position: 'right',
                    },
                ],
            },
            footer: {
                style: 'dark',
                links: [
                    {
                        title: 'Community',
                        items: [{
                            label: 'Kotlinlang Slack',
                            href: 'https://kotlinlang.slack.com/messages/CPM6UMD2P',
                        },
                        {
                            label: 'Twitter',
                            href: 'https://twitter.com/orbit_mvi',
                        },
                    ],
                },
                {
                    title: 'More',
                    items: [
                        {
                            label: 'GitHub',
                            href: 'https://github.com/orbit-mvi/orbit-mvi',
                        },
                    ],
                },
            ],
            copyright: `Copyright © 2021-${new Date().getFullYear()} Mikołaj Leszczyński & Appmattus Limited`,
        },
      prism: {
        theme: prismThemes.github,
        darkTheme: prismThemes.dracula,
      },
      mermaid: {
          theme: {light: 'neutral', dark: 'dark'},
      },
    }),
    markdown: {
        mermaid: true,
    },
    themes: ['@docusaurus/theme-mermaid'],
    plugins: ['./src/plugins/github-latest-release'],
};

export default config;
