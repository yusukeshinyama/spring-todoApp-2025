package com.example.todoApp

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TodoAppApplicationTests(
	@Autowired val restTemplate: TestRestTemplate,
	@LocalServerPort val port: Int,
	@Autowired val repository: TodoRepository,
) {

	@Test
	fun contextLoads() {
	}

	@BeforeEach
	fun setup() {
		// 各テストは項目が空の状態で始める。
		repository.deleteAll()
	}

	@Test
	fun `最初のテスト`() {
		assertThat(1+2, equalTo(3))
	}

	@Test
	fun `GETリクエストはOKステータスを返す`() {
		// localhost/todos に GETリクエストを発行する。
		val response = restTemplate.getForEntity("http://localhost:$port/todos", String::class.java)

		// レスポンスのステータスコードは OK である。
		assertThat(response.statusCode, equalTo(HttpStatus.OK))
	}

	@Test
	fun `GETリクエストは空のリストを返す`() {
		// localhost/todos に GETリクエストを送り、レスポンスを TodoEntity の配列として解釈する。
		val response = restTemplate.getForEntity("http://localhost:$port/todos", Array<TodoEntity>::class.java)
		val todos = response.body!!

		// 配列は0個の要素をもつこと。
		assertThat(todos.size, equalTo(0))
	}

	@Test
	fun `POSTリクエストはOKステータスを返す`() {
		// localhost/todos に POSTリクエストを発行する。このとき、新規に作成する TodoEntity を送る。
		val newTodo = TodoEntity(text="hello")
		val response = restTemplate.postForEntity("http://localhost:$port/todos", newTodo, String::class.java)

		// レスポンスのステータスコードは OK である。
		assertThat(response.statusCode, equalTo(HttpStatus.OK))
	}

	@Test
	fun `POSTリクエストの後GETすると、項目が追加されている`() {
		// localhost/todos に POSTリクエストを発行し、項目を新規作成する。
		val newTodo = TodoEntity(text="hello")
		restTemplate.postForEntity("http://localhost:$port/todos", newTodo, String::class.java)

		// localhost/todos に GETリクエストを送り、TodoEntity の配列を取得する。
		val response = restTemplate.getForEntity("http://localhost:$port/todos", Array<TodoEntity>::class.java)
		val todos = response.body!!

		// 配列は1つの要素をもち、idはnullでない値、textは "hello" であること。
		assertThat(todos.size, equalTo(1))
		assertThat(todos[0].id, notNullValue())
		assertThat(todos[0].text, equalTo(newTodo.text))
	}

	@Test
	fun `POSTリクエストは新規作成した項目のIDを返す`() {
		// 項目を新規作成する。
		val newTodo = TodoEntity(text="hello")
		val postResponse = restTemplate.postForEntity("http://localhost:$port/todos", newTodo, Long::class.java)
		// 返されたIDを取得する。
		val newId = postResponse.body

		// TodoEntity の配列を取得する。
		val getResponse = restTemplate.getForEntity("http://localhost:$port/todos", Array<TodoEntity>::class.java)
		val todos = getResponse.body!!

		// 配列中に返されたIDをもつ要素があること。
		assertThat(todos[0].id, equalTo(newId))
		assertThat(todos[0].text, equalTo(newTodo.text))
	}

	@Test
	fun `特定の項目をIDを指定してGETできる`() {
		// 項目を新規作成する。
		val newTodo = TodoEntity(text="hello")
		val postResponse = restTemplate.postForEntity("http://localhost:$port/todos", newTodo, Long::class.java)
		val newId = postResponse.body

		// localhost/todos/$id に GETリクエストを送り、レスポンスを1個の TodoEntity として解釈する。
		val getResponse = restTemplate.getForEntity("http://localhost:$port/todos/$newId", TodoEntity::class.java)
		val actualTodo = getResponse.body!!

		// 新規作成したものと内容が一致している。
		assertThat(actualTodo.id, equalTo(newId))
		assertThat(actualTodo.text, equalTo(newTodo.text))
	}

	@Test
	fun `存在しないIDでGETすると404を返す`() {
		// id=999 を指定して GETリクエストを送る。
		val response = restTemplate.getForEntity("http://localhost:$port/todos/999", TodoEntity::class.java)

		// レスポンスのステータスコードは NOT_FOUND である。
		assertThat(response.statusCode, equalTo(HttpStatus.NOT_FOUND))
	}

	@Test
	fun `DELETEで削除できる`() {
		// 項目を新規作成する。
		val newTodo = TodoEntity(text="hello")
		val response1 = restTemplate.postForEntity("http://localhost:$port/todos", newTodo, Long::class.java)
		val newId = response1.body!!

		// localhost/todos/$id に DELETEリクエストを送る。
		restTemplate.delete("http://localhost:$port/todos/$newId")

		// その項目は存在しない (削除されている)。
		val response = restTemplate.getForEntity("http://localhost:$port/todos/$newId", TodoEntity::class.java)
		assertThat(response.statusCode, equalTo(HttpStatus.NOT_FOUND))
	}

}
