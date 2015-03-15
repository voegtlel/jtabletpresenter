# JTabletPresenter
(If you are not interested, skip to [Installation])
JTabletPresenter was developed by Lukas Vögtle for a lecture on university, where the lecturer was unhappy with the quality of existing commercial products as presentation tools. Its aim is to make a simple way for the lecturer to write on a "whiteboard" using the digital pen and export the result as pdf to provide it to the students.

Later on, support for rendering PDFs and storing the annotations was added.
In addition an experimental feature for live streaming modifications was successfully tested in a lecture.

The tool has capabilities to upload live annotations to a server, which students may connect to using the client to add live private annotations. Read More in [IntroductionAndPurpose].

## Features
 * Draw directly on screen using a digital pen or mouse
 * Supports digital pen detection
 * Serialize current editing state in internal format to continue editing later
 * Undo-Redo (History)
 * Quick-Tool-Panel (large buttons for usage with digital pen)
 * Open and draw on PDFs
 * Serialize as PDF
 * Shortcuts
 * (experimental) Live stream to server


# Installation
Simply unpack the archive and run the `JTabletPresenter.jar` file.

## Hints
### Windows
If you have Java installed, it should be possible to directly run the `.jar` file.

If you want to use the `.bat` files, the PATH environment variable must be set correctly for `javaw.exe`. (Computer -> Properties -> Advanced System Settings -> Advanced -> Environment Variables -> Under System Variables search for "`Path`" and add "`;<Path to java bin>`")

### Linux
If you have a Java Runtime installed, everything should work without problems. The jar can be started via `start.sh` (adds "." to the searchpath for native libraries).

On some machines it is required to copy the `libfreetype.so.*` to the binary directory as `libfreetype.so`.


# Default configuration

## Intention
The default config file is designed for presentation using digital pens.

