module.exports = {
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
