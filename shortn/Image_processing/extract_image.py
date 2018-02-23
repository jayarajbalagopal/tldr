from PIL import Image
from pytesseract import image_to_string
import sys
import os
from tldr.settings import BASE_DIR

fname = sys.argv[1]
abs_fname = os.path.join(BASE_DIR,'media','documents',fname.name)
grey_name = os.path.join(BASE_DIR,'media','documents','greyscale',fname.name)

x=Image.open(abs_fname,'r')
x=x.convert('L')
x.save(grey_name)

y=Image.open(grey_name,'r')

out = image_to_string(y)
out = out.encode('utf-8')

with open(os.path.join(BASE_DIR,'shortn','Inputs','art.txt'),'w') as fp:
	fp.write(out)

os.remove(abs_fname)
os.remove(grey_name)