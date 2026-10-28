const { scenario, command, camera, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

await scenario(async ({ clients }) => {
  const client = clients[0];
  command(client, 'gamemode creative');
  command(client, 'gamerule sendCommandFeedback false');
  command(client, 'time set day');
  command(client, 'weather clear');
  command(client, 'fill -7 0 -10 7 0 5 minecraft:smooth_stone');
  command(client, 'fill -5 1 5 5 8 5 minecraft:smooth_stone');
  const metals = ['invar', 'titanium', 'netherite', 'tungsten_steel'];
  const displayedParts = ['pickaxe_head', 'pickaxe', 'sword', 'shears', 'tuyere', 'shield', 'horse_armor'];
  for (let index = 0; index < metals.length; index++) {
    const x = -3 + index * 2;
    command(client, `setblock ${x} 1 -4 tfcm:metal/anvil/${metals[index]}[facing=north]`);
    for (let row = 0; row < displayedParts.length; row++) {
      command(client, `summon minecraft:item_frame ${x} ${8 - row} 4.49 {Facing:2b,Fixed:1b,Item:{id:"tfcm:metal/${displayedParts[row]}/${metals[index]}",count:1}}`);
    }
    command(client, `summon minecraft:armor_stand ${x} 1 2.5 {NoGravity:1b,ShowArms:1b,Rotation:[180f],ArmorItems:[{id:"tfcm:metal/boots/${metals[index]}",count:1},{id:"tfcm:metal/greaves/${metals[index]}",count:1},{id:"tfcm:metal/chestplate/${metals[index]}",count:1},{id:"tfcm:metal/helmet/${metals[index]}",count:1}],HandItems:[{id:"tfcm:metal/sword/${metals[index]}",count:1},{}]}`);
  }
  await new Promise(resolve => setTimeout(resolve, 12000));
  await camera(client, { x: 0.5, y: 4.4, z: -2.5, yaw: 0, pitch: 0 });
  await new Promise(resolve => setTimeout(resolve, 3000));
  const status = mct(client, 'status', 'health');
  if (status.health <= 0) throw new Error('Player died before the equipment and anvil screenshot');
  screenshot(client, 'AC-F007-001', 'four-metal-equipment');
  await camera(client, { x: 0.5, y: 2.1, z: -9.5, yaw: 0, pitch: 8 });
  await new Promise(resolve => setTimeout(resolve, 3000));
  screenshot(client, 'AC-F008-001', 'four-metal-anvils');
  await camera(client, { x: 1.5, y: 2.1, z: -6.5, yaw: 0, pitch: 20 });
  mct(client, 'block', 'interact', '1', '1', '-4');
  mct(client, 'gui', 'wait-open');
  screenshot(client, 'AC-F008-001', 'tfc-anvil-ui');
  mct(client, 'gui', 'close');
});
