const { scenario, command, camera, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

const parts = [
  "leather_sheet",
  "leather_strip",
  "leather_strap",
  "leather_string",
  "plant_fiber",
  "plant_string",
  "plant_fabric",
  "plant_clothing",
  "bamboo_fiber",
  "bamboo_string",
  "bow_limb",
  "bow_chord",
  "arrow_head",
  "arrow_tread",
  "arrow_fletch",
  "arrow_shaft",
  "short_wood_handle",
  "saw_wood_handle",
  "long_stick",
  "metal/crossguard/invar",
  "metal/pommel/invar",
  "metal/rivet/invar",
  "metal/ring/invar",
  "metal/wire/invar"
];

await scenario(async ({ clients }) => {
  const client = clients[0];
  command(client, 'gamemode creative');
  command(client, 'gamerule randomTickSpeed 0');
  command(client, 'fill -8 0 -12 8 0 5 minecraft:smooth_stone');
  command(client, 'fill -7 0 4 7 6 4 minecraft:smooth_stone');
  command(client, 'setblock -7 1 3 tfcm:plant_block');
  for (let index = 0; index < parts.length; index++) {
    const x = -4 + (index % 8);
    const y = 5 - Math.floor(index / 8) * 2;
    command(client, `summon minecraft:item_frame ${x} ${y} 3.49 {Facing:2b,Fixed:1b,Item:{id:"tfcm:${parts[index]}",count:1}}`);
  }
  await camera(client, { x: 0.5, y: 3.4, z: -10.5, yaw: 0, pitch: 5 });
  const status = mct(client, 'status', 'health');
  if (status.health <= 0) throw new Error('Player died before the material-parts screenshot');
  screenshot(client, 'AC-F006-001', 'registered-material-parts');
});
