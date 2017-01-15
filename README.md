gb-rta-bruteforce
==========

#### Credits

This code wouldn't be possible without the work of [MrWint's gb-tas-gen](https://github.com/mrwint/gb-tas-gen) which the JNI libgambatte bridge is taken from.

Most of the code has been written by [Dabomstew](https://github.com/Dabomstew/gb-rta-bruteforce).

#### Disclaimer

Spaghetti code all over the place, this code isn't really meant for public consumption but others have kindly volunteered their CPU time to run it so I'm sharing it anyways.

#### Overview

This is a bruteforce bot originally created to find God Nidorans for Pokemon Red/Blue RTA speedruns. It can be adapted to find desired encounters in other places too.

There are also implementations for checking IGT0 frames for Mt. Moon manipulations, as well as TID manipulations for Red/Blue and Crystal.

The bot runs a modified [libgambatte](https://github.com/sinamas/gambatte) as a core, and spits out giant log files to be combed over manually.

#### Installation (Linux/Mac)

You'll need some prerequisites: `ant`, `scons`, `libsdl1.2-dev`, as well as a Java (8+) and a C compiler.

Clone the repository, build the JNI interface by running `ant` in `libgambatte/java/`, and then compile gambatte by running `scons` in `libgambatte/`. You might need to change some paths in `libgambatte/SConstruct` for the JNI because I'm lazy like that.

After that you can put/link the compiled `libgambatte` library into your library path (e.g. `/usr/lib`) to have it be detected by the Java runtime, and then fire up your favorite Java IDE and start using it.

#### Installation for Windows

Check out [this guide by piapwns](http://pastebin.com/iexyJ2Q7).

You should skip the step relating to editing SConstruct and you don't need to rename cyggambatte.dll anymore.

#### Basic Usage

The framework is written in Java, so it's easiest to use with Java (or compatible languages).

Main programs are in NidoBot/ShrewBot/LassIGT0Checker/MoonIGT0Checker/CrystalTIDManip/RedBlueTIDManip .java files. Each of these programs can be modified for custom manip needs.

Currently, the programs are set up expecting a roms folder to be created in the root directory. The expected names for the ROMs are: pokered.gbc, pokeblue.gbc, pokecrystal.gbc, and gbc_bios.bin (for the GBC bootrom).

There is a dependency needed right now for the FFEF bot. You can use the gradle wrapper provided to import it (`gradlew idea` or `gradlew eclipse`; change to `gradlew.bat` if on Windows) and add the library to your classpath to make sure the bot runs properly.