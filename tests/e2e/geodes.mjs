const { scenario, command, camera, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

await scenario(async ({ clients }) => {
  const client = clients[0];
  command(client, 'gamemode creative');
  command(client, 'gamerule sendCommandFeedback false');
  command(client, 'time set clear');
  command(client, 'fill -9 -60 -12 9 -60 3 minecraft:smooth_stone');
  command(client, 'fill -9 -59 1 9 -50 1 tfc:rock/raw/quartzite');
  const minerals = ['quartz', 'amethyst', 'certus_quartz'];
  for (let row = 0; row < minerals.length; row++) {
    const mineral = minerals[row];
    const y = -58 + row * 3;
    const label = mineral.split('_').map(word => word[0].toUpperCase() + word.slice(1)).join(' ');
    command(client, `setblock -8 ${y} 0 minecraft:oak_wall_sign[facing=north]{front_text:{messages:['{"text":"${label}"}','""','""','""']}}`);
    command(client, `setblock -5 ${y} 0 tfcm:mineral/${mineral}_block`);
    command(client, `setblock -3 ${y} 0 tfcm:mineral/budding_${mineral}`);
    command(client, `setblock -1 ${y} 0 tfcm:mineral/small_${mineral}_bud[facing=north]`);
    command(client, `setblock 1 ${y} 0 tfcm:mineral/medium_${mineral}_bud[facing=north]`);
    command(client, `setblock 3 ${y} 0 tfcm:mineral/large_${mineral}_bud[facing=north]`);
    command(client, `setblock 5 ${y} 0 tfcm:mineral/${mineral}_cluster[facing=north]`);
  }
  await new Promise(resolve => setTimeout(resolve, 12000));
  await camera(client, { x: -0.5, y: -57, z: -11.5, yaw: 0, pitch: 0 }, 5000);
  const status = mct(client, 'status', 'health');
  if (status.health <= 0) throw new Error('Player died before the geode screenshot');
  screenshot(client, 'AC-F023-001', 'three-mineral-block-budding-buds-and-clusters');
});
