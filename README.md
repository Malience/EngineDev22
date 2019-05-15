# EngineDev22
EngineDev 2.2

A reimplementation of the old EngineDev project using Vulkan instead of OpenGL.

# Features
Forward renderer implemented in Vulkan

Bindless Textures: Rather than binding the textures of an object for each object rendered, Textures are kept in an array and selected based on the objects "index".

Most object information stored in Uniform Buffer Objects to reduce the number of descriptor set changes per frame.

Stores scenes in a JSON format.

# Releases

Demo's can be found here:

https://github.com/MasterMatthew/EngineDev22/releases
