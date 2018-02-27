import os
import sys
from tldr.settings import BASE_DIR

def getsummary():

	try:
		cratio = sys.argv[1]
		if(os.path.exists(os.path.join(BASE_DIR,'shortn/Inputs/art.txt'))):
			os.system('javac '+os.path.join(BASE_DIR,'shortn/Extractive/Controller.java'))
			os.system('java -cp '+os.path.join(BASE_DIR,'shortn/Extractive Controller '+os.path.join(BASE_DIR,'shortn/Inputs/art.txt ')+cratio))
			# file = "java Controller ../Inputs/"
			# file = file + filename
			# os.system(file)

			# with open("../Outputs/out.txt","rb") as fp:
			# 	print(fp.read())
		else:
			print("no such file")

	except:
		print("no file")

getsummary()
