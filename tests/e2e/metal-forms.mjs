const { scenario, command, camera, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

await scenario(async ({ clients }) => {
  const client = clients[0];
  command(client, 'gamemode creative');
  command(client, 'gamerule sendCommandFeedback false');
  command(client, 'time set day');
  command(client, 'weather clear');
  command(client, 'fill -2 0 -5 2 0 0 minecraft:stone');
  await camera(client, { x: 0.5, y: 1, z: -2.5, yaw: 0, pitch: 10 });
  const extended = ['foil', 'gear', 'heavy_sheet', 'nail', 'ring', 'rivet', 'screw', 'stamen', 'wire'];
  for (let slot = 0; slot < extended.length; slot++) {
    command(client, `item replace entity @s hotbar.${slot} with tfcm:metal/${extended[slot]}/titanium`);
  }
  mct(client, 'input', 'key', 'combo', 'f3', 'd');
  await new Promise(resolve => setTimeout(resolve, 12000));
  mct(client, 'input', 'key', 'press', 'e');
  mct(client, 'gui', 'wait-open');
  screenshot(client, 'AC-F004-001', 'extended-forms');
  mct(client, 'gui', 'close');

  const forms = ['plate', 'nugget', 'dust'];
  const metals = ['titanium', 'invar', 'tungsten_steel'];
  for (let slot = 0; slot < 9; slot++) {
    command(client, `item replace entity @s hotbar.${slot} with tfcm:metal/${forms[slot % 3]}/${metals[Math.floor(slot / 3)]}`);
  }
  mct(client, 'input', 'key', 'combo', 'f3', 'd');
  await new Promise(resolve => setTimeout(resolve, 12000));
  mct(client, 'input', 'key', 'press', 'e');
  mct(client, 'gui', 'wait-open');
  screenshot(client, 'AC-F005-004', 'plate-nugget-dust');
});
