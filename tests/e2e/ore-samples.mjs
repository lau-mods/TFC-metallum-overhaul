const { scenario, command, camera, screenshot } = await import(process.env.HARNESS_E2E_LIB);

await scenario(async ({ clients }) => {
  const client = clients[0];
  command(client, 'gamemode creative');
  command(client, 'time set day');
  command(client, 'weather clear');
  command(client, 'fill -3 0 -3 3 0 2 minecraft:smooth_stone');
  command(client, 'setblock -1 1 0 tfcm:ore/small_native_platinum');
  command(client, 'setblock 1 1 0 tfcm:ore/small_fluorite');
  await new Promise(resolve => setTimeout(resolve, 12000));
  await camera(client, { x: 0.5, y: 1.7, z: -2.5, yaw: 0, pitch: 24 });
  await new Promise(resolve => setTimeout(resolve, 3000));
  screenshot(client, 'AC-F009-003', 'small-ore-and-groundcover');
});
