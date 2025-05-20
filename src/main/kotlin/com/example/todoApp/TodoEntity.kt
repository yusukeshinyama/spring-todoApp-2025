package com.example.todoApp

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id

@Entity
class TodoEntity(
    @Id
    @GeneratedValue
    var id: Long? = null,
    var text: String,
)
