package com.example.semproject

data class Library(
    val id: String = "", // Identifikátor knihy (pro Firestore)
    var name: String = "" , // Název knihy
    var autor: String = "", //jméno autora
    var place: String = "", //místo, kde se nachází kniha
    var type: String = "", //žánr knihy
    var taken: Boolean = false
)
