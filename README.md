gb-rta-bruteforce
==========

#### Credits

This code wouldn't be possible without the work of [MrWint's gb-tas-gen](https://github.com/mrwint/gb-tas-gen) which the JNI libgambatte bridge is taken from. All other code is written (somewhat crudely) by me, Dabomstew

#### Disclaimer

Spaghetti code all over the place, this code isn't really meant for public consumption but others have kindly volunteered their CPU time to run it so I'm sharing it anyways.

#### Overview

This is a bruteforce bot to find God Nidorans for Pokemon Red RTA. It could be adapted to find other encounters in other places too.

It runs a modified [libgambatte](https://github.com/sinamas/gambatte) as a core, and spits out giant log files to be combed over manually.

#### Installation (Linux/Mac)

You'll need some prerequisites: `ant`, `scons`, `libsdl1.2-dev`, as well as a Java (8+) and a C compiler.

Clone the repository, build the JNI interface by running `ant` in `libgambatte/java/`, and then compile gambatte by running `scons` in `libgambatte/`. You might need to change some paths in `libgambatte/SConstruct` for the JNI because I'm lazy like that.

After that you can put/link the compiled `libgambatte` library into your library path (e.g. `/usr/lib`) to have it be detected by the Java runtime, and then fire up your favorite Java IDE and start using it.

#### Installation for Windows

Check out [this guide by piapwns](http://pastebin.com/iexyJ2Q7).

You should skip the step relating to editing SConstruct and you don't need to rename cyggambatte.dll anymore.

#### Basic Usage

The framework is written in Java, so it's easiest to use with Java (or compatible languages).

Adjust NidoBot.java to your needs, run it and have fun.


