from django import forms

class DocumentForm(forms.Form):
    docfile = forms.FileField(label='',widget=forms.FileInput(attrs={'class':'upload',
    	'id':'file',
    	'onchange':'ani();'})
    )