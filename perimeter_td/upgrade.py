import defense as d
f=d.files
def key(n):return 'data/relay/functions/'+n+'.mcfunction'
def append(n,*lines):f[key(n)]+='\n'.join(lines)+'\n'
def prepend(n,*lines):f[key(n)]='\n'.join(lines)+'\n'+f[key(n)]
def install():
    # Build on top of the local terrain. Fixed entrances on four sides.
    d.fn('starter_base','fill ~-7 ~-1 ~-7 ~7 ~-1 ~7 stone_bricks',
         'fill ~-6 ~ ~-6 ~6 ~3 ~6 air',
         *[f'fill {a} {b} cobblestone' for a,b in [('~-6 ~ ~-6','~6 ~2 ~-6'),('~-6 ~ ~6','~6 ~2 ~6'),('~-6 ~ ~-5','~-6 ~2 ~5'),('~6 ~ ~-5','~6 ~2 ~5')]],
         *[f'fill {a} {b} air' for a,b in [('~-1 ~ ~-6','~1 ~1 ~-6'),('~-1 ~ ~6','~1 ~1 ~6'),('~-6 ~ ~-1','~-6 ~1 ~1'),('~6 ~ ~-1','~6 ~1 ~1')]],
         'setblock ~-3 ~ ~3 crafting_table','setblock ~-2 ~ ~3 furnace','setblock ~3 ~ ~3 lantern','setworldspawn ~ ~ ~')
    prepend('deploy','function relay:starter_base')
    # Each new player receives the kit once, including later co-op arrivals.
    f[key('deploy')]='\n'.join(line for line in f[key('deploy')].splitlines() if not line.startswith('give @s'))+'\n'
    d.fn('arrival','tp @s ~ ~ ~2','spawnpoint @s ~ ~ ~2','give @s stone_sword','give @s bow','give @s arrow 40','give @s shield','give @s leather_chestplate','give @s bread 12','give @s stone_pickaxe','tag @s add rd.joined',d.say('Оператор: осмотрись и экипируйся. /trigger radio — волны, укрепления и производство.'))
    prepend('second', 'execute in minecraft:overworld unless score #built rd matches 1 as @a[limit=1,sort=arbitrary,gamemode=!spectator] at @s if dimension minecraft:overworld positioned over motion_blocking_no_leaves align xyz positioned ~0.5 ~ ~0.5 run function relay:deploy',
            'execute in minecraft:overworld at @e[tag=rd.core,limit=1] as @a[tag=!rd.joined,gamemode=!spectator] run function relay:arrival')
    for n in ['win','lose']:prepend(n,'perimeter_restore')
    f[key('wave')]=f[key('wave')].replace('matches 1.. positioned ~-14','matches 2.. positioned ~-14').replace('matches 1.. positioned ~1 ~ ~-16','matches 3.. positioned ~1 ~ ~-16')
    # Add reinforcements every 20 seconds in later waves; casualties must be cleared before timeout.
    append('wave','scoreboard players operation #tier rd = #wave rd','scoreboard players set #five rd 5','scoreboard players operation #tier rd /= #five rd','execute if score #tier rd matches 17.. run scoreboard players set #tier rd 16','function relay:reinforce_start')
    d.fn('reinforce_start','scoreboard players operation #batch rd = #tier rd','execute if score #batch rd matches 1.. run function relay:reinforce')
    d.fn('reinforce','execute positioned ~-17 ~ ~5 positioned over motion_blocking_no_leaves run summon spore:inf_human ~ ~ ~ {Tags:["rd.mob"],PersistenceRequired:1b}','scoreboard players remove #batch rd 1','execute if score #batch rd matches 1.. run function relay:reinforce')
    prepend('battle','execute if score #wave rd matches 5.. if score #time rd matches 20 run function relay:reinforce_start','execute if score #wave rd matches 10.. if score #time rd matches 40 run function relay:reinforce_start')
    # Visible factory plots. Each bought once, upgrades increase passive income.
    prepend('mine','execute if score #mine rd matches 0 positioned ~-10 ~ ~2 run function relay:mine_shed')
    d.fn('mine_shed','fill ~-1 ~-1 ~-1 ~1 ~-1 ~1 cobblestone','setblock ~ ~ ~ blast_furnace','setblock ~-1 ~ ~ cobblestone_wall','setblock ~1 ~ ~ cobblestone_wall','fill ~-1 ~2 ~ ~1 ~2 ~ stone_slab')
    d.fn('sawmill','execute unless score #saw rd matches 1.. positioned ~10 ~ ~2 run function relay:saw_shed','scoreboard players remove #cash rd 12','scoreboard players add #saw rd 1')
    d.fn('saw_shed','fill ~-1 ~-1 ~-1 ~1 ~-1 ~1 spruce_planks','setblock ~ ~ ~ stonecutter','setblock ~1 ~ ~ oak_log','setblock ~-1 ~ ~ oak_log')
    append('produce','scoreboard players operation #wood rd += #saw rd','execute if score #wood rd matches 129.. run scoreboard players set #wood rd 128')
    d.fn('logs','give @s oak_log','scoreboard players remove #wood rd 1','execute if score #wood rd matches 1.. run function relay:logs')
    append('deploy',d.score('saw',0),d.score('wood',0))
    prepend('input',f'execute if score @s radio matches 30 if entity @e[tag=rd.core,distance=..8] if score #cash rd matches 12.. unless score #saw rd matches 4.. at {d.core} run function relay:sawmill','execute if score @s radio matches 31 if entity @e[tag=rd.core,distance=..8] if score #wood rd matches 1.. run function relay:logs')
    import json
    append('menu','tellraw @s '+json.dumps(['',d.label(30,'Лесопилка I–IV • 12'),d.label(31,'Забрать брёвна'),{'text':' На складе: '},{'score':{'name':'#wood','objective':'rd'}}],ensure_ascii=False))
    prepend('gun','execute if score #gun rd matches 0 run setblock ~3 ~ ~-3 dispenser')
    prepend('walls','execute if score #max rd matches 30 run fill ~-6 ~3 ~-6 ~6 ~3 ~-6 stone_bricks','execute if score #max rd matches 40 run fill ~-6 ~3 ~6 ~6 ~3 ~6 stone_bricks','execute if score #max rd matches 50 run fill ~6 ~3 ~-5 ~6 ~3 ~5 stone_bricks')
