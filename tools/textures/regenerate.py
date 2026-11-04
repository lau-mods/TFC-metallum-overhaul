#!/usr/bin/env python3
"""Rebuild all metal and processing textures from named reference PNGs.

Requires Python 3.10+ and Pillow. Default stages in .tmp/texture-L/generated.
--apply installs the complete verified result; --check compares it to resources.
Metal palettes use the accepted L method. Processing retains mineral-only colors.
No downloads, archives, hash tables, Gradle or game startup are involved.
"""
import argparse
import csv
from pathlib import Path
import shutil

from PIL import Image
from palette_transfer import lab, load, normalize, palette, pixels, recolor

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[1]
ASSETS = ROOT/'src/main/resources/assets'
REFERENCE = HERE/'reference'


# Material choices describe appearance, never gameplay tiers.
METAL_BASES = {'compressed_iron': 'wrought_iron',
 'platinum': 'silver',
 'naquadah': 'black_steel',
 'iridium': 'silver',
 'osmium': 'wrought_iron',
 'osmiridium': 'sterling_silver',
 'mithril': 'silver',
 'arcane': 'sterling_silver',
 'refined_glowstone': 'gold',
 'refined_obsidian': 'black_steel',
 'antimony': 'tin',
 'titanium': 'steel',
 'cobalt': 'nickel',
 'lithium': 'zinc',
 'aluminum': 'zinc',
 'constantan': 'brass',
 'invar': 'wrought_iron',
 'electrum': 'gold',
 'lead': 'bismuth',
 'uranium': 'nickel',
 'tungsten': 'steel',
 'solder': 'tin',
 'tungsten_steel': 'black_steel',
 'high_carbon_tungsten_steel': 'high_carbon_black_steel',
 'netherite': 'black_steel',
 'dawnstone': 'brass',
 'andesite_alloy': 'wrought_iron',
 'draconium': 'black_steel',
 'awakened_draconium': 'red_steel',
 'boron': 'steel',
 'thorium': 'cast_iron',
 'magnesium': 'tin',
 'beryllium': 'tin',
 'zirconium': 'wrought_iron',
 'chromium': 'steel',
 'stainless_steel': 'steel',
 'bismuth_bronze': 'bismuth_bronze',
 'tin': 'tin',
 'black_steel': 'black_steel',
 'black_bronze': 'black_bronze',
 'bronze': 'bronze',
 'bismuth': 'bismuth',
 'nickel': 'nickel',
 'wrought_iron': 'wrought_iron',
 'cast_iron': 'cast_iron',
 'brass': 'brass',
 'copper': 'copper',
 'silver': 'silver',
 'sterling_silver': 'sterling_silver',
 'red_steel': 'red_steel',
 'rose_gold': 'rose_gold',
 'blue_steel': 'blue_steel',
 'zinc': 'zinc',
 'steel': 'steel',
 'gold': 'gold'}
TFC_METALS = set("bismuth bismuth_bronze black_bronze black_steel blue_steel brass bronze cast_iron copper gold nickel red_steel rose_gold silver steel sterling_silver tin wrought_iron zinc".split())
ARMOR_BASES = dict(invar='wrought_iron', titanium='black_steel', tungsten_steel='steel', netherite='black_bronze')
PROJECTILE_BASES = dict(invar=('wrought_iron','steel'), titanium=('steel','wrought_iron'), tungsten_steel=('red_steel','blue_steel'), netherite=('red_steel','blue_steel'))
# Geometry follows the established equipment design, independently of METAL_BASES
# (the material chosen for lightness). Red Steel tool silhouettes must not become
# Black Steel silhouettes when the palette's lightness reference changes.
TOOL_SHAPES = PROJECTILE_BASES
STANDARD = {'ingot','double_ingot','sheet','double_sheet','rod'}
EXTENDED = {'foil','gear','heavy_sheet','nail','ring','rivet','screw','stamen','wire'}
ARMOR = {'helmet','chestplate','greaves','boots','unfinished_helmet','unfinished_chestplate','unfinished_greaves','unfinished_boots','horse_armor'}
FIXED_TOOLS = {'fishing_rod','pickaxe','propick','axe','shovel','hoe','chisel','hammer','saw','javelin','sword','mace','knife','scythe','shears'}
TOOL_METALS = 'copper bismuth_bronze black_bronze bronze wrought_iron steel black_steel blue_steel red_steel'.split()
AREA = dict(mining_hammer='hammer',quarry_hammer='hammer',excavator='shovel')
PRESERVED_TFC_FORMS = {'pommel','crossguard',*AREA,*(name+'_head' for name in AREA)}
# TFC's ranges wash out this near-black material, even with Cast Iron.
# Preserve its original dark values while retaining Black Steel's geometry.
SOURCE_RANGE = {'naquadah'}


