// @ts-check

// This runs in Node.js - Don't use client-side code here (browser APIs, JSX...)

/**
 * Creating a sidebar enables you to:
 - create an ordered group of docs
 - render a sidebar for each doc of that group
 - provide next/previous navigation

 The sidebars can be generated from the filesystem, or explicitly defined here.

 Create as many sidebars as you want.

 @type {import('@docusaurus/plugin-content-docs').SidebarsConfig}
 */
const sidebars = {
  defaultSidebar: [
    'index',
    {
      type: 'category',
      label: 'Core',
      link: {type: 'doc', id: 'Core/index'},
      items: ['Core/architecture'],
    },
    'ViewModel/index',
    'Compose/index',
    'Test/index',
    {
      type: 'category',
      label: 'Dokka',
      items: [
        {
          type: 'link',
          label: 'orbit-core',
          href: 'pathname:///dokka/orbit-core',
        },
        {
          type: 'link',
          label: 'orbit-viewmodel',
          href: 'pathname:///dokka/orbit-viewmodel',
        },
        {
          type: 'link',
          label: 'orbit-compose',
          href: 'pathname:///dokka/orbit-compose',
        },
        {
          type: 'link',
          label: 'orbit-test',
          href: 'pathname:///dokka/orbit-test',
        },
      ],
    },
    'resources',
  ]
};

export default sidebars;
