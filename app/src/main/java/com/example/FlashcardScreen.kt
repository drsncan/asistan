package com.example

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.roundToInt

@Composable
fun FlashcardScreen(
    viewModel: QuestionViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.flashcardState.collectAsStateWithLifecycle()
    val currentCard = state.currentCard ?: return

    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(targetValue = offsetX, label = "offsetX")

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Heading for TalkBack
        Text(
            text = "Kavram Kartları",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .semantics { heading() },
            style = MaterialTheme.typography.titleLarge
        )

        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(300.dp)
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX > 150f) {
                                viewModel.markAsCorrect()
                            } else if (offsetX < -150f) {
                                viewModel.markAsIncorrect()
                            }
                            offsetX = 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount
                        }
                    )
                }
                .clickable(
                    onClickLabel = if (!state.isFlipped) "Cevabı dinle" else "Kartı çevir"
                ) {
                    viewModel.flipFlashcard()
                }
                .semantics {
                    role = Role.Button
                    if (!state.isFlipped) {
                        contentDescription = "Kavram Kartı: ${currentCard.frontText}. Cevabı dinlemek için çift dokunun."
                    } else {
                        contentDescription = "Cevap: ${currentCard.backText}."
                    }
                    customActions = listOf(
                        CustomAccessibilityAction("Doğru bildim") {
                            viewModel.markAsCorrect()
                            offsetX = 0f
                            true
                        },
                        CustomAccessibilityAction("Yanlış bildim") {
                            viewModel.markAsIncorrect()
                            offsetX = 0f
                            true
                        }
                    )
                },
            colors = CardDefaults.cardColors(
                containerColor = if (!state.isFlipped) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (!state.isFlipped) {
                    Text(
                        text = currentCard.frontText,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    Text(
                        text = currentCard.backText,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(16.dp)
                            .semantics {
                                liveRegion = LiveRegionMode.Polite
                            }
                    )
                }
            }
        }
    }
}