def build_recipes(image):
    """Derive recipes from form rules and actual named PNGs, not a per-file table."""
    recipes = []
    used = set()
    def ref(path):
        assert (REFERENCE/path).is_file(), path
        used.add(path)
        return path
    def tfc(form, metal):
        return ref(f'tfc/{form}/{metal}.png')
    def original(metal):
        return tfc('ingot',metal) if metal in TFC_METALS else ref(f'original/{metal}.png')
    def project(path):
        return ref('project/'+str(path.relative_to(ASSETS)))
    def fixed(shape, comparisons):
        a = pixels(image(shape))
        others = [pixels(image(p)) for p in comparisons]
        assert all(len(p)==len(a) for p in others)
        return [i for i,p in enumerate(a) if p[3] and all(v[i]==p for v in others)]
    def tool_fixed(form, shape, comparisons):
        result=set(fixed(shape,comparisons))
        a=pixels(image(shape));others=[pixels(image(p)) for p in comparisons]
        def brown(p):
            return p[3] and p[0]-p[1]>=8 and p[1]-p[2]>=5
        # Knife/shears grip shading differs by a few RGB steps across original
        # TFC metals. Exact color equality alone would recolor those grip pixels.
        result.update(i for i,p in enumerate(a) if brown(p) and all(
            brown(q[i]) and max(abs(x-y) for x,y in zip(p,q[i]))<=5 for q in others))
        if form=='shears':
            # Shears share the same grip outline across metals. Use the neutral
            # Wrought Iron original to identify the whole brown grip, including
            # blended boundary pixels, then keep this shape's actual TFC colors.
            grip=pixels(image(tfc('shears','wrought_iron')))
            result.update(i for i,p in enumerate(grip) if brown(p))
        return sorted(result)
    def other(base, form=None):
        # Red/Blue Steel fishing rods have a black grip; other tiers have wood.
        # Compare the same grip type when identifying the fixed, nonmetal pixels.
        if base=='black_steel' and form=='fishing_rod':
            return 'steel'
        return 'red_steel' if base=='black_steel' else 'steel' if base=='wrought_iron' else 'wrought_iron'
    def armor_fixed(shape):
        # These original TFC styles have brown leather against cool metal.
        # Restrict material detection to those styles, before palette transfer:
        # brown pixels in a warm-colored metal are not automatically leather.
        path=Path(shape);base=path.stem.split('_layer_')[0]
        leather=(base in ('steel','black_steel') and
                 path.parent.name in ('chestplate','greaves','boots','armor'))
        # Black Bronze boots also have cream bindings and dark brown footwear.
        bindings=base=='black_bronze' and path.parent.name=='boots'
        return [i for i,(r,g,b,a) in enumerate(pixels(image(shape)))
                if a and ((leather and r>g>b) or (bindings and r>=g>b))]
    def add(path, shape, palette_source=None, tone=None, **options):
        size=Image.open(path).size if path.exists() else image(shape).size
        recipe = dict(output=str(path.relative_to(ASSETS)), shape=shape, size=list(size), **options)
        if palette_source:
            recipe['palette'] = palette_source
        if tone and Path(path).stem not in SOURCE_RANGE:
            recipe['tone'] = tone
        recipes.append(recipe)
    for path in sorted((ASSETS/'tfcm/textures/item/metal').glob('*/*.png')):
        form,metal = path.parent.name,path.stem
        suffix = ''
        if form=='shield' and metal.endswith(('_front','_back')):
            metal,suffix=metal.rsplit('_',1);suffix='_'+suffix
        base=METAL_BASES[metal];options={};tone=None;colors=original(metal)
        if form in STANDARD:
            shape=tone=tfc(form,base)
        elif form in EXTENDED:
            shape=ref(f'moreitems/{form}/{"stainless_steel" if metal=="stainless_steel" else base}.png')
            tone=ref(f'moreitems/{form}/{base}.png')
        elif form in ('plate','dust','nugget'):
            shape=project(path if form=='nugget' else ASSETS/f'tfcm/textures/item/metal/{form}/invar.png')
            tone=tfc('sheet' if form=='plate' else 'ingot',base)
        elif form.removesuffix('_head') in AREA:
            shape=project(path)
            if metal in TFC_METALS:
                add(path,shape)  # Keep the existing TFC-metal tools/heads byte-identical.
                continue
            tool=AREA[form.removesuffix('_head')]
            tone=tfc(tool+('_head' if form.endswith('_head') else ''),base)
            if not form.endswith('_head'):
                # The authored area-tool handles use the TFC Hammer's wood.
                # Some silhouettes omit its brightest shade entirely, so an
                # intersection across all area tools loses valid wooden pixels.
                wood=set(pixels(image(tfc('hammer','wrought_iron')))) & set(pixels(image(tfc('hammer','steel'))))
                options['fixed']=[i for i,p in enumerate(pixels(image(shape))) if p[3] and p in wood]
                options['tone_fixed']=fixed(tone,[tfc(tool,other(base))])
        elif form in ('crossguard','pommel'):
            shape_base=TOOL_SHAPES[metal][0] if metal in TOOL_SHAPES else base
            shape=ref(f'metaltools/{form}/{shape_base}.png')
            tone=ref(f'metaltools/{form}/{base}.png')
            if metal in TFC_METALS:
                colors=tone=None  # Native TFC Metal Tools PNG is copied byte for byte.
        elif form in ('tongs','tong_part'):
            shape_base,comparison=TOOL_SHAPES[metal]
            shape=ref(f'hotornot/{form}/{shape_base}.png')
            tone=ref(f'hotornot/{form}/{base}.png')
            options['fixed']=fixed(shape,[ref(f'hotornot/{form}/{comparison}.png')])
            options['tone_fixed']=fixed(tone,[ref(f'hotornot/{form}/{other(base)}.png')])
        elif form=='shield':
            shape=tfc(form,ARMOR_BASES[metal]+suffix)
            tone=tfc(form,base) if base=='wrought_iron' else [tfc(form,base+'_front'),tfc(form,base+'_back')] if not suffix else tfc(form,base+suffix)
        elif form in ARMOR:
            shape=tfc(form,ARMOR_BASES[metal]);tone=tfc(form,base)
            if form=='horse_armor':
                comparisons=[tfc(form,m) for m in TOOL_METALS]
                options['fixed']=fixed(shape,comparisons);options['tone_fixed']=fixed(tone,comparisons)
            else:
                options['fixed']=armor_fixed(shape);options['tone_fixed']=armor_fixed(tone)
        else:
            shape_base,comparison=TOOL_SHAPES[metal]
            shape=tfc(form,shape_base);tone=tfc(form,base)
            if form in FIXED_TOOLS:
                options['fixed']=tool_fixed(form,shape,[tfc(form,comparison)])
                options['tone_fixed']=tool_fixed(form,tone,[tfc(form,other(base,form))])
        add(path,shape,colors,tone,**options)
    for namespace,form in [('tfcm','block'),('tfcm','smooth'),('tfc','smooth')]:
        for path in sorted((ASSETS/f'{namespace}/textures/block/metal/{form}').glob('*.png')):
            shape=tfc(form,METAL_BASES[path.stem]);add(path,shape,original(path.stem),shape)
    for path in sorted((ASSETS/'tfcm/textures/models/armor').glob('*.png')):
        metal,layer=path.stem.split('_layer_')
        shape=ref(f'tfc/armor/{ARMOR_BASES[metal]}_layer_{layer}.png')
        tone=ref(f'tfc/armor/{METAL_BASES[metal]}_layer_{layer}.png')
        add(path,shape,original(metal),tone,fixed=armor_fixed(shape),tone_fixed=armor_fixed(tone))
    # AnimalArmorItem resolves this entity texture from the armor material ID;
    # an inventory horse-armor icon alone does not cover the equipped horse.
    horse_sources=[tfc('horse_armor_entity',m) for m in sorted(set(ARMOR_BASES.values()))]
    # All four styles share the cloth UVs, but use different cloth colors.
    # Black Steel's red cloth distinguishes those pixels from its gray metal;
    # keep each source style's actual cloth color at the corresponding UVs.
    horse_cloth={i for i,(r,g,b,a) in enumerate(pixels(image(tfc('horse_armor_entity','black_steel'))))
                 if a and r>g and r>b}
    def horse_fixed(shape):
        return sorted(set(fixed(shape,horse_sources)) | horse_cloth)
    for metal,base in ARMOR_BASES.items():
        path=ASSETS/f'tfcm/textures/entity/horse/armor/horse_armor_{metal}.png'
        shape=tfc('horse_armor_entity',base);tone=tfc('horse_armor_entity',METAL_BASES[metal])
        add(path,shape,original(metal),tone,
            fixed=horse_fixed(shape),tone_fixed=horse_fixed(tone))
    for path in sorted((ASSETS/'tfcm/textures/entity/projectiles').glob('*.png')):
        metal=path.stem.removesuffix('_javelin');old,comparison=PROJECTILE_BASES[metal];base=METAL_BASES[metal]
        def projectile(name):return ref(f'tfc/projectiles/{name}_javelin.png')
        shape=projectile(old);tone=projectile(base)
        add(path,shape,original(metal),tone,fixed=fixed(shape,[projectile(comparison)]),tone_fixed=fixed(tone,[projectile(other(base))]))
    ores={};processing=ASSETS/'tfcm/textures/item/ore_processing'
    for path in sorted((processing/'chunks').glob('*.png')):
        name=path.stem
        for group in ('tfc','firmalife','tfcm'):
            found=next((f'{group}/ore/{stem}.png' for stem in ('normal_'+name,name) if (REFERENCE/f'{group}/ore/{stem}.png').is_file()),None)
            if found:
                ores[name]=ref(found);break
        assert name in ores,name
    for path in sorted(processing.rglob('*.png')):
        form,name=path.parent.name,path.stem;options={}
        if name in ('rock_overlay','rock_powder'):
            add(path,project(path));continue
        if form=='sand_pile':
            shape=ref(f'washing/sand_pile/{name}.png');colors=ref(f'tfc/sand/{name}.png')
        else:
            colors=ores[name]
            shape=ref('tfc/powder/graphite.png') if form=='powder' else ref(f'washing/{form}_{"chromium" if form in ("pellet","briquet") else "graphite"}.png')
            if form=='rocky_chunks':
                overlay=image(project(processing/'rocky_chunks/rock_overlay.png'))
                options['fixed']=[i for i,p in enumerate(pixels(overlay)) if p[3]]
        add(path,shape,colors,**options)
    return recipes,ores,used


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    modes = parser.add_mutually_exclusive_group()
    modes.add_argument('--apply', action='store_true')
    modes.add_argument('--check', action='store_true')
    parser.add_argument('--output', type=Path, default=ROOT/'.tmp/texture-L/generated')
    args = parser.parse_args()
    images = {}
    def image(name):
        if name not in images:
            images[name] = load(REFERENCE/name)
        return images[name]
    recipes,ore_palettes,used_references = build_recipes(image)
    outputs = set()
    differences = []
    records = []
    cache = {}
    for r in recipes:
        relative = r['output']
        assert relative not in outputs, relative
        outputs.add(relative)
        shape = image(r['shape'])
        fixed = r.get('fixed', [])
        if 'palette' not in r:
            result = shape.copy()
            records.append([relative,0,'copy',len(palette(shape)),len(palette(shape)),len(fixed)])
        else:
            source = image(r['palette'])
            assert source.height == source.width, relative
            if '/ore_processing/' in relative and Path(relative).stem in ore_palettes:
                assert r['palette'] == ore_palettes[Path(relative).stem]
                assert '/ore/' in r['palette'] and 'tone' not in r
                assert source.getchannel('A').getextrema()[0] == 0
            correction = None
            if 'tone' in r:
                names = [r['tone']] if isinstance(r['tone'],str) else r['tone']
                key = (r['palette'],tuple(names),tuple(r.get('tone_fixed',[])))
                if key not in cache:
                    original = palette(source)
                    target = sorted({c for name in names for c in palette(image(name),r.get('tone_fixed',[]))},
                                    key=lambda c:(.2126*c[0]+.7152*c[1]+.0722*c[2],c))
                    corrected = normalize(original,target)
                    lo,hi = min(lab(c)[0] for c in target),max(lab(c)[0] for c in target)
                    # Account for half an sRGB code value per channel. A fixed
                    # OKLab tolerance is too small near black and too large near white.
                    assert all(lab(tuple(max(0,v-.501) for v in c))[0] <= hi+1e-6
                               and lab(tuple(min(255,v+.501) for v in c))[0] >= lo-1e-6
                               for c in corrected),relative
                    cache[key] = dict(zip(original,corrected))
                correction = cache[key]
            colors = palette(source)
            if correction:
                colors = [correction[c] for c in colors]
            result = recolor(shape,colors,fixed)
            records.append([relative,0,'L' if correction else 'source palette',
                            len(colors),len(palette(result,fixed)),len(fixed)])
        assert list(result.size) == r['size'],(relative,result.size,r['size'])
        target = args.output/relative
        target.parent.mkdir(parents=True,exist_ok=True)
        if 'palette' not in r:
            shutil.copyfile(REFERENCE/r['shape'],target)
        else:
            result.save(target,optimize=True)
        current = load(ASSETS/relative) if (ASSETS/relative).exists() else None
        if current is None or current.size != result.size or current.tobytes() != result.tobytes():
            differences.append(relative)
    roots = ['tfcm/textures/item/metal','tfcm/textures/block/metal','tfc/textures/block/metal/smooth',
             'tfcm/textures/models/armor','tfcm/textures/entity/projectiles','tfcm/textures/item/ore_processing',
             'tfcm/textures/entity/horse/armor']
    expected = {str(p.relative_to(ASSETS)) for directory in roots for p in (ASSETS/directory).rglob('*.png')}
    expected.update(f'tfcm/textures/entity/horse/armor/horse_armor_{m}.png' for m in ARMOR_BASES)
    assert outputs == expected,{'missing':sorted(expected-outputs),'extra':sorted(outputs-expected)}
    assert len(ore_palettes) == 35
    args.output.mkdir(parents=True,exist_ok=True)
    with (args.output/'color-counts.csv').open('w',newline='') as f:
        writer = csv.writer(f)
        writer.writerow(['texture','frame','method','palette_colors','output_metal_colors','fixed_pixels'])
        writer.writerows(records)
    for r in recipes:
        if Path(r['output']).parent.name in PRESERVED_TFC_FORMS and Path(r['output']).stem in TFC_METALS:
            assert (args.output/r['output']).read_bytes()==(REFERENCE/r['shape']).read_bytes()
            if args.check:
                assert (ASSETS/r['output']).read_bytes()==(REFERENCE/r['shape']).read_bytes()
    if args.check and differences:
        raise SystemExit(f'{len(differences)} textures differ: {differences[:5]}')
    if args.apply:
        for relative in sorted(outputs):
            (ASSETS/relative).parent.mkdir(parents=True,exist_ok=True)
            shutil.copyfile(args.output/relative,ASSETS/relative)
    print(f'{len(outputs)} textures; {len(differences)} pixel changes; '
          f'{"applied" if args.apply else "checked" if args.check else "staged"}.')


if __name__ == '__main__':
    main()
