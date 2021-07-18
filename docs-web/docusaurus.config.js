const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');

/** @type {import('@docusaurus/types').DocusaurusConfig} */
module.exports = {
    title: 'Orbit',
    tagline: 'Just add MVI',
    url: 'https://orbit-mvi.org',
    baseUrl: '/',
    onBrokenLinks: 'throw',
    onBrokenMarkdownLinks: 'warn',
    favicon: 'img/orbit-favicon.ico',
    organizationName: 'orbit-mvi', // Usually your GitHub org/user name.
    projectName: 'orbit-mvi', // Usually your repo name.
    themeConfig: {
        navbar: {
            title: 'Orbit MVI',
            logo: {
                alt: 'Orbit Logo',
                src: 'img/orbit.svg',
            },
            items: [{
                type: 'doc',
                docId: 'intro',
                position: 'left',
                label: 'Docs',
            },
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
            copyright: `Copyright © ${new Date().getFullYear()} Orbit MVI. Built with Docusaurus.`,
        },
        prism: {
            theme: lightCodeTheme,
            darkTheme: darkCodeTheme,
            additionalLanguages: ['kotlin'],
        },
    },
    presets: [
        [
            '@docusaurus/preset-classic',
            {
                docs: {
                    routeBasePath: '/',
                    sidebarPath: require.resolve('./sidebars.js'),
                    // Please change this to your repo.
                    editUrl: 'https://github.com/orbit-mvi/orbit-mvi/edit/master/website/',
                },
                theme: {
                    customCss: require.resolve('./src/css/custom.css'),
                },
            },
        ],
    ],
};