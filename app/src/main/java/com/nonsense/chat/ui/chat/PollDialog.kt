package com.nonsense.chat.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Composable
fun PollDialog(onDismiss: () -> Unit, onCreate: (poll: JsonElement, question: String) -> Unit) {
    var question by remember { mutableStateOf("") }
    val options = remember { mutableStateOf(listOf("", "")).value.toMutableStateList() }
    var isQuiz by remember { mutableStateOf(false) }
    var isMultiple by remember { mutableStateOf(false) }
    var isAnonymous by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новый опрос") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(question, { question = it }, label = { Text("Вопрос") },
                    modifier = Modifier.fillMaxWidth())
                options.forEachIndexed { i, opt ->
                    OutlinedTextField(opt, { options[i] = it }, label = { Text("Вариант ${i + 1}") },
                        modifier = Modifier.fillMaxWidth())
                }
                TextButton(onClick = { if (options.size < 12) options.add("") }) { Text("Добавить вариант") }
                ToggleRow("Режим викторины", isQuiz) { isQuiz = it }
                ToggleRow("Несколько ответов", isMultiple) { isMultiple = it }
                ToggleRow("Анонимно", isAnonymous) { isAnonymous = it }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val opts = options.map { it.trim() }.filter { it.isNotEmpty() }
                    if (question.isBlank() || opts.size < 2) return@TextButton
                    val poll = buildJsonObject {
                        put("question", question.trim())
                        put("desc", "")
                        put("options", buildJsonArray { opts.forEach { add(JsonPrimitive(it)) } })
                        put("isQuiz", isQuiz)
                        put("isMultiple", isMultiple)
                        put("isAnonymous", isAnonymous)
                        put("votes", buildJsonObject {})
                        put("correctIdxs", buildJsonArray {})
                    }
                    onCreate(poll, question.trim())
                    onDismiss()
                },
            ) { Text("Создать") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Checkbox(checked = checked, onCheckedChange = onChange)
    }
}
