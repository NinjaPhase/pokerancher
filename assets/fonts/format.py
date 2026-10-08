import os
import math
from PIL import Image, ImageFilter

im = Image.open('latin_small.png')


num_rows = math.floor(im.height / 16);

buffer_size = num_rows * 16 * 64;

test = list(range(buffer_size))

dstOff = 0

for row in range(num_rows):
  for col in range(16):
    for glyphTile in range(4):
      pixelsX = (col * 16) + ((glyphTile % 1) * 8)

      for i in range(8):
        pixelsY = (row * 16) + ((glyphTile >> 1) * 8) + i
        pixel = im.getpixel((pixelsX, pixelsY))
        test[dstOff] = im.getpixel((pixelsX, pixelsY))
        test[dstOff + 1] = im.getpixel((pixelsX + 1, pixelsY))
        dstOff += 2;
