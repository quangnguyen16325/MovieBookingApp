package com.example.moviebooking.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.moviebooking.ui.theme.AccentColor
import com.example.moviebooking.ui.theme.DarkNavyLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    isError: Boolean = false,
    errorMessage: String = "",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.White.copy(alpha = 0.7f)) },
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null, tint = AccentColor) } },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        isError = isError,
        supportingText = if (isError) { { Text(errorMessage, color = MaterialTheme.colorScheme.error) } } else null,
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            containerColor = DarkNavyLight,
            cursorColor = AccentColor,
            focusedBorderColor = AccentColor,
            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
            focusedLabelColor = AccentColor,
            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    imeAction: ImeAction = ImeAction.Done,
    isError: Boolean = false,
    errorMessage: String = "",
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.White.copy(alpha = 0.7f)) },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AccentColor) },
        trailingIcon = {
            val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    icon,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        isError = isError,
        supportingText = if (isError) { { Text(errorMessage, color = MaterialTheme.colorScheme.error) } } else null,
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            containerColor = Color.Black.copy(alpha = 0.7f),
            cursorColor = AccentColor,
            focusedBorderColor = AccentColor,
            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
            focusedLabelColor = AccentColor,
            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun MovieButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(50.dp)
            .fillMaxWidth(),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrimary) AccentColor else DarkNavyLight.copy(alpha = 0.7f),
            contentColor = Color.White
        )
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun HorizontalDivider(
    modifier: Modifier = Modifier
) {
    Divider(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.3f),
        thickness = 1.dp
    )
}