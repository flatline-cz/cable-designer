# cable-designer
Generates production documentation for cable harness from textual design

# Audience
Anyone who need to create a manufacturing specification for cable harness.

# What this program IS
This program allows you to describe the wires, connectors, etc in a form of 
structured text document. 

There are 3 kind of information you need to provide:
1. Signal strength (maximum current)
2. On which pins the signal is present
3. Physical layout of the harness 

Even in this early stage the program runs some 
fundamental checks (it will not allow you to assign 2 different signal to 
a single pin).

It will find routing for all the signals you have described. Then it will 
try to choose appropriate wires, pins, pin seals and so on. Finally, it will
generate a documentation in the format you have choosen (txt, xlsx or pdf).

# What this program IS NOT
1. Finished. I have a list of improvements that I'm going to get done. These are 
those I need to be satisfied with my own harness.
2. Fancy GUI

# List of improvements:

1. Multi-wire cables
2. <del>**STAR** topology of certain signals (especially ground)</del> *Done*
3. Connector variants
4. <del>Pin sections</del> *Done*
5. Logical signal routing (based on device description)
6. <del>Correct handing of pin size / wire size when more than one wire is 
connected to the pin</del> Done


# Arguments
**-I** directory - (a list of) directory where the source files are

**-O** directory - output directory

**-F** format - output format (txt, xlsx, pdf)

First other argument is the source file.