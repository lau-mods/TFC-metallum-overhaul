const { scenario, command, camera, screenshot } = await import(process.env.HARNESS_E2E_LIB);

await scenario(async ({ clients }) => {
  const client = clients[0];
  command(client, 'time set day');
  command(client, 'weather clear');
  command(client, 'setblock 0 0 2 minecraft:stone');
  await camera(client, { x: 0.5, y: 1.6, z: -2.5, yaw: 0, pitch: 10 });
  screenshot(client, 'AC-F001-001', 'joined-world');
});
