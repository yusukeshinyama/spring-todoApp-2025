package com.example.todoApp

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class TodoController(
    @Autowired private val repository: TodoRepository
) {

    @GetMapping("/todos")
    fun getTodos(): List<TodoEntity> {
        val todos = repository.findAll()
        return todos.toList()
    }

    @PostMapping("/todos")
    fun postTodo(@RequestBody todo: TodoEntity): Long {
        val savedTodo = repository.save(todo)
        return savedTodo.id!!
    }

    @GetMapping("/todos/{id}")
    fun getTodoById(@PathVariable id: Long): ResponseEntity<TodoEntity> {
        val todo = repository.findById(id)
        if (todo.isPresent) {
            return ResponseEntity.ok(todo.get())
        } else {
            return ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/todos/{id}")
    fun deleteTodo(@PathVariable id: Long) {
        repository.deleteById(id)
    }
}