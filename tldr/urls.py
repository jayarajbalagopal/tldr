"""tldr URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/1.8/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  url(r'^$', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  url(r'^$', Home.as_view(), name='home')
Including another URLconf
    1. Add an import:  from blog import urls as blog_urls
    2. Add a URL to urlpatterns:  url(r'^blog/', include(blog_urls))
"""
from django.conf.urls import include, url
from django.contrib import admin
from shortn.views import shortnView,get_started,about_us
from shortn.views import generatesummary,rand
from django.conf import settings
from shortn.views import err

urlpatterns = [
    url(r'^admin/', include(admin.site.urls)),
    url(r'^shortn/home/$',shortnView.as_view(),name='home'),
    url(r'^shortn/aboutus/$',about_us,name='aboutus'),
    url(r'^shortn/$',get_started,name='shortn'),
    url(r'^shortn/summarize/$',generatesummary.as_view(),name='sum'),
    url(r'^error/$',err,name='err'),
    url(r'^$',rand),
]
