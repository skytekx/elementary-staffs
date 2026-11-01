# Elementary Staffs for Forge 1.20.1

A Forge 1.20.1 port of Kroko's Elementary Staffs for the SkyTekx3 modpack. The original mod brings
back the staffs of the old Better Dungeons mod: the electric, fire, ice, poison, darkness, water,
wind and healing staffs, their essences, elemental arrows and grenades.

The original is by Kroko (KrokoOnAir) and published under the MIT license on
[Modrinth](https://modrinth.com/mod/elementary-staffs) and CurseForge. Its last 1.20.1 build,
2.0.0, exists only for Fabric. This port starts from that build. The original source is not
public, so this repository is a port rather than a GitHub fork. It is neither affiliated with nor
endorsed by Kroko. Bug reports about this port belong here and not to the original author.

## Credits

- Kroko wrote Elementary Staffs and made its textures and sounds.
- The port to Forge 1.20.1 was made by kleberbaum in November 2026.

The textures, sounds, item models, recipes, loot tables and English texts are those of 2.0.0. The
German texts are new.

## License

The original mod and this port are licensed under the MIT license. The text in
[LICENSE](LICENSE) is the one of 2.0.0, with its blank copyright line kept as it was. The file
[NOTICE](NOTICE) names Kroko as the author and describes the port. Both ship inside the jar as
`LICENSE_elementary_staffs` and `NOTICE_elementary_staffs`.

## Items

The mod id is `elementary_staffs`. Forge does not allow hyphens in mod ids, so the namespace
`elementary-staffs` of 2.0.0 became `elementary_staffs`. The paths of the ids stayed as they were.

| Item | Id | Recipe |
|---|---|---|
| Staff | `staff` | Magic essence, stick and diamond, shapeless |
| Darkness staff | `staff/darkness` | Staff and darkness essence, shapeless |
| Electric staff | `staff/electric` | Staff and electric essence, shapeless |
| Fire staff | `staff/fire` | Staff and fire essence, shapeless |
| Healing staff | `staff/heal` | Staff and life essence, shapeless |
| Ice staff | `staff/ice` | Staff and ice essence, shapeless |
| Poison staff | `staff/poison` | Staff and poison essence, shapeless |
| Water staff | `staff/water` | Staff and water essence, shapeless |
| Wind staff | `staff/wind` | Staff and wind essence, shapeless |
| Magic essence | `essence/magic` | One of each of the other eight essences, shapeless |
| Darkness, electric, fire, ice, life, poison, water and wind essence | `essence/<name>` | Chest loot only |
| Dark, electric, fire, ice, life, poison, water and wind arrow | `arrow/<name>` | Eight arrows around the essence make 8 |
| Dark, electric, fire, ice, life, poison, water and wind grenade | `grenade/<name>` | Eight glass bottles around the essence make 8 |
| Vampire ball, heal ball and water ball | `special/vampire`, `special/hearth`, `special/bubble` | None, they only give the staff projectiles their look and are in no tab |

The enchantment Magical Power I to III (`elementary_staffs:magical_power`) only goes on the eight
elemental staffs. It comes from enchanted books in chest loot and is applied in an anvil. The
staffs cannot be enchanted at the table, as in 2.0.0.

## How it works

Every staff loses one point of durability per use and is repaired in an anvil with its own
essence. Magical Power raises a multiplier that is 1 without the enchantment and 1, 1.5 and 2
with levels I, II and III, as in 2.0.0. The ice and poison staffs and their hits add the level to
the effect strength directly, and the fire staff only hurts with the enchantment.

| Staff | Durability | Use | Hit |
|---|---|---|---|
| Darkness | 100 | Shoots a vampire ball that drains 2 health from what it hits and sends a heal ball back that heals the player by the same amount | Normal hit |
| Electric | 100 | Launches the player in the direction of view and catches the next fall | Normal hit |
| Fire | 400 | Sets living entities up to about 6 blocks in front on fire and pushes them. With Magical Power it also hurts. Does nothing under water | Sets the target on fire for 4 seconds |
| Healing | 1024 | Nothing | Heals the target by 2 instead of hurting it |
| Ice | 1024 | Slowness for 3.5 seconds on every living entity within 6.5 blocks. Costs durability only when there is one | Slowness II for 11.5 seconds |
| Poison | 1024 | Poison II for 15 seconds on every living entity within 6.5 blocks | Poison II for 15 seconds |
| Water | 400 | Shoots a water ball. What it hits rises in a bubble for 5 seconds, which puts out fire | Normal hit |
| Wind | 150 | Sends out a whirlwind for 9 seconds that pulls entities within 4 blocks and hurts living ones at its center | Normal hit |

Arrows go in bows, crossbows and dispensers through the tag `minecraft:arrows`. On a hit the dark
arrow blinds, the electric arrow adds magic damage that triples on confused targets, the fire arrow
burns, the ice arrow slows and freezes, the life arrow heals back what it hurt and 2 more, the
poison arrow poisons, the water arrow confuses and puts out fire, and the wind arrow knocks back
as with Punch II. A picked up elemental arrow turns into a plain arrow, as in 2.0.0.

Grenades are drawn like a bow and fly farther the longer they are held. They burst on the first
block or entity and act on living entities within 4 blocks across and 2 up and down, but never on
the thrower, except that the water grenade also confuses and puts out its thrower. Dark blinds and
deals 8 damage, electric deals 4 or 8 to wet or confused targets, fire burns for 8 seconds, ice
slows and deals 8, life gives Regeneration II and heals 1.5, poison poisons for 20 seconds, water
confuses, puts out fire and deals 8 or 16 to fire immune targets, and wind pushes everything away
and deals 4.

Each chest loot table from the game or a mod jar gets one extra pool that draws either from
`elementary_staffs:chests/essences` or from `elementary_staffs:chests/magical_power`, as in 2.0.0.
Data packs or KubeJS can change these tables.

### Settings

The settings of 2.0.0 are in `config/elementary_staffs-common.toml`.

| Setting | Default | Meaning |
|---|---|---|
| `dungeonLoot` | true | Add the essence pool to chest loot |
| `magicalPowerEnchantment` | true | Allow Magical Power and put its books into the loot |
| `cooldownStaffs` | false | Put the staffs on a cooldown of 5 or 10 ticks after each use |
| `cooldownGrenades` | false | Put the grenades on a cooldown that grows with the draw time |
| `blacklistedModIds` | empty | Namespaces whose chest tables get no essences |

In SkyTekx2 on 1.12.2 a script removed the essence pool from some Chocolate Quest and Quark chests
as excessive. The blacklist does the same here, by namespace.

## Changes against 2.0.0

- The code is plain Java for Forge without Kotlin, owo-lib or mixins. 2.0.0 used a mixin into
  `Player.attack` for the staff hits. Forge calls `Item.onLeftClickEntity` at the same place, so
  the port needs no mixin.
- The game logic of staffs, arrows, grenades and staff projectiles runs on the server only.
  Sounds and particles are sent from the server, so other players see and hear them too. In 2.0.0
  most of them played only for the user. The electric staff and the whirlwind move each player on
  that player's own client, as before.
- The wind arrow had the entity type of the water arrow, so other players saw a water arrow. It
  now has its own. Its extra knockback was added each time a player started to see the arrow and
  is now that of two Punch levels.
- Grenades burst on the first hit and are removed. In 2.0.0 a grenade that hit an entity, or the
  side of a block in some directions, flew on and could burst again.
- The throw speed of grenades stops at 1.5 blocks per tick, the speed of a snowball, after 3.75
  seconds. In 2.0.0 it grew without a limit.
- Grenade and fire staff damage hits only living entities, so item frames, paintings, boats and
  minecarts are safe. The wind grenade still pushes them. Grenades deal thrown damage instead of
  generic damage, so the thrower gets the kill and claim plugins see who did it.
- Vampire and water balls speed up in water only up to 3 blocks per tick. In 2.0.0 they sped up
  without a limit, which made the server load every chunk on their way.
- Two whirlwinds of different owners call down lightning at most once a second each. In 2.0.0
  it was every tick.
- The heal ball cannot heal twice, and one without an owner disappears after 5 seconds.
- Magical Power on a staff in the off hand now counts for the vampire ball, water ball and
  whirlwind. 2.0.0 read it from the main hand on the server.
- Magical Power has its own enchantment category. In 2.0.0 the enchanting table could offer it
  for any damageable item, where it did nothing.
- Dispensers shoot the elemental arrows. In 2.0.0 they dropped them as items.
- The settings moved from the owo config screen to a Forge config file. The English keys of the
  config screen were dropped, and names for the entities and hidden items were added.
- Creative tabs, item ids, entity ids, the essence loot and the recipes are unchanged apart from
  the namespace.

## Differences between 2.0.0 and 1.0.6

Players of SkyTekx2 know version 1.0.6 for Minecraft 1.12.2 with the mod id `element`. 1.0.6 had
the staff, the eight staffs, the nine essences and the three projectile items. Arrows, grenades,
Magical Power and the settings came with 2.0.0, so the port keeps every item of 1.0.6. Where the
two differ, the port follows 2.0.0.

- The magic essence was a shaped recipe in 1.0.6 and is shapeless in 2.0.0. The recipes of 1.0.6
  also took ore dictionary essences.
- The fire staff in 1.0.6 set every entity within 6 blocks around the player on fire and never
  hurt. In 2.0.0 it acts in front of the player, not under water, and hurts with Magical Power.
- The ice staff slowed for half a second in 1.0.6 and for 3.5 seconds in 2.0.0.
- The healing staff, the ice staff and the poison staff hit only mobs in 1.0.6. In 2.0.0 they work
  on every living entity, players included.
- The electric staff reset the fall distance in 1.0.6. In 2.0.0 it also catches the next fall.
- The water staff could not be used in water in 1.0.6. The bubble rose for 70 ticks in 1.0.6 and
  rises for 105 in 2.0.0.
- The whirlwind lived for 4 seconds in 1.0.6 and lives for 9 in 2.0.0.
- The essence pool in 1.0.6 added one essence to every chest. In 2.0.0 the pool draws from its own
  table and may add Magical Power books.

## Building and testing

The build needs a JDK 17.

- Run `./gradlew build` to write the mod to `build/libs/elementary-staffs-1.20.1-2.0.0.1.jar`.
  The jar is reobfuscated for production and holds no test classes.
- Run `./gradlew runGameTestServer` to run the 25 GameTests in `src/gametest` on a Forge game test
  server. It exits with the number of failed tests.
- Run `./gradlew runServer` to start a dedicated development server in `run/server`.
