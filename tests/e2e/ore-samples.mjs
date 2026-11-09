const { scenario, command, camera, screenshot } = await import(process.env.HARNESS_E2E_LIB);

// One sample per indicator shape, in IndicatorModels.Shape order.
const SHAPES = [
  'native_platinum', 'native_iridium', 'mithril_matrix', 'uraninite', 'stibnite',
  'wolframite', 'galena', 'fluorite', 'halite', 'quartz',
  'amethyst', 'emerald', 'diamond', 'ruby', 'bauxite',
  'sulfur', 'gypsum', 'bituminous_coal', 'lignite',
];

await scenario(async ({ clients }) => {
  const client = clients[0];
  command(client, 'gamemode creative');
  for (const type of ['item_frame', 'armor_stand', 'item']) {
    command(client, `execute if entity @e[type=minecraft:${type}] run kill @e[type=minecraft:${type}]`);
  }
  command(client, 'fill -12 1 -12 12 12 8 minecraft:air');
  command(client, 'time set day');
  command(client, 'fill -6 0 -3 6 0 6 minecraft:smooth_stone');
  SHAPES.forEach((ore, index) => {
    command(client, `setblock ${(index % 5) * 2 - 4} 1 ${Math.floor(index / 5) * 2} tfcm:ore/small_${ore}`);
  });
  await new Promise(resolve => setTimeout(resolve, 12000));
  await camera(client, { x: 0, y: 3, z: -2.5, yaw: 0, pitch: 40 });
  await new Promise(resolve => setTimeout(resolve, 5000));
  await camera(client, { x: 0, y: 3, z: -2.5, yaw: 0, pitch: 40 });
  screenshot(client, 'AC-F009-003', 'indicator-shapes-overview');
  await camera(client, { x: -3.5, y: 1.4, z: -0.6, yaw: 0, pitch: 45 });
  screenshot(client, 'AC-F009-003', 'native-platinum-groundcover-closeup');
  await camera(client, { x: 3.5, y: 1.4, z: 1.4, yaw: 0, pitch: 45 });
  screenshot(client, 'AC-F009-003', 'fluorite-groundcover-closeup');
  await camera(client, { x: -1.5, y: 1.6, z: 3.4, yaw: 0, pitch: 45 });
  screenshot(client, 'AC-F009-003', 'amethyst-groundcover-closeup');
});
