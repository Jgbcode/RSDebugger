# RedstoneDebugger
A bukkit plugin to aid in debugging large-scale sequential redstone circuits.

## Developing in Eclipse
1. Right click in the package manager and select "Import".
2. Locate the maven folder and select "Check out Maven Projects from SCM".
3. If you do not have the SCM handler for git click "m2e Marketplace", install the git handler, and restart eclipse.
4. Use the git SCM connector and input https://github.com/Jgbcode/RSDebugger as the URL.
5. Click finish.

## Installing
You may either download the plugin from [the release page](https://github.com/Jgbcode/RSDebugger/releases) or you may clone the repository and use the command "mvn install" while in the cloned repo to build the jar yourself. Place the jar in your plugins folder.

## Permissions
**rsdebugger.use** - allows the player to use the redstone debugger <br/>
**rsdebugger.genscript** - allows the player to generate scripts <br/>
**rsdebugger.load** - allows the player to load scripts from remote locations <br/>

## Wiki
For help regarding the usage of the plugin refer to the [wiki](https://github.com/Jgbcode/RSDebugger/wiki).

## License
This project is licensed under the GNU General Public License - see the [LICENSE](https://github.com/Jgbcode/RSDebugger/blob/master/LICENSE) file for details.
