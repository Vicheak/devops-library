#!/usr/bin/env groovy
def call(String name){
    sh "echo 'Hello ${name}'"
}