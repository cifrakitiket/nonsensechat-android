package com.nonsense.chat.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nonsense.chat.model.Poll

@Composable
fun PollView(poll: Poll, currentUid: String, onVote: (Int) -> Unit) {
    val myVotes = poll.votesOf(currentUid)
    val voted = myVotes.isNotEmpty()
    val total = poll.totalVoters.coerceAtLeast(1)

    Column(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(poll.question, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        if (poll.desc.isNotBlank()) {
            Text(poll.desc, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            buildString {
                append(if (poll.isQuiz) "Викторина" else "Опрос")
                if (poll.isAnonymous) append(" · анонимно")
                if (poll.isMultiple) append(" · неск. ответов")
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        poll.options.forEachIndexed { idx, option ->
            val count = poll.countFor(idx)
            val pct = if (voted) (count * 100 / total) else 0
            val chosen = idx in myVotes
            val correct = poll.isQuiz && voted && idx in poll.correctIdxs
            val barColor = when {
                correct -> MaterialTheme.colorScheme.primary
                chosen -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onVote(idx) },
            ) {
                // Progress fill.
                if (voted) {
                    Box(
                        Modifier
                            .fillMaxWidth(pct / 100f)
                            .background(barColor.copy(alpha = 0.35f))
                            .padding(vertical = 10.dp),
                    ) {}
                }
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        (if (chosen) "● " else "○ ") + option,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (voted) Text("$pct%", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Text("Проголосовало: $total", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
    }
}