## Key Features
 * Designed for 4:3 aspect ratio, 1024x768 (default presentations)
 * Intentional coloring (darkened default colors for better readability)
 * Uses JMuPdf renderer (has better support)
 * Autosaving for each page and on exit
 * Default saving/loading as PDF (unfortunately the internal format is not desired by most users though it would be much more practicable :( )
 * No networking
 * Intentional shortcuts:
     - open/save (Ctrl+O/S)
     - next page (N, Space, Page Down, Right, Down)
     - previous page (P, Page Up, Left, Up)
     - undo/redo (ctrl+Z/Y)
     - toggle fullscreen + toolbar (F1, Escape)
     - toggle toolbar (F2)
     - show color square (C)
     - use scribble/eraser/line/image tool as primary (1/2/3/4)


# Configuration

The config.ini file has a simple format:
- Comments start with # (Hash)
- Key-Value pair consists of: \w`*`([a-zA-Z.])\w`*`=\w`*`(.`*`)\w`*` where \1 is the key and \2 is the value.

## Configuration Variables

### Editor
 * `editor.variableThickness` (boolean, false): If true and a real pen is used, the pressure of the pen is used for drawing.
 * `editor.voidColor` (color, FF000000): Background color for void space (outside of aspect ratio clipping)
 * `editor.eraser.thickness` (float, 15.0): Thickness (radius) of the eraser
 * `editor.eraser.fastCollide` (boolean, true): If true, a fast collision algorithm will be used for erasing (only erase vertexes), otherwise line segments can be partially erased
 * `editor.selectMove.thickness` (float, 1.0): Thickness of the select-move-tool borders
 * `editor.defaultPen.thickness` (float, 1.0): Thickness of the pen
 * `editor.defaultPen.color` (Color, FF000000): Color of the pen
 * `editor.scribble.drawThreshold` (float, 0.0): The Threshold, at which drawing will start. Can be used to prevent squiggles.

### Document
 * `document.background.color` (color, FFFFFFFF): Background color for the whole document
 * `document.useDefaultRatio` (boolean, false): If true, use the default ratio for pages (see `document.defaultRatio` and `pdf.useRatio`)
 * `document.defaultRatio` (float, 4.0/3.0): If using default ratio (see `document.useDefaultRatio`), this specifies the ratio (width/height)

### PDF
 * `pdf.renderer` (int, 0): Specifies the PDF renderer (0: JPod, 1: JMuPDF)
 * `pdf.useRatio` (boolean, false): If true, for PDF pages the ratio of the PDF page is used (overrides `document.useDefaultRatio`)
 * `pdf.defaultWidth`, `pdf.defaultHeight` (int, 1024x768): Default PDF width/height for exporting as PDF if there is no PDF page set for the rendered page
 * `pdf.ignoreEmptyPages` (boolean, false): If true, empty pages won't be exported to PDF
 * `pdf.showPageNumber` (boolean, true): If true, the page number is printed on the lower right on PDFs
 * `pdf.ignoreEmptyPageNumber` (boolean, true): If true and empty pages are printed (`pdf.ignoreEmptyPages`) these won't have page numbers
 * `pdf.thicknessFactor` (float, 0.2): The factor for line thickness for rendering to PDF

### General
 * `color.red`, `color.green`, `color.blue`, `color.black` (Color, FFBB0000, FF00BB00, FF0000BB, FF000000): Color values for the color-square selected colors.
 * `fullscreen.autotoggleToolbar` (boolean, true): If true, the toolbar is hidden when changing into fullscreen and shown when switching to windowed mode

### Save/Load
 * `autosave.next`, `autosave.previous`, `autosave.spinner` (boolean, true): If true, the current page will be autosaved on next/previous/spinner selected page
 * `autosave.saveExit` (boolean, false): If true, the current session is autosaved as session.dat (see `autosave.loadStartup`)
 * `autosave.loadStartup` (boolean, false): If true, the autosaved session.dat is loaded on startup (see `autosave.saveExit`)
 * `save.defaultExt`, `open.defaultExt` (string, jpd): Default selected extension for the save/open-dialog
 * `file.dialog.saveLocation` (boolean, false): If true, the last location of saving/loading dialog will be remembered (see `file.dialog.location`)
 * `file.dialog.location` (string, empty): If the location is saved (see *  * `file.dialog.saveLocation`), then this stores the last location.

### Shortcuts
`shortcut.*`: Configuration for shortcuts, Actions:

 * `shortcut.tools.new.*`: New Document
 * `shortcut.tools.open.*`: Open file dialog
 * `shortcut.tools.save.*`: Save file dialog
 * `shortcut.tools.toggleToolbar.*`: Toggle the toolbar
 * `shortcut.tools.primary.scribble.*`: Select scribble tool as primary tool
 * `shortcut.tools.primary.eraser.*`: Select eraser tool as primary tool
 * `shortcut.tools.primary.deleter.*`: Select deleter tool as primary tool
 * `shortcut.tools.primary.image.*`: Select image tool as primary tool
 * `shortcut.tools.primary.selectMove.*`: Select selectMove tool as primary tool
 * `shortcut.tools.secondary.scribble.*`: Select scribble tool as primary tool
 * `shortcut.tools.secondary.eraser.*`: Select eraser tool as primary tool
 * `shortcut.tools.secondary.deleter.*`: Select deleter tool as primary tool
 * `shortcut.tools.secondary.image.*`: Select image tool as primary tool
 * `shortcut.tools.secondary.selectMove.*`: Select selectMove tool as primary tool
 * `shortcut.tools.page.new.*`: Create new page
 * `shortcut.tools.page.clone.*`: Clone current page
 * `shortcut.tools.page.delete.*`: Delete current page
 * `shortcut.tools.page.clear.*`: Clear current page
 * `shortcut.tools.document.new.*`: New Document (clear all)
 * `shortcut.tools.document.clearPdf.*`: Clear the background PDF
 * `shortcut.tools.document.openPdf.*`: Open a PDF for the background
 * `shortcut.next.*`: Next page
 * `shortcut.previous.*`: Previous page
 * `shortcut.undo.*`: Undo
 * `shortcut.redo.*`: Redo
 * `shortcut.toggleFullscreen.*`: Toggle fullscreen
 * `shortcut.color.*`: Show color square


## Network streaming
The idea behind this is to allow a lecturer to draw on the screen and allow students to add notes on their private file. To allow this, the data on the lecturers screen is streamed to a server (write-only), that distributes the current state to its clients (read-only). It is possible at any time to connect to the server.

This feature is still experimental, but was tested on a lecture (about 20 hours of running), where it did never crash. If you plan to use this feature, please contact me directly.
