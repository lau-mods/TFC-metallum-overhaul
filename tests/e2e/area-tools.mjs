const { scenario, command, camera, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

await scenario(async ({ clients }) => {
  const client = clients[0];
  command(client, 'gamemode creative');
  command(client, 'gamerule sendCommandFeedback false');
  command(client, 'time set day');
  command(client, 'weather clear');
  command(client, 'fill -6 0 -8 6 0 2 minecraft:smooth_stone');
  command(client, 'fill -6 1 2 6 7 2 minecraft:smooth_stone');
  const metals = ['invar', 'titanium', 'netherite', 'tungsten_steel'];
  const tools = ['mining_hammer', 'quarry_hammer', 'excavator'];
  for (let row = 0; row < tools.length; row++) {
    for (let column = 0; column < metals.length; column++) {
      const x = -4.5 + column * 3;
      const y = 6 - row * 2;
      command(client, `summon minecraft:item_frame ${x} ${y} 1.49 {Facing:2b,Fixed:1b,Item:{id:"tfcm:metal/${tools[row]}/${metals[column]}",count:1}}`);
      command(client, `summon minecraft:item_frame ${x + 1} ${y} 1.49 {Facing:2b,Fixed:1b,Item:{id:"tfcm:metal/${tools[row]}_head/${metals[column]}",count:1}}`);
    }
  }
  await new Promise(resolve => setTimeout(resolve, 12000));
  await camera(client, { x: 0.5, y: 4, z: -5.5, yaw: 0, pitch: 0 });
  screenshot(client, 'AC-F017-003', 'mining-hammer-tools-and-heads');
  screenshot(client, 'AC-F018-004', 'quarry-hammer-tools-and-heads');
  screenshot(client, 'AC-F019-002', 'excavator-tools-and-heads');

  command(client, 'kill @e[type=minecraft:item_frame]');
  for (let index = 0; index < tools.length; index++) {
    const x = -3 + index * 3;
    command(client, `summon minecraft:item_frame ${x} 5 1.49 {Facing:2b,Fixed:1b,Item:{id:"tfcm:ceramic/unfired_${tools[index]}_head_mold",count:1}}`);
    command(client, `summon minecraft:item_frame ${x + 1} 5 1.49 {Facing:2b,Fixed:1b,Item:{id:"tfcm:ceramic/${tools[index]}_head_mold",count:1}}`);
    command(client, `summon minecraft:item_frame ${x} 3 1.49 {Facing:2b,Fixed:1b,Item:{id:"tfcm:metal/${tools[index]}_head/copper",count:1}}`);
    command(client, `summon minecraft:item_frame ${x + 1} 3 1.49 {Facing:2b,Fixed:1b,Item:{id:"tfcm:metal/${tools[index]}/copper",count:1}}`);
  }
  await new Promise(resolve => setTimeout(resolve, 3000));
  const status = mct(client, 'status', 'health');
  if (status.health <= 0) throw new Error('Player died before the area tool screenshots');
  screenshot(client, 'AC-F021-001', 'unfired-fired-molds-heads-and-tools');
  screenshot(client, 'AC-F021-003', 'quarry-hammer-mold-head-and-tool');
});
