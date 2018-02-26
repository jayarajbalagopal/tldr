from django.shortcuts import render,redirect
from django.views.generic import TemplateView
from tldr.settings import BASE_DIR
from django.http import HttpResponseRedirect
import sys
# Create your views here.
import os
import sys
import subprocess
from .forms import DocumentForm
from .models import Document

class shortnView(TemplateView):
	def get(self,request,*args,**kwargs):
		
		form = DocumentForm()
		context = {
			'form' : form,
		}
		return render(request,"home.html",context)

	def post(self,request,*args,**kwargs):
		summary = ""
		form = DocumentForm(request.POST, request.FILES)
		if form.is_valid():
			filename = request.FILES.get('docfile')
			newdoc = Document(docfile = request.FILES['docfile'])
			newdoc.save()
			sys.argv[1] = filename 
			
			confname = filename.name
			extensions = confname.split('.')
			
			if(extensions[1]=="jpeg" or extensions[1]=="jpg"):
				execfile(os.path.join(BASE_DIR,'shortn','Image_processing','extract_image.py'))
				return HttpResponseRedirect('/shortn/summarize')
			elif(extensions[1]=="pdf"):
				execfile(os.path.join(BASE_DIR,'shortn','Image_processing','PDFtoText.py'))
				return HttpResponseRedirect('/shortn/summarize')
			else:
				os.remove(os.path.join(BASE_DIR,'media','documents',filename.name))
				return HttpResponseRedirect('/error')


		execfile(os.path.join(BASE_DIR,'shortn/Extractive/driver.py'))
		with open(os.path.join(BASE_DIR,'shortn/Outputs/out.txt'),'rb') as fp:
			summary = fp.read()
		context = {}
		return render(request,"home.html",context)


class generatesummary(TemplateView):
	def get(self,request,*args,**kwargs):
		context = {
			'flag':0
		}
		return render(request,"summary.html",context)

	def post(self,request,*args,**kwargs):
		summary = ""
		flag = 0 
		execfile(os.path.join(BASE_DIR,'shortn/Extractive/driver.py'))
		with open(os.path.join(BASE_DIR,'shortn/Outputs/out.txt'),'rb') as fp:
			summary = fp.read()

		if summary:
			flag = 1
		context = {
			'sum' : summary,
			'flag' :flag
		}
		return render(request,"summary.html",context)


def err(request):
	if request.method =='GET':
		context={}
		return render(request,'error.html',context)


def get_started(request):
	return render(request,'getstarted.html')

def about_us(request):
	return render(request,'aboutus.html')

def rand(request):
	return redirect('/shortn')