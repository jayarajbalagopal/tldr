import PyPDF2

fname = sys.argv[1]
abs_fname = os.path.join(BASE_DIR,'media','documents',fname.name)
pdfFileObj = open(abs_fname, 'rb')

pdfReader = PyPDF2.PdfFileReader(pdfFileObj)
pdfReader.numPages
pageObj = pdfReader.getPage(0)
out = pageObj.extractText()
out = out.encode('utf-8')

out.replace('\n',' ')

with open(os.path.join(BASE_DIR,'shortn','Inputs','art.txt'),'w') as fp:
	fp.write(out)

os.remove(abs_fname)
