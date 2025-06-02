# SkiesSkins
<img height="50" src="https://camo.githubusercontent.com/a94064bebbf15dfed1fddf70437ea2ac3521ce55ac85650e35137db9de12979d/68747470733a2f2f692e696d6775722e636f6d2f6331444839564c2e706e67" alt="Requires Fabric Kotlin"/>

A Fabric server-sided Skins mod for Cobblemon, allowing players to purchase, apply, and remove Skins through a managed system. Designed to be highly customizable, with extensive options when possible!

More information on configuration can be found on the [Wiki](https://github.com/PokeSkies/SkiesSkins/wiki)!

## Features
- (mostly) Infinite skins
  - Full aspect customization
  - Display names
  - Descriptions
- (mostly) Infinite Shops
  - Random Skin Sets with refresh timers
  - Static Skin Sets
  - Package Skin Sets
  - Per Skin currency options
  - Purchase Limits
- Scraping System
  - Allow players to trade unwanted skins for currency
- Customizable GUIs
- Economy Integrations (Impactor, Pebbles Economy, CobbleDollars, and BEconomy)
- Supports both SQL and MongoDB databases for data storage

## Installation
1. Download the latest version of the mod from [Modrinth](https://modrinth.com/mod/skiesskins).
2. Download all required dependencies:
  - [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin)
  - [Cobblemon](https://modrinth.com/mod/cobblemon)
3. Download any optional dependencies:
  - [Impactor](https://modrinth.com/mod/impactor) **_(Economy, Placeholders)_**
  - [MiniPlaceholders](https://modrinth.com/plugin/miniplaceholders) **_(Placeholders)_**
  - [PlaceholderAPI](https://modrinth.com/mod/placeholder-api) **_(Placeholders)_**
4. Install the mod and dependencies into your server's `mods` folder.
5. Configure the mod in the `./config/skiesskins/` folder.

## Commands/Permissions
| Command                                              | Description                                                                       | Permission                             |
|------------------------------------------------------|-----------------------------------------------------------------------------------|----------------------------------------|
| /skins                                               | Opens the player's Skins inventory                                                | skiesskins.command.base                |
| /skins reload                                        | Reload the Mod                                                                    | skiesskins.command.reload              |
| /skins debug                                         | Toggle the debug mode for more insight into issues                                | skiesskins.command.debug               |
| /skins giveskin \<targets> \<skin_id> \[amount]      | Give players a specific skin, with an amount optional                             | skiesskins.command.giveskin            |
| /skins remover                                       | Opens the Skin Remover UI                                                         | skiesskins.command.remover             |
| /skins remover \[player]                             | Opens a Skin Remover UI for another player                                        | skiesskins.command.remover.other       |
| /skins shop \<shop_id> \[player]                     | Opens the specified Skin Shop, optionally for another player                      | skiesskins.command.shop                |
| /skins resetinventory \<player>                      | Resets the specified player's Skin inventory                                      | skiesskins.command.shop.resetinventory |
| /skins resetshop \<targets> \<shop_id>               | Resets all of a player's data for a given shop                                    | skiesskins.command.shop.resetshop      |
| /skins resetshop \<targets> \<shop_id> random \[set] | Resets a player's Random Skins for a given shop, optionally for a specific set    | skiesskins.command.shop.resetshop      |
| /skins resetshop \<targets> \<shop_id> static        | Resets a player's Static Skins for a given shop                                   | skiesskins.command.shop.resetshop      |
| /skins resetshop \<targets> \<shop_id> packages      | Resets a player's Package Skins for a given shop                                  | skiesskins.command.shop.resetshop      |

| Permission                | Description                                           |
|---------------------------|-------------------------------------------------------|
| skiesskins.open.<shop_id> | Permission to open a Shop when using an Alias Command |


## Planned Features
- Migrate to SGui instead of GooeyLibs
- Add more placeholder integrations
- Message Configuration
- Sound Configuration
- Additional Shop Features
  - Purchase History
  - Price variation?
  - Permission based discounts
  - Package Availability timer
- Inventory Filtering
  - Oldest-newest
  - Newest-oldest
  - Species A-Z
  - Species Z-A
  - Dex Highest-Lowest
  - Dex Lowest-Highest
  - Skin Type A-Z
  - Skin Type Z-A
- Skin Collections/Previewing UI

## Donations
This mod was developed as part of the Skies Development goal of **providing free, high quality, and open sourced mods** for the Cobblemon and Fabric communities! If you are able to support this mission, **please consider making a one-time donation or becoming a Member** on [Ko-fi](https://ko-fi.com/stampede2011). Being a member gives you early access to all new mods as well as helping decide on the development direction.
<br><br>
During this mods development, the following people supported Skies Development through an active Membership: **Jephon, Vince, Zephyr, iMystxc, Mango, GriffinFluff, Guga, AllieDragon, Frost, and Ezequiel.**  Thank you for your generosity! ❤️

## Support
A community support Discord has been opened up for all Skies Development related projects! Feel free to join and ask questions or leave suggestions :)

<a class="discord-widget" href="https://discord.gg/cgBww275Fg" title="Join us on Discord"><img src="https://discordapp.com/api/guilds/1158447623989116980/embed.png?style=banner2"></a>
