package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun QuestionScreen(
    viewModel: QuestionViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            uiState.question?.let { question ->
                // Soru Metni
                Text(
                    text = question.text,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .semantics { 
                            // Soru olduğunu özellikle belirtiyoruz
                            contentDescription = "Soru: ${question.text}"
                        }
                )

                // Şıklar
                question.options.forEach { option ->
                    val isSelected = uiState.selectedOptionId == option.id
                    val isCorrect = question.correctOptionId == option.id
                    val showResult = uiState.isAnswered

                    OptionItem(
                        option = option,
                        isSelected = isSelected,
                        isCorrect = isCorrect,
                        showResult = showResult,
                        onClick = { viewModel.onOptionSelected(option.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ekran Okuyucu otonom bildirimi için Live Region (Görsel olarak da geri bildirim verir)
            if (uiState.feedbackMessage.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .semantics {
                            liveRegion = LiveRegionMode.Polite
                        }
                ) {
                    Text(
                        text = uiState.feedbackMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.selectedOptionId == uiState.question?.correctOptionId) {
                            Color(0xFF2E7D32) // Koyu yeşil
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OptionItem(
    option: Option,
    isSelected: Boolean,
    isCorrect: Boolean,
    showResult: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Arka plan rengini belirleme
    val backgroundColor = when {
        !showResult && isSelected -> MaterialTheme.colorScheme.primaryContainer
        showResult && isCorrect -> Color(0xFFE8F5E9) // Açık Yeşil
        showResult && isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    // Durum açıklaması (Ekran okuyucu için)
    val stateDesc = when {
        !showResult && isSelected -> "Seçili"
        !showResult && !isSelected -> "Seçili değil"
        showResult && isCorrect -> "Doğru cevap"
        showResult && isSelected && !isCorrect -> "Yanlış cevap olarak seçtiniz"
        else -> ""
    }

    Card(
        modifier = modifier
            .semantics(mergeDescendants = true) {
                // Şıkkın okunmasını gruplaştırarak bilişsel yükü azaltıyoruz.
                role = Role.RadioButton
                stateDescription = stateDesc
            }
            .clickable(
                enabled = !showResult,
                onClick = onClick,
                // Semantics içerisinde onClick belirttiğimiz için Card'ın tıklanma özelliğini
                // ekran okuyucu uyumlu hale getiriyor.
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Şıkkın Harfi (A, B, vs)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected && !showResult) MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option.id,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected && !showResult) MaterialTheme.colorScheme.onPrimary 
                            else MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Şıkkın Metni
            Text(
                text = option.text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            // Sonuç durumuna göre ikon gösterme
            if (showResult) {
                if (isCorrect) {
                 Icon(
                     imageVector = Icons.Default.CheckCircle,
                     contentDescription = null, // Semantics'te hallettik
                     tint = Color(0xFF2E7D32)
                 )   
                } else if (isSelected && !isCorrect) {
                  Icon(
                      imageVector = Icons.Default.Close,
                      contentDescription = null, // Semantics'te hallettik
                      tint = MaterialTheme.colorScheme.error
                  )  
                }
            }
        }
    }
}
