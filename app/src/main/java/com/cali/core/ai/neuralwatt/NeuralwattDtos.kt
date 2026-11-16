package com.cali.core.ai.neuralwatt

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

// DTO cho POST /v1/chat/completions (OpenAI-compatible). Chỉ các trường app dùng.

@Serializable
internal data class ChatCompletionRequest(
    val model: String,
    @SerialName("max_tokens") val maxTokens: Int,
    val messages: List<ChatMessage>,
    val tools: List<ToolParam>,
    /** `"auto"` hoặc `{"type":"function","function":{"name":…}}`. */
    @SerialName("tool_choice") val toolChoice: JsonElement,
    @SerialName("reasoning_effort") val reasoningEffort: String? = null,
)

/** `content` là chuỗi (system / assistant) hoặc mảng phần (user có ảnh). */
@Serializable
internal data class ChatMessage(val role: String, val content: JsonElement)

@Serializable
internal data class ToolParam(val function: FunctionDef) {
    @OptIn(ExperimentalSerializationApi::class)
    @EncodeDefault
    val type: String = "function"
}

@Serializable
internal data class FunctionDef(val name: String, val description: String, val parameters: JsonObject)

// --- Response ---

@Serializable
internal data class ChatCompletionResponse(
    val id: String? = null,
    val model: String? = null,
    val choices: List<Choice> = emptyList(),
    val usage: Usage? = null,
)

@Serializable
internal data class Choice(
    @SerialName("finish_reason") val finishReason: String? = null,
    val message: ResponseMessage? = null,
)

@Serializable
internal data class ResponseMessage(
    val content: String? = null,
    val refusal: String? = null,
    @SerialName("tool_calls") val toolCalls: List<ToolCall> = emptyList(),
)

@Serializable
internal data class ToolCall(val id: String? = null, val function: FunctionCall? = null)

/** `arguments` là chuỗi JSON. */
@Serializable
internal data class FunctionCall(val name: String? = null, val arguments: String? = null)

@Serializable
internal data class Usage(
    @SerialName("prompt_tokens") val promptTokens: Int = 0,
    @SerialName("completion_tokens") val completionTokens: Int = 0,
    @SerialName("prompt_tokens_details") val promptTokensDetails: PromptTokensDetails? = null,
)

@Serializable
internal data class PromptTokensDetails(@SerialName("cached_tokens") val cachedTokens: Int? = null)

@Serializable
internal data class ErrorResponse(val error: ErrorBody? = null)

@Serializable
internal data class ErrorBody(
    val message: String? = null,
    val type: String? = null,
    val code: String? = null,
    /** 429 có thể kèm `retryable: false` (vd. `tpm_uncached_request_too_large`). */
    val retryable: Boolean? = null,
)

@Serializable
internal data class ModelsResponse(val scope: String? = null)
