package com.example.photodrawer

interface UndoRedoListener
{
    fun simpleUndoRedoNotify(map: Map<String, Boolean>)
}