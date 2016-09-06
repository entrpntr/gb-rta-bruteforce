- Intended to work on Bizhawk w/modified libgambatte for console RNG accuracy

- Instructions in MrWint's README should be enough to build libgambatte and
  the JNI interface

- Ensure you have the right JDK directories in SConstruct file (global_cpppath)

- Make sure the path you place new libgambatte is included in java.library.path

- Create a roms/ directory in the top level and place a Red ROM named red.gb in
  the directory. If working with save files, you can put a red.sav as well.
  
- Generated .bkm files initially appeared to have an extra frame included
  after hard resets. Now that we are working with .sav files, there are a few
  more [minor] issues to work out with movie files.

- My added files are in segment/pokemon/gen1/rta, if you want an example

- Working within the framework of Segments and StateBuffers should be fine,
  but we'll have to create a lot of this from scratch (MrWint's TAS are for very
  different paradigm)
