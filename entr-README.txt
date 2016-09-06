- Intended to work on Bizhawk w/modified libgambatte for console RNG accuracy

- Instructions in MrWint's README should be enough to build libgambatte and
  the JNI interface

- Ensure you have the right JDK directories in SConstruct file (global cpppath)

- Make sure the path you place new libgambatte is included in java.library.path
  
- Generated .bkm files appear to have an extra frame included after hard resets

- My added files are in segment/pokemon/gen1/rta, if you want an example

- Working within the framework of Segments and StateBuffers should be fine,
  but we'll have to create a lot of this from scratch (MrWint's TAS are for very
  different paradigm)
